package ru.leonaugust.link_aggregator.ai.injection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.ai.common.AiClient;
import ru.leonaugust.link_aggregator.ai.common.EmbedRequest;
import ru.leonaugust.link_aggregator.ai.common.EmbedResponse;
import ru.leonaugust.link_aggregator.ai.config.AiProperties;
import ru.leonaugust.link_aggregator.ai.config.PromptInjectionProperties;
import ru.leonaugust.link_aggregator.application.api.link.outbound.PromptInjectionDetector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Детекция prompt injection по косинусной близости эмбеддингов слов текста
 * к эмбеддингам ключевых слов известных атак.
 * ContentFetcherPort отдаёт текст в виде мешка слов без порядка, поэтому
 * векторный поиск выполняется по ключевым словам паттернов, а не по целым фразам:
 * эмбеддинг фразы, разложенной на мешок слов, неотличим от benign-текста.
 * <p>
 * Паттерн - это AND из групп, внутри группы OR из синонимов: группа выполнена,
 * если хотя бы одно её слово найдено в тексте. Дополнительно у паттерна есть
 * якоря - редкие маркеры атаки (jailbreak-маркер, глагол утечки, маркер персоны).
 * Без якорей обычная проза с похожими словами ("now a new model is available")
 * удовлетворяла бы группам и давала ложное срабатывание, поэтому паттерн
 * срабатывает, только если совпал хотя бы один якорь. Якорь - это то же слово,
 * что и член группы, поэтому дополнительных эмбеддингов проверка якорей не добавляет.
 * <p>
 * Якоря берутся только из редких слов паттерна: слова, которые практически не
 * встречаются в обычной технической прозе. Общая лексика
 * (show, print, output, system, instructions, extract, leak, bypass, покажи, без,
 * ограничений и подобные) в якоря не входит, поэтому атака, собранная только из
 * общих слов, не считается атакой. Такой класс пропусков принят осознанно:
 * те же слова встречаются в обычных заданиях ("верни только значение строки",
 * "покажи свой системный промпт" в статье о prompt engineering), и ложное
 * срабатывание молча помечает полезную ссылку как неопределённую. Точность
 * важнее полноты: пропуск виден на конкретной странице, ложное срабатывание
 * портит классификацию легитимной ссылки.
 * <p>
 * Перед эмбеддингами работает лексический пред-фильтр: если ни одно слово
 * страницы не начинается с якоря, паттерн сработать не может, и проверка
 * завершается без обращений к embed-сервису. Это необходимое условие, а не
 * достаточное, поэтому оно лишь ускоряет проверку и не меняет решения по
 * найденным якорям.
 * <p>
 * Эмбеддинги слов кэшируются между вызовами (ключевые слова паттернов и слова страницы
 * используют общий кэш), поэтому повторные страницы не обращаются к embed-сервису,
 * а первый вызов оплачивает только реально проверенные группы: якоря проверяются
 * первыми, и если ни один не совпал, группы паттерна не проверяются вовсе.
 * <p>
 * Проверка полностью реактивная (без .block()) и ограничена по времени, чтобы
 * не блокировать поток Kafka-консьюмера и не превысить max.poll.interval.ms:
 * не больше maxWords distinct-слов страницы, каждый embed - с таймаутом embedTimeout,
 * общий бюджет detectionTimeout; превышение бюджета или ошибка embed - fail-open
 * (текст считается чистым).
 */
@Slf4j
public class EmbeddingPromptInjectionDetector implements PromptInjectionDetector {

