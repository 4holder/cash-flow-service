data "terraform_remote_state" "infra_production_state" {
  backend = "gcs"
  config = {
    bucket      = "infra_production_terraform"
    prefix      = "production.tfstate"
    credentials = "./credentials/account.json"
  }
}