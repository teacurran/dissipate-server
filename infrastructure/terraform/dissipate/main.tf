variable "project" {
  default = ""
}
variable "environment" {
  default = ""
}
variable "billing_account" {
  default = ""
}
variable "region" {
  default = ""
}

variable "org_id" {
  default = ""
}
resource "google_project" "project" {
  auto_create_network = true
  name            = var.project
  project_id      = var.project
  billing_account     = var.billing_account
  org_id         = var.org_id

  labels = {
    environment = var.environment
    firebase = "enabled"
  }
}

