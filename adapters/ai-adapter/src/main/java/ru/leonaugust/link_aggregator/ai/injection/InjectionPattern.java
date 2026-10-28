package ru.leonaugust.link_aggregator.ai.injection;

import java.util.List;
import java.util.Objects;

/**
 * Паттерн prompt-injection.
 * <p>
 * Требования ({@code keywordGroups}) — группы синонимов: группа выполнена, если
 * в тексте найдено любое одно её слово (OR внутри группы). Паттерн срабатывает,
 * только когда выполнены ВСЕ группы (AND между группами).
 * <p>
 * Якоря ({@code anchors}) — редкие слова-маркеры атаки (jailbreak, exfiltrate,
 * reveal и т.п.). Каждый якорь обязан быть членом одной из групп этого паттерна.
 * Если якоря заданы, паттерн срабатывает, только когда совпал хотя бы один якорь:
 * это отсекает ложные срабатывания на обычной прозе со словами вроде "system",
 * "prompt", "now". Пустой список якорей сохраняет поведение без якорной проверки.
 */
record InjectionPattern(String name, List<List<String>> keywordGroups, List<String> anchors) {

    InjectionPattern {
        keywordGroups = keywordGroups == null ? List.of() : keywordGroups.stream()
                .filter(Objects::nonNull)
                .map(group -> group.stream().filter(Objects::nonNull).toList())
                .toList();
        anchors = anchors == null ? List.of() : anchors.stream().filter(Objects::nonNull).toList();
    }

    static InjectionPattern of(String name, List<String> keywords) {
        return new InjectionPattern(
                name,
                keywords.stream().map(List::of).toList(),
                List.of());
    }
}
