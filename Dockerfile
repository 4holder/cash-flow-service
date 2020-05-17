FROM openjdk:8-jre

WORKDIR /cash-flow-service-1.0

COPY cash-flow-service-1.0 .

EXPOSE 9000

ENTRYPOINT ["bin/cash-flow-service", "-Dhttp.port=9000", "-Dconfig.file=conf/application-prod.conf"]