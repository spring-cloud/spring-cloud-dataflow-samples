
# How to monitor Spring Cloud Task

The `task-demo-metrics-influx` project creates a sample Spring Cloud Task (and Spring Batch) with the monitoring infrastructure, so it can be monitored in Spring Cloud Data Flow.

## Create custom Spring Cloud Task
If you use the provided `task-demo-metrics-influx` source code you can skip this section. 

Otherwise follow the instructions below to build your own `monitorable` Task from scratch.  

Bootstrap by follow the [Task development instructions](https://docs.spring.io/spring-cloud-task/docs/2.0.0.RELEASE/reference/htmlsingle/#getting-started-developing-first-task) and then: 

* Set the parent POM version of Boot to 2.2.1.RELEASE or latest

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.1.RELEASE</version>
    <relativePath/>
</parent>
``` 

* Make sure that `spring-cloud-dependencies` version `Hoxton.RC1` or newer are imported: 

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Hoxton.RC1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
</dependencyManagement>
```

* Add dependencies to enable the `Spring Cloud Task` (and optionally `Spring Task Batch`) functionality and to configure the jdbc dependencies for the task repository.
Use version `2.2.0.RC1` or newer! 

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-task</artifactId>
        <version>2.2.0.RC1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-core</artifactId>
        <version>2.2.0.RC1</version>
    </dependency>
    
    <!-- Required when Spring Batch is used -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-batch</artifactId>
        <version>2.2.0.RC1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-task-stream</artifactId>
        <version>2.2.0.RC1</version>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-task-dependencies</artifactId>
            <version>2.2.0.RC1</version>
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

To enable InfuxDB metrics add the following dependencies in place of the prometheus one:  
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-influx</artifactId>
</dependency>
``` 
## Build

Run 
```
./mvnw clean install
```

Will produce `task-demo-metrics-influx-0.0.1-SNAPSHOT.jar` task application under the `target` folder.

## Spring Cloud Data FLow server

Follow the [Task Monitoring](https://dataflow.spring.io/docs/feature-guides/batch/monitoring) instructions  to register, run and monitor the task sample using Influx or Prometheus on the desired platform.

## Docker images

Build and publish docker image

```
./mvnw clean install docker:build
./mvnw docker:push
```
