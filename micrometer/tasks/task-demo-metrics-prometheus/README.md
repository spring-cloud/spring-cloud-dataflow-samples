
# How to monitor Spring Cloud Tasks

The `task-demo-metrics-prometheus` project creates a sample Spring Cloud Task (and Spring Batch) enabled for Spring Cloud Data Flow monitoring.  

## Create custom Spring Cloud Task
If you use the provided `task-demo-metrics-prometheus` source code you can skip this section. 

Otherwise follow the instructions below to build your own `monitorable` Task from scratch.  

Bootstrap by follow the [Task development instructions](https://docs.spring.io/spring-cloud-task/docs/2.0.0.RELEASE/reference/htmlsingle/#getting-started-developing-first-task) and then: 

* Set the POM parent to Boot 2.2.0.M6 or newer

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.0.M6</version>
    <relativePath/>
</parent>
``` 
* Make sure that `spring-cloud-dependencies` version `Hoxton.M2` or newer are imported: 

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Hoxton.M2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
</dependencyManagement>
```

* Add dependencies to enable the `Spring Cloud Task` (and optionally `Spring Task Batch`) functionality and to configure the jdbc dependencies for the task repository.
Use version `2.2.0.M2` or newer! 

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-task</artifactId>
        <version>2.2.0.M2</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-core</artifactId>
        <version>2.2.0.M2</version>
    </dependency>
    
    <!-- Required when Spring Batch is used -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-batch</artifactId>
        <version>2.2.0.M2</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-stream</artifactId>
        <version>2.2.0.M2</version>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-task-dependencies</artifactId>
            <version>2.2.0.M2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
``` 

* Add dependencies to configure the jdbc dependencies for the task repository: 

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <scope>runtime</scope>
</dependency>
``` 

* Add dependencies to configure Micrometer integration: 

The Micrometer library uses the actuator internally: 
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

To enable Prometheus metrics collection add:

```xml
<dependency>
    <groupId>io.micrometer.prometheus</groupId>
    <artifactId>prometheus-rsocket-spring</artifactId>
    <version>0.8.0</version>
</dependency>
<dependency>
    <groupId>io.micrometer.prometheus</groupId>
    <artifactId>prometheus-rsocket-client</artifactId>
    <version>0.8.0</version>
</dependency>
```
Note that the version must be `0.8.0` or newer.
 
## Build

Run 
```
./mvnw clean install
```

Will produce `task-demo-metrics-prometheus-0.0.1-SNAPSHOT.jar` task application under the `target` folder.

## Spring Cloud Data FLow server

Follow the [Task Monitoring](https://dataflow.spring.io/docs/feature-guides/batch/monitoring) instructions to register, run and monitor the demo tasks with Influx or Prometheus on various platforms.

## Docker images

Build and publish docker image

```
./mvnw clean install docker:build
./mvnw docker:push

```
