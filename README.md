# fiteo

Ktor based backend written with kotlin multiplatform. Exposed used as ORM, flyway for database migration.
Project contains 3 different APIs.

* fiteo - api created for training app
* ToDo - api created for ToDo application: https://github.com/mariuszmarzec/todo
* CheatDay - api for gathering data about weights for CheatDay application: https://github.com/mariuszmarzec/cheatDay

## Setup

1. Set local.properties with connection details to database. Variable `database.migration` is intended for switching 
environment for flyway migration tool.

```properties
database.migration=test

database.testEndpoint=jdbc:mysql://127.0.0.1:3306/fiteo_test_database?createDatabaseIfNotExist=TRUE
database.testUser=admin
database.testPassword=password
database.testDatabase=FITEO_TEST_DATABASE

database.endpoint=jdbc:mysql://127.0.0.1:3306/fiteo_database?createDatabaseIfNotExist=TRUE
database.user=admin
database.password=password
database.database=fiteo_database
```

2. After setting database configuration run `flywayMigrate` task to migrate database.

## How to run and deploy
To run locally fire `application:run` task. To build fat jar with all dependencies use 
`build:jvmJar` task.

## Tests
Test uses hardcoded database working on localhost. You can use mysql bundled in xampp for instance.
To run all tests use `verification:jvmTest` task. 
To generate test coverage report use `verification:jacocoTestReport`.