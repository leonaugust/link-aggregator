package ru.leonaugust.link_aggregator.fraud.opentip;

import java.util.Arrays;

/**
 * Зона, в которую Kaspersky OpenTIP помещает найденный объект индикаторов компрометации.
 * Значения соответствуют одноимённому полю {@code Zone} в ответе API.
 *
 * <p>Семантика зон и решение о блокировке сосредоточены здесь: политика безопасности
 * проверки ссылок должна читаться в одном месте, а не собираться из флагов в конфигурации.
 */
public enum OpenTipZone {

  /** Критическая угроза: вредоносное ПО, фишинг и подобное. Ссылка блокируется. */
  RED,

  /** Высокая подозрительность. Ссылка блокируется. */
  ORANGE,

  /**
   * Средняя подозрительность, в том числе контентные категории (adult, gambling и т.п.).
   * Вредоносности не означает, поэтому проверка не блокирующая.
   */
  YELLOW,

  /** Объект безопасный. */
  GREEN,

  /** Данных об объекте нет. */
  GREY,

  /** Зона не распознана: значение пустое либо API вернул неизвестную зону. */
  UNKNOWN;

  /**
   * Разбирает значение поля {@code Zone}. Пустое значение и неизвестная зона дают
   * {@link #UNKNOWN}, чтобы изменение контракта OpenTIP не приводило к блокировке ссылок.
   */
  public static OpenTipZone from(String raw) {
    if (raw == null || raw.isBlank()) {
      return UNKNOWN;
    }
    var value = raw.trim();
    return Arrays.stream(values())
        .filter(zone -> zone.name().equalsIgnoreCase(value))
        .findFirst()
        .orElse(UNKNOWN);
  }

  /** {@code true} для зон, которые означают вредоносность и должны блокировать сохранение ссылки. */
  public boolean blocking() {
    return this == RED || this == ORANGE;
  }
}