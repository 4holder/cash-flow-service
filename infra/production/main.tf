variable "gcloud_project_id" { default = "fin2you" }
variable "credentials_file" { default = "../credentials/account.json" }

variable "region" {}
variable "environment" {}
variable "k8s_username" {}
variable "k8s_password" {}
variable "k8s_namespace" {}
variable "cluster_name" {}
variable "replicas" { default = 1 }
variable "min_replicas" { default = 1 }
variable "max_replicas" { default = 2 }

variable "default_cash_flow_service_container" { default = "gcr.io/fin2you/cash-flow-service:db92da27c9aef9e85f65d3c74f0e6cfd5eed9ecb" }

variable "application_secret" {}
variable "http_application_secret" {}

variable "database_connection_string" {}

variable "gcloud_sql_instance" {}

locals {
  zone = "${var.region}-a"
}

provider "google" {
  credentials = file("../credentials/account.json")
  project     = var.gcloud_project_id
  region      = var.region
}

terraform {
  backend "gcs" {
    bucket      = "cash_flow_service_production_terraform"
    prefix      = "cash_flow_service_production.tfstate"
    credentials = "../credentials/account.json"
  }
}

module "cash_flow_service" {
  source                      = "../modules/k8s"
  credentials_file            = var.credentials_file
  gcloud_project_id           = var.gcloud_project_id
  gcloud_region               = local.zone

  default_container           = var.default_cash_flow_service_container
  environment                 = var.environment

  k8s_master_host             = data.terraform_remote_state.infra_production_state.outputs.endpoint
  k8s_ca_certificate          = data.terraform_remote_state.infra_production_state.outputs.cluster_ca_certificate
  k8s_client_certificate      = data.terraform_remote_state.infra_production_state.outputs.client_certificate
  k8s_client_key              = data.terraform_remote_state.infra_production_state.outputs.client_key
  k8s_username                = var.k8s_username
  k8s_password                = var.k8s_password
  k8s_namespace               = var.k8s_namespace
  cluster_name                = var.cluster_name
  replicas                    = var.replicas
  min_replicas                = var.min_replicas
  max_replicas                = var.max_replicas

  gcloud_sql_instance         = var.gcloud_sql_instance

  database_connection_string  = var.database_connection_string
  application_secret          = var.application_secret
  http_application_secret     = var.http_application_secret
}
