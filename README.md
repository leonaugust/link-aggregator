Для реальной отправки отчёта в ReportSendingE2EIT
нужно указать env переменные:

MAIL_SENDER_EMAIL - email отправителя
MAIL_SENDER_PASSWORD - пароль отправителя(токен)
MAIL_SENDER_RECIPIENT: получатель отчёта

Для антифрод-проверки ссылок через Kaspersky OpenTIP:
OPENTIP_API_KEY - API-токен OpenTIP. Без него приложение не стартует: запросы без токена
OpenTIP отклоняет с 401. Токен выдаётся на https://opentip.kaspersky.com/


docker compose -f docker-compose-ai.yml up -d to run ai locally

## SonarQube (локальный анализ покрытия)

Сервис вынесен в отдельный compose-файл (как AI), основной docker-compose.yaml не трогаем.

Требование для Elasticsearch (bootstrap-проверки) — опционально: в этом compose они отключены
через `SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true`, поэтому шаг нужен только если проверки включены
(на уже настроенных системах значение обычно достаточно велико):

sudo sysctl -w vm.max_map_count=262144

Запуск:

docker compose -f docker-compose-sonar.yml up -d

Дождаться статуса UP:

until curl -sf http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do sleep 5; done

Сканеру SonarQube нужен токен (логин/пароль для `./gradlew sonar` не подходит). `admin/admin` —
начальные учётные данные и на свежем инстансе работают для создания токена через API, но
веб-интерфейс SonarQube (2025.1+/26.x) принудительно требует смены пароля администратора при
первом входе, после чего `admin/admin` перестаёт работать — используйте новый пароль для
последующих генераций токена.

Создать токен:
- UI: http://localhost:9000 -> My Account -> Security -> Generate Tokens
- API (работает только пока пароль admin не изменён):
  curl -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate?name=local"

Запуск анализа (агрегированный JaCoCo XML подхватывается из
jacoco-aggregator/build/jacocoReports/testCodeCoverageReport/testCodeCoverageReport.xml):

SONAR_TOKEN=<token> ./gradlew :jacoco-aggregator:verifySonarJacocoReport :jacoco-aggregator:sonar

Токен можно задать как Gradle-свойство (хранится вне репозитория в ~/.gradle/gradle.properties,
не требует env-переменной):

sonar.token=<token>   # добавить в ~/.gradle/gradle.properties

Автоматизация целиком (ожидание UP + генерация/переиспользование токена + анализ):

./scripts/sonar-analyze.sh

Анализ можно встроить в фазу `check` (после сборки). По умолчанию выключен, чтобы
`clean build` не падал без запущенного SonarQube/токена. Включение:

./gradlew clean build -Psonar.analyze=true     # или SONAR_ANALYZE=true ./gradlew clean build

Если `SONAR_PORT` отличается от 9000, укажите адрес для Gradle-задачи:

export SONAR_HOST_URL=http://localhost:<port>

Проверка покрытия:
- UI: http://localhost:9000/dashboard?id=link-aggregator
- API: curl -u <token>: "http://localhost:9000/api/measures/component?component=link-aggregator&metricKeys=coverage"

Проверить агрегированный JaCoCo XML до отправки в Sonar:

./gradlew verifySonarJacocoReport
