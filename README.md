# Cash Flow Service

In a nutshell: this service is responsible for the financial contracts, incomes and expenses management.

### Dependencies
- Scala 2.12
- Sbt 0.13
- Docker

### Running tests

Unit tests
```shell script
 sbt unit
```

Before running the integration tests, run docker-compose file on project root.

Integration tests
```shell script
 sbt integration
```

Coverage
```shell script
 sbt coverage test
 sbt coverageReport
```

### Running application

If you want some data to poke around locally, run the seed command:

```shell script
 sbt seedDatabase
```

```shell script
 sbt run
```