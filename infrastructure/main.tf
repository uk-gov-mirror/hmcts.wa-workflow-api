provider "azurerm" {
  features {}
}

locals {
  key_vault_name                  = "${var.product}-${var.env}"
}

data "azurerm_key_vault" "wa_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}
