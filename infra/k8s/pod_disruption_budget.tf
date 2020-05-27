resource "kubernetes_pod_disruption_budget" "cash_flow_service_pdb" {
  metadata {
    name            = "cash-flow-service-pdb"
  }
  spec {
    min_available   = 1
    selector {
      match_labels  = {
        service     = "cash-flow-service"
      }
    }
  }
}