    private static final String PATTERNS_RESOURCE = "/ai/prompt-injection/patterns.json";
    private static final ObjectMapper MAPPER = createMapper();
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\p{N}");
    private static final Pattern FORMAT_CHARS_PATTERN = Pattern.compile("\\p{Cf}");
    private static final Map<Character, Character> LEET_REPLACEMENTS = Map.of(
            '0', 'o',
            '1', 'i',
            '3', 'e',
            '4', 'a',
            '5', 's',
            '7', 't');
    private static final int MIN_TOKEN_LENGTH = 3;
    private static final List<String> ENGLISH_SUFFIXES = List.of("ing", "ed", "es", "s");
    private static final int STEM_MIN_LENGTH = 4;
    private static final int MAX_MERGED_LETTER_RUN = 20;
    private static final int MAX_CACHED_WORD_EMBEDDINGS = 10_000;
    private static final int EMBEDDING_CONCURRENCY = 8;

    private final AiClient aiClient;
    private final AiProperties aiProperties;
    private final PromptInjectionProperties properties;
    private final List<InjectionPattern> patterns;
    private final Map<String, List<Float>> wordEmbeddings = new ConcurrentHashMap<>();
    private final Set<String> catalogueKeywords;
    private final int maxKeywordLength;
    private final List<String> anchorVocabulary;

    public EmbeddingPromptInjectionDetector(AiClient aiClient,
                                            AiProperties aiProperties,
                                            PromptInjectionProperties promptInjectionProperties) {
        this(aiClient, aiProperties, promptInjectionProperties, loadPatterns());
    }

