provider "azurerm" {
  features {}
}

locals {
  preview_vault_name     = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name         = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

}


data "azurerm_key_vault" "wa_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault_secret" "testsecret" {
  name      = "testsecret"
  vault_uri = "${data.azurerm_key_vault.wa_key_vault.vault_uri}"
}
