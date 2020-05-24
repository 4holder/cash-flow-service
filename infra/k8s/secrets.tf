resource "kubernetes_secret" "cash_flow_service_secrets" {
  metadata {
    name                        = "cash-flow-service-secrets"
  }

  data = {
    APPLICATION_SECRET          = var.application_secret
    HTTP_APPLICATION_SECRET     = var.http_application_secret
    DATABASE_CONNECTION_STRING  = var.database_connection_string
  }
}