    EmbeddingPromptInjectionDetector(AiClient aiClient,
                                     AiProperties aiProperties,
                                     PromptInjectionProperties promptInjectionProperties,
                                     List<InjectionPattern> patterns) {
        this.aiClient = aiClient;
        this.aiProperties = aiProperties;
        this.properties = promptInjectionProperties;
        this.patterns = patterns;
        this.catalogueKeywords = patterns.stream()
                .flatMap(pattern -> pattern.keywordGroups().stream())
                .flatMap(List::stream)
                .map(word -> word.toLowerCase(Locale.ROOT))
                .filter(word -> word.length() >= MIN_TOKEN_LENGTH)
                .collect(Collectors.toUnmodifiableSet());
        this.maxKeywordLength = catalogueKeywords.stream().mapToInt(String::length).max().orElse(0);
        this.anchorVocabulary = patterns.stream()
                .flatMap(pattern -> pattern.anchors().stream())
                .map(anchor -> anchor.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    @Override
    public Mono<Boolean> isInjection(String text) {
        List<String> tokens;
        try {
            tokens = tokenize(text);
        } catch (Exception exception) {
            log.warn("Не удалось разобрать текст для проверки на prompt-injection: {}", exception.getMessage());
            return Mono.just(false);
        }
        if (tokens.isEmpty() || patterns.stream().allMatch(pattern -> pattern.keywordGroups().isEmpty())) {
            return Mono.just(false);
        }
        if (!containsAnchorCandidate(tokens)) {
            log.debug("В тексте нет слов-кандидатов в якоря, проверка prompt-injection пропущена");
            return Mono.just(false);
        }
        var startedAt = System.nanoTime();
        return checkPatterns(tokens)
                .timeout(properties.detectionTimeout())
                .onErrorResume(TimeoutException.class, exception -> {
                    log.warn("Бюджет проверки на prompt-injection исчерпан ({}), текст считается чистым (fail-open)",
                            properties.detectionTimeout());
                    return Mono.just(false);
                })
                .onErrorResume(exception -> {
                    log.warn("Не удалось проверить текст на prompt-injection (fail-open), считаем текст чистым: {}",
                            exception.getMessage());
                    return Mono.just(false);
                })
                .doOnNext(injection -> log.debug("Проверка на prompt-injection завершена за {} мс, результат: {}",
                        (System.nanoTime() - startedAt) / 1_000_000, injection));
    }

    /**
     * Лексический пред-фильтр: слово страницы считается кандидатом в якорь,
     * если оно совпадает с якорем, начинается с него или совпадает после
     * отбрасывания частых английских словоизменительных суффиксов
     * ("ignoring", "revealing" → "ignore", "reveal"; "обойдите" ловится
     * префиксом от "обойди").
     */
    private boolean containsAnchorCandidate(List<String> tokens) {
        if (anchorVocabulary.isEmpty()) {
            return true;
        }
        return tokens.stream().anyMatch(this::isAnchorCandidate);
    }

    private boolean isAnchorCandidate(String token) {
        var stem = stripEnglishSuffix(token);
        for (var anchor : anchorVocabulary) {
            if (token.equals(anchor)
                    || token.startsWith(anchor)
                    || (stem.length() >= STEM_MIN_LENGTH && anchor.startsWith(stem))) {
                return true;
            }
        }
        return false;
    }

    private static String stripEnglishSuffix(String word) {
        for (var suffix : ENGLISH_SUFFIXES) {
            if (word.endsWith(suffix) && word.length() - suffix.length() >= MIN_TOKEN_LENGTH) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }

    private Mono<Boolean> checkPatterns(List<String> tokens) {
        return embedTokens(tokens).flatMap(vectors -> Flux.fromIterable(patterns)
                .concatMap(pattern -> matches(pattern, vectors)
                        .filter(Boolean::booleanValue)
                        .map(matched -> {
                            log.warn("Обнаружена попытка prompt injection, сработал паттерн: {}", pattern.name());
                            return matched;
                        }))
                .next()
                .defaultIfEmpty(false));
    }

    private Mono<Boolean> matches(InjectionPattern pattern, List<TokenVector> vectors) {
        var requirements = pattern.keywordGroups();
        if (requirements.isEmpty()) {
            return Mono.just(false);
        }
        return matchedAnchor(pattern, vectors).flatMap(anchor -> {
            if (!pattern.anchors().isEmpty() && anchor.isEmpty()) {
                return Mono.just(false);
            }
            return matchRequirement(orderByAnchor(requirements, anchor), 0, vectors);
        });
    }

    /**
     * Группа, в которой найден якорь, проверяется первой: эмбеддинг якоря уже в кэше,
     * поэтому группа закрывается без дополнительных запросов.
     */
    private static List<List<String>> orderByAnchor(List<List<String>> requirements, String anchor) {
        if (anchor.isEmpty()) {
            return requirements;
        }
        for (int index = 0; index < requirements.size(); index++) {
            if (requirements.get(index).contains(anchor)) {
                if (index == 0) {
                    return requirements;
                }
                var ordered = new ArrayList<>(requirements);
                var group = ordered.remove(index);
                ordered.add(0, group);
                return ordered;
            }
        }
        return requirements;
    }

    private Mono<Boolean> matchRequirement(List<List<String>> requirements, int index, List<TokenVector> vectors) {
        if (index >= requirements.size()) {
            return Mono.just(true);
        }
        return groupSatisfied(requirements.get(index), vectors)
                .flatMap(satisfied -> satisfied
                        ? matchRequirement(requirements, index + 1, vectors)
                        : Mono.just(false));
    }

    private Mono<Boolean> groupSatisfied(List<String> group, List<TokenVector> vectors) {
        if (group.isEmpty()) {
            return Mono.just(false);
        }
        return Flux.fromIterable(group)
                .flatMap(keyword -> embedWord(keyword)
                        .map(keywordEmbedding -> matchesAny(vectors, keywordEmbedding))
                        .onErrorResume(exception -> Mono.just(false)), EMBEDDING_CONCURRENCY)
                .filter(Boolean::booleanValue)
                .next()
                .defaultIfEmpty(false);
    }

    private Mono<String> matchedAnchor(InjectionPattern pattern, List<TokenVector> vectors) {
        if (pattern.anchors().isEmpty()) {
            return Mono.just("");
        }
        return Flux.fromIterable(pattern.anchors())
                .flatMap(anchor -> embedWord(anchor)
                        .map(anchorEmbedding -> matchesAny(vectors, anchorEmbedding) ? anchor : "")
                        .onErrorResume(exception -> Mono.just("")), EMBEDDING_CONCURRENCY)
                .filter(anchor -> !anchor.isEmpty())
                .next()
                .defaultIfEmpty("");
    }

    private boolean matchesAny(List<TokenVector> vectors, List<Float> keywordEmbedding) {
        for (var vector : vectors) {
            if (cosine(vector.embedding(), keywordEmbedding) >= properties.threshold()) {
                return true;
            }
        }
        return false;
    }

    private Mono<List<TokenVector>> embedTokens(List<String> tokens) {
        return Flux.fromIterable(tokens)
                .flatMap(token -> embedWord(token)
                        .map(TokenVector::new)
                        .onErrorResume(exception -> Mono.empty()), EMBEDDING_CONCURRENCY)
                .collectList();
    }

    private Mono<List<Float>> embedWord(String word) {
        var cached = wordEmbeddings.get(word);
        if (cached != null) {
            return Mono.just(cached);
        }
        return Mono.defer(() -> {
            if (wordEmbeddings.size() >= MAX_CACHED_WORD_EMBEDDINGS) {
                wordEmbeddings.clear();
            }
            return aiClient.embed(new EmbedRequest(aiProperties.embedModel(), word))
                    .timeout(properties.embedTimeout())
                    .map(EmbedResponse::embedding)
                    .doOnNext(embedding -> wordEmbeddings.put(word, embedding));
        });
    }

    /**
     * Нормализация текста и извлечение слов.
     * NFKC сворачивает полноширинные символы и прочие совместимые варианты записи,
     * символы формата (zero-width, soft hyphen, BOM) удаляются - ими слова разбивают,
     * чтобы обойти детектор. Слова короче трёх букв отбрасываются, повторы удаляются,
     * итоговый список ограничен maxWords.
     * <p>
     * Склеенные по одной букве слова ("i g n o r e", "i.g.n.o.r.e", "i  g  n  o  r  e")
     * собираются в одно слово: разделителем может быть любой набор пробелов, точек
     * и дефисов, иначе обфускация двойным пробелом обходит детектор.
     * Многобуквенные куски не склеиваются: "ignore-instructions" остаётся двумя
     * словами, иначе AND-сопоставление по двум словам перестало бы работать.
     * <p>
     * Склейка разбирается по каталогу ключевых слов: из накопленного ряда букв
     * выделяются слова каталога ("a i g n o r e" даёт "ignore", а
     * "i g n o r e   p r e v i o u s" даёт "ignore" и "previous"). Выдуманные
     * фрагменты ("nore", "gnore") не выделяются: они только добавляют шум в
     * эмбеддинги. Слишком длинные склейки разбиваются на части, а не отбрасываются
     * молча.
     */
    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        var normalized = Normalizer.normalize(text, Normalizer.Form.NFKC);
        normalized = FORMAT_CHARS_PATTERN.matcher(normalized).replaceAll("");
        var distinct = new LinkedHashSet<String>();
        var letterRun = new StringBuilder();
        var matcher = ALPHANUMERIC_PATTERN.matcher(normalized.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            var chunk = matcher.group();
            if (chunk.length() == 1 && Character.isLetter(chunk.charAt(0))) {
                letterRun.append(chunk);
                continue;
            }
            flushLetterRun(distinct, letterRun);
            letterRun.setLength(0);
            addChunk(distinct, chunk);
        }
        flushLetterRun(distinct, letterRun);
        return List.copyOf(distinct);
    }

    private void flushLetterRun(Set<String> distinct, StringBuilder run) {
        if (run.isEmpty()) {
            return;
        }
        var merged = run.toString();
        if (merged.length() <= MAX_MERGED_LETTER_RUN) {
            addToken(distinct, merged);
        } else {
            for (var start = 0; start < merged.length(); start += MAX_MERGED_LETTER_RUN) {
                addToken(distinct, merged.substring(start, Math.min(start + MAX_MERGED_LETTER_RUN, merged.length())));
            }
        }
        addCatalogueWords(distinct, merged);
    }

    /**
     * Жадный разбор склейки по словам каталога: берётся самое длинное подходящее
     * слово, поэтому "instructions" не превращается в "instruction".
     */
    private void addCatalogueWords(Set<String> distinct, String merged) {
        var index = 0;
        while (index < merged.length()) {
            var word = longestCatalogueWord(merged, index);
            if (word == null) {
                index++;
                continue;
            }
            addToken(distinct, word);
            index += word.length();
        }
    }

    private String longestCatalogueWord(String merged, int from) {
        var maxLength = Math.min(maxKeywordLength, merged.length() - from);
        for (var length = maxLength; length >= MIN_TOKEN_LENGTH; length--) {
            var candidate = merged.substring(from, from + length);
            if (catalogueKeywords.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void addChunk(Set<String> distinct, String chunk) {
        if (!DIGIT_PATTERN.matcher(chunk).find()) {
            addToken(distinct, chunk);
            return;
        }
        var folded = foldLeet(chunk);
        if (folded.length() >= MIN_TOKEN_LENGTH) {
            addToken(distinct, folded);
            return;
        }
        var letters = DIGIT_PATTERN.matcher(chunk).replaceAll("");
        if (letters.length() >= MIN_TOKEN_LENGTH) {
            addToken(distinct, letters);
        }
    }

    /**
     * Складывает leet-подстановки только если все цифры слова раскладываются в буквы,
     * иначе вернёт пустую строку - тогда используется слово без цифр.
     */
    private static String foldLeet(String chunk) {
        var folded = new StringBuilder(chunk.length());
        for (var index = 0; index < chunk.length(); index++) {
            var symbol = chunk.charAt(index);
            if (Character.isLetter(symbol)) {
                folded.append(symbol);
                continue;
            }
            var replacement = LEET_REPLACEMENTS.get(symbol);
            if (replacement == null) {
                return "";
            }
            folded.append(replacement);
        }
        return folded.toString();
    }

    private void addToken(Set<String> distinct, String token) {
        if (token.length() >= MIN_TOKEN_LENGTH && distinct.size() < properties.maxWords()) {
            distinct.add(token);
        }
    }

    private static double cosine(List<Float> first, List<Float> second) {
        if (first.size() != second.size()) {
            return 0;
        }
        double dot = 0;
        double normFirst = 0;
        double normSecond = 0;
        for (int i = 0; i < first.size(); i++) {
            double a = first.get(i);
            double b = second.get(i);
            dot += a * b;
            normFirst += a * a;
            normSecond += b * b;
        }
        if (normFirst == 0 || normSecond == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normFirst) * Math.sqrt(normSecond));
    }

    static List<InjectionPattern> loadPatterns() {
        return loadPatterns(PATTERNS_RESOURCE);
    }

    static List<InjectionPattern> loadPatterns(String resourcePath) {
        try (InputStream resource = EmbeddingPromptInjectionDetector.class.getResourceAsStream(resourcePath)) {
            if (resource == null) {
                log.warn("Ресурс с паттернами prompt-injection не найден: {}", resourcePath);
                return List.of();
            }
            return parsePatterns(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            log.warn("Не удалось прочитать паттерны prompt-injection из {} (fail-open): {}",
                    resourcePath, exception.getMessage());
            return List.of();
        }
    }

    static List<InjectionPattern> parsePatterns(String json) {
        try {
            var root = MAPPER.readTree(json);
            if (!root.isArray()) {
                log.warn("Ресурс паттернов prompt-injection ожидает массив, получено: {}", root.getNodeType());
                return List.of();
            }
            var patterns = MAPPER.treeToValue(root, new TypeReference<List<InjectionPattern>>() {
            });
            log.info("Загружено паттернов prompt-injection: {}", patterns.size());
            return patterns;
        } catch (IOException exception) {
            log.warn("Не удалось разобрать паттерны prompt-injection (fail-open): {}", exception.getMessage());
            return List.of();
        }
    }

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private record TokenVector(List<Float> embedding) {
    }
}
