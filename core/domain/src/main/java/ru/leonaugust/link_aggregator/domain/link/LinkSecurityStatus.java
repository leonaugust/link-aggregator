package ru.leonaugust.link_aggregator.domain.link;

/**
 * Статус безопасности ссылки, определённый на этапе антифрод-проверки.
 * <p>
 * {@link #CHECK_OK} — ссылка прошла проверку репутации URL и не содержит
 * попытки prompt injection, её можно категоризировать.
 * {@link #SUSPICIOUS} — ссылка признана подозрительной (вредоносный URL или
 * инъекция в контенте): она сохраняется, но дальше не категоризируется.
 */
public enum LinkSecurityStatus {
  CHECK_OK,
  SUSPICIOUS
}
