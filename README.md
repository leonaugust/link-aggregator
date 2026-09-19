Для реальной отправки отчёта в ReportSendingE2EIT
нужно указать env переменные:

MAIL_SENDER_EMAIL - email отправителя
MAIL_SENDER_PASSWORD - пароль отправителя(токен)
MAIL_SENDER_RECIPIENT: получатель отчёта

Для антифрод-проверки ссылок через Kaspersky OpenTIP:
OPENTIP_API_KEY - API-токен OpenTIP. Без него приложение не стартует: запросы без токена
OpenTIP отклоняет с 401. Токен выдаётся на https://opentip.kaspersky.com/


docker compose -f docker-compose-ai.yml up -d to run ai locally