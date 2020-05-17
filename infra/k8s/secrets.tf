resource "kubernetes_secret" "cash_flow_service_secrets" {
  metadata {
    name                = "cash-flow-service-secrets"
  }

  data = {
    APPLICATION_SECRET  = var.application_secret
    DATABASE_USER       = var.database_user
    DATABASE_PASSWORD   = var.database_password
  }
}
