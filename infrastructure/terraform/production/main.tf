provider "google" {
  project     = "gweb-restor-staging"
  region      = "us-central1"
}

module "dissipate" {
  source = "../dissipate"
  environment = "production"
  project = "dissipate"
  region = "us-central1"
  billing_account = "013172-ABFA8D-19EDC9"
  org_id = "301727278003"
}