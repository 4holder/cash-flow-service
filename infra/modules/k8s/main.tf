data "google_client_config" "default" {}

provider "google" {
  credentials = file(var.credentials_file)
  project     = var.gcloud_project_id
  region      = var.gcloud_region
}

data "google_container_cluster" "four_holder_production_cluster" {
  name = var.cluster_name
  location = var.gcloud_region
}

provider "kubernetes" {
  load_config_file = false
  host = data.google_container_cluster.four_holder_production_cluster.endpoint
  token = data.google_client_config.default.access_token

  client_certificate = base64decode(data.google_container_cluster.four_holder_production_cluster.master_auth[0].client_certificate)
  client_key = base64decode(data.google_container_cluster.four_holder_production_cluster.master_auth[0].client_key)
  cluster_ca_certificate = base64decode(data.google_container_cluster.four_holder_production_cluster.master_auth[0].cluster_ca_certificate)
}
