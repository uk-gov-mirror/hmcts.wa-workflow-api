provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "postgres_network"
  subscription_id            = var.aks_subscription_id
}

data "azurerm_key_vault" "wa_key_vault" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-${var.env}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
  name         = "microservicekey-wa-workflow-api"
}

resource "azurerm_key_vault_secret" "s2s_secret_workflow_api" {
  name         = "s2s-secret-workflow-api"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

module "wa_workflow_api_database" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = var.product
  name               = "${var.product}-${var.component}-postgres-db"
  location           = var.location
  env                = var.env
  database_name      = var.postgresql_database_name
  postgresql_user    = var.postgresql_user
  postgresql_version = "11"
  common_tags        = merge(var.common_tags, tomap({ "lastUpdated" = timestamp() }))
  subscription       = var.subscription
}

//Azure Flexible Server DB
module "wa_workflow_api_database_flex" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source          = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  product         = var.product
  component       = var.component
  name            = "${var.product}-${var.component}-postgres-db-flex"
  location        = var.location
  business_area   = "cft"
  env             = var.env
  pgsql_databases = [
    {
      name : var.postgresql_database_name
    }
  ]

  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql,pg_stat_statements,pg_buffercache"
    }
  ]

  pgsql_version = 15
  common_tags   = merge(var.common_tags, tomap({ "lastUpdated" = timestamp() }))

  admin_user_object_id = var.jenkins_AAD_objectId

}

//Save secrets in vault
resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.wa_workflow_api_database.user_name
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.wa_workflow_api_database.postgresql_password
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.wa_workflow_api_database.host_name
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.wa_workflow_api_database.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.wa_workflow_api_database.postgresql_database
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

// Secrets for Postgres Flex Server DB
resource "azurerm_key_vault_secret" "POSTGRES-USER-V15" {
  name         = "${var.component}-POSTGRES-USER-V15"
  value        = module.wa_workflow_api_database_flex.username
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V15" {
  name         = "${var.component}-POSTGRES-PASS-V15"
  value        = module.wa_workflow_api_database_flex.password
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST-V15" {
  name         = "${var.component}-POSTGRES-HOST-V15"
  value        = module.wa_workflow_api_database_flex.fqdn
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT-V15" {
  name         = "${var.component}-POSTGRES-PORT-V15"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE-V15" {
  name         = "${var.component}-POSTGRES-DATABASE-V15"
  value        = "${var.postgresql_database_name}"
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}
