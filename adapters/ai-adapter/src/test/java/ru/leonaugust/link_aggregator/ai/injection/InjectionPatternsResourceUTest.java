package ru.leonaugust.link_aggregator.ai.injection;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InjectionPatternsResourceUTest {

    private static final int MIN_PATTERNS = 29;
    private static final int MAX_DISTINCT_KEYWORDS = 400;
    private static final int MIN_KEYWORD_LENGTH = 3;

    /**
     * Частые слова обычной прозы: якорь из такого слова давал ложные срабатывания,
     * поэтому в роли якоря они не используются, а остаются только в группах-контексте.
     */
    private static final Set<String> GENERIC_WORDS = Set.of(
            "now", "new", "all", "everything", "any", "only", "just", "else", "with", "show", "say", "read",
            "act", "keep", "dump", "mode", "model", "rules", "system", "prompt", "developer", "instructions",
            "your", "how", "very", "more", "some", "one", "first", "last", "next", "previous", "text", "word",
            "words", "answer", "enable", "free", "different", "another", "change", "safety", "security", "content",
            "policy", "policies", "guidelines", "principles", "order", "limits", "without", "past", "prior", "initial",
            "original", "hidden", "secret", "confidential", "internal", "admin", "root", "urgent",
            "critical", "above", "below", "following", "earlier", "preceding", "single", "solely", "exclusively",
            "stop", "quit", "cease", "html", "xml", "markdown", "educational", "academically",
            "theoretically", "purely", "character", "assume", "simulate", "persona", "role", "expert", "professional",
            "specialist", "professor", "doctor", "operator", "owner", "credentials", "secrets", "endpoint",
            "email", "url", "server", "myself", "my", "yourself", "memory", "context", "follow", "obey", "execute",
            "command", "commands", "nothing", "token", "string", "phrase", "behavior", "behaviour", "unlimited",
            "print", "return", "reply", "respond", "output", "extract", "leak", "expose", "overflow", "bypass",
            "repeat", "tell", "evil", "злой", "свободный", "без", "ограничений", "покажи", "отобрази", "расскажи",
            "изображай", "выдавай", "выдумай", "представь", "примите", "считайте", "вообрази", "imagine");

    private final List<InjectionPattern> patterns = EmbeddingPromptInjectionDetector.loadPatterns();

    @Test
    void patternsResource_containsEnoughPatterns() {
        assertThat(patterns).hasSizeGreaterThanOrEqualTo(MIN_PATTERNS);
    }

    @Test
    void patternsResource_patternNamesAreUnique() {
        var names = patterns.stream().map(InjectionPattern::name).toList();

        assertThat(names).doesNotHaveDuplicates();
    }

    @Test
    void patternsResource_everyPatternHasAtLeastTwoKeywordGroups() {
        assertThat(patterns).allSatisfy(pattern ->
                assertThat(pattern.keywordGroups()).hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void patternsResource_everyKeywordGroupIsNotEmpty() {
        assertThat(patterns).allSatisfy(pattern ->
                assertThat(pattern.keywordGroups()).allSatisfy(group ->
                        assertThat(group).isNotEmpty()));
    }

    @Test
    void patternsResource_everyKeywordIsLowercaseWordOfAtLeastThreeChars() {
        assertThat(patterns).allSatisfy(pattern ->
                assertThat(pattern.keywordGroups()).allSatisfy(group ->
                        assertThat(group).allSatisfy(keyword -> {
                            assertThat(keyword).hasSizeGreaterThanOrEqualTo(MIN_KEYWORD_LENGTH);
                            assertThat(keyword.chars().allMatch(Character::isLetterOrDigit)).isTrue();
                            assertThat(keyword).isEqualTo(keyword.toLowerCase(Locale.ROOT));
                        })));
    }

    @Test
    void patternsResource_everyPatternHasName() {
        assertThat(patterns).allSatisfy(pattern -> assertThat(pattern.name()).isNotBlank());
    }

    @Test
    void patternsResource_everyPatternHasAnchors() {
        assertThat(patterns).allSatisfy(pattern ->
                assertThat(pattern.anchors()).isNotEmpty());
    }

    @Test
    void patternsResource_everyAnchorIsMemberOfPatternGroups() {
        assertThat(patterns).allSatisfy(pattern -> {
            var members = pattern.keywordGroups().stream().flatMap(List::stream).toList();
            assertThat(pattern.anchors()).allSatisfy(anchor ->
                    assertThat(members).contains(anchor));
        });
    }

    @Test
    void patternsResource_noAnchorIsGenericProseWord() {
        assertThat(patterns).allSatisfy(pattern ->
                assertThat(pattern.anchors()).allSatisfy(anchor ->
                        assertThat(GENERIC_WORDS).doesNotContain(anchor)));
    }

    @Test
    void patternsResource_distinctKeywordCountWithinBudget() {
        var distinct = patterns.stream()
                .flatMap(pattern -> pattern.keywordGroups().stream())
                .flatMap(List::stream)
                .distinct()
                .toList();

        assertThat(distinct).hasSizeLessThanOrEqualTo(MAX_DISTINCT_KEYWORDS);
    }
}
