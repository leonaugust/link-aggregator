Для реальной отправки отчёта в ReportSendingE2EIT
нужно указать env переменные:

MAIL_SENDER_EMAIL - email отправителя
MAIL_SENDER_PASSWORD - пароль отправителя(токен)
MAIL_SENDER_RECIPIENT: получатель отчёта


docker compose -f docker-compose-ai.yml up -d to run ai locally