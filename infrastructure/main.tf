provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
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

locals {
  db_name = "${var.product}-${var.component}-postgres-db-flex"
}

//Azure Flexible Server DB
module "wa_workflow_api_database_flex" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source                      = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  product                     = var.product
  component                   = var.component
  name                        = local.db_name
  location                    = var.location
  business_area               = "cft"
  env                         = var.env
  action_group_name           = join("-", [local.db_name, var.action_group_name])
  email_address_key           = var.email_address_key
  email_address_key_vault_id  = data.azurerm_key_vault.wa_key_vault.id

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
  value        = var.postgresql_database_name
  key_vault_id = data.azurerm_key_vault.wa_key_vault.id
}
