resource "kubernetes_config_map" "cash_flow_service_config" {
  metadata {
    name                = "cash-flow-service-config"
  }

  data = {
    DATABASE_NAME       = var.database_name
    DATABASE_HOST       = var.database_host
    DATABASE_PORT       = var.database_port
  }
}
