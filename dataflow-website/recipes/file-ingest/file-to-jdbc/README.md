File to JDBC Ingest Spring Batch Sample 
---

This project implements a Spring Batch job that reads from a CSV text file with lines formatted as `first_name,last_name` and writes each entry to a database table using a [JdbcBatchItemWriter](https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/item/database/JdbcBatchItemWriter.html)] that executes `INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)` for each line. 


## Build

### To run locally with an H2 database or with Cloud Foundry Java Buildpack to bind to a JDBC service:

```bash
$ ./mvnw clean package
```

### To run in Kubernetes (and add a Mariadb driver)

```bash
$ ./mvnw clean package docker:build -Pkubernetes 
```

### To zip the source code and test data:

```bash
$ ./mvnw clean package -Pdist
``` 