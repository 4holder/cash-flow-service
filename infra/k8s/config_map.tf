resource "kubernetes_config_map" "cash_flow_service_config" {
  metadata {
    name                = "cash-flow-service-config"
  }

  data = {}
}
