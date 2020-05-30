resource "kubernetes_deployment" "cash_flow_service" {

  metadata {
    name = "cash-flow-service"
    labels = {
      service = "cash-flow-service"
      environment = var.environment
    }
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = {
        service = "cash-flow-service"
        environment = var.environment
      }
    }

    template {
      metadata {
        labels = {
          service = "cash-flow-service"
          environment = var.environment
        }
      }

      spec {
        container {
          image = var.default_container
          name = "cash-flow-service"

          port {
            name = "api-http"
            container_port = 9000
          }

          env_from {
            config_map_ref {
              name = "cash-flow-service-config"
            }
          }

          env_from {
            secret_ref {
              name = "cash-flow-service-secrets"
            }
          }

          resources {
            limits {
              cpu  = "200m"
            }
            requests {
              cpu  = "50m"
            }
          }

          liveness_probe {
            http_get {
              scheme = "HTTP"
              path = "/infra/health"
              port = 9000
            }
            timeout_seconds = 5
            success_threshold = 1
            failure_threshold = 5
            period_seconds = 30
            initial_delay_seconds = 45
          }

          readiness_probe {
            http_get {
              scheme = "HTTP"
              path = "/infra/health"
              port = 9000
            }
            timeout_seconds = 5
            success_threshold = 1
            failure_threshold = 5
            period_seconds = 30
            initial_delay_seconds = 30
          }
        }
        container {
          name = "cloudsql-proxy"
          image = "gcr.io/cloudsql-docker/gce-proxy:1.16"
          command = [
            "/cloud_sql_proxy",
            "-instances=${var.gcloud_sql_instance}",
            "-credential_file=/secrets/cloudsql/account.json"
          ]

          security_context {
            run_as_user = 2
            allow_privilege_escalation = false
          }

          volume_mount {
            name = "cloudsql-instance-credentials"
            mount_path = "/secrets/cloudsql"
            read_only = true
          }
        }
        volume {
          name = "cloudsql-instance-credentials"
          secret {
            secret_name = "cloudsql-instance-credentials"
          }
        }
      }
    }
  }

  lifecycle {
    ignore_changes = [
      "spec[0].template[0].spec[0].container[0].image"
    ]
  }
}
