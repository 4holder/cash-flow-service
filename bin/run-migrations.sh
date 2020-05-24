#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker run --name cloudsqlproxy -d --network host --rm -v $DIR/../infra/credentials:/secrets/cloudsql \
  gcr.io/cloudsql-docker/gce-proxy:1.16 \
  ./cloud_sql_proxy \
  -instances=$GCLOUD_SQL_INSTANCE \
  -credential_file=/secrets/cloudsql/account.json

docker run --network host --rm -v $DIR/../migrations:/flyway/sql boxfuse/flyway:5.2.4 \
  -url=jdbc:postgresql://localhost:5432/$DB_NAME \
  -schemas=public \
  -user=$DB_USER \
  -password=$DB_PWD \
  -connectRetries=30 migrate

docker stop cloudsqlproxy