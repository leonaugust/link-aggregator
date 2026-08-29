rootProject.name = "link-aggregator"
include("core")
include("core:application")
include("core:domain")

include("jacoco-aggregator")

include("adapters")
include("adapters:postgres-adapter")
include("adapters:rest-adapter")
include("adapters:email-adapter")
include("adapters:html-fetcher-adapter")
include("adapters:kafka-adapter")
include("adapters:util")

include("build-tools")

include("boot")

findProject(":core:application")?.name = "link-aggregator-core-application"
findProject(":core:domain")?.name = "link-aggregator-core-domain"

findProject(":adapters:postgres-adapter")?.name = "link-aggregator-postgres-adapter"
findProject(":adapters:rest-adapter")?.name = "link-aggregator-rest-adapter"
findProject(":adapters:email-adapter")?.name = "link-aggregator-email-adapter"
findProject(":adapters:html-fetcher-adapter")?.name = "link-aggregator-html-fetcher-adapter"
findProject(":adapters:kafka-adapter")?.name = "link-aggregator-kafka-adapter"
findProject(":adapters:util")?.name = "link-aggregator-util"

findProject(":boot")?.name = "boot"

findProject(":build-tools")?.name = "link-aggregator-build-tools"
