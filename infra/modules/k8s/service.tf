resource "kubernetes_service" "cash_flow_service" {
  metadata {
    name = "cash-flow-service"
    labels = {
      service = "cash-flow-service"
    }
  }

  spec {
    port {
      name = "api-http"
      protocol    = "TCP"
      port        = 80
      target_port = 9000
    }

    selector = {
      environment = var.environment
      service = "cash-flow-service"
    }

    type = "NodePort"
  }
}
