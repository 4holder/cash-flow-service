variable "credentials_file" {}
variable "gcloud_project_id" {}
variable "gcloud_region" {}

variable "default_container" { }

variable "environment" {}
variable "cluster_name" {}
variable "k8s_master_host" {}
variable "k8s_ca_certificate" {}
variable "k8s_client_certificate" {}
variable "k8s_client_key" {}
variable "k8s_username" {}
variable "k8s_password" {}
variable "k8s_namespace" { default = "default" }

variable "gcloud_sql_instance" {}

variable "replicas" {}
variable "min_replicas" {}
variable "max_replicas" {}

variable "application_secret" {}
variable "http_application_secret" {}
variable "database_connection_string" {}

