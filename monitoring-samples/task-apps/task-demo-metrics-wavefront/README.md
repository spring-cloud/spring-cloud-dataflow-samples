
# How to monitor Spring Cloud Task

The `task-demo-metrics-wavefront` project creates a sample Spring Cloud Task (and Spring Batch) with the monitoring infrastructure, so it can be monitored in Spring Cloud Data Flow.  

## Create custom Spring Cloud Task
If you use the provided `task-demo-metrics-wavefront` source code you can skip this section. 

Otherwise, follow the instructions below to build new Task with enabled Wavefront monitoring  

Follow the [Task development instructions](https://docs.spring.io/spring-cloud-task/docs/2.0.0.RELEASE/reference/htmlsingle/#getting-started-developing-first-task) and then: 

* Set the parent `POM` version of Boot to 2.3.1.RELEASE or latest

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.3.1.RELEASE</version>
    <relativePath/>
</parent>
``` 
* Make sure that `spring-cloud-dependencies` version `Hoxton.SR6` or newer are imported: 

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Hoxton.SR6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

* Add dependencies to enable the `Spring Cloud Task` (and optionally `Spring Task Batch`) functionality and to configure the jdbc dependencies for the task repository. 

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-task</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
</dependencies>
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

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-wavefront</artifactId>
</dependency>
```
 
## Build

Run 
```
./mvnw clean install
```

Will produce `task-demo-metrics-wavefront-0.0.1-SNAPSHOT.jar` task application under the `target` folder.

## Spring Cloud Data Flow server

Follow the [Task Monitoring](https://dataflow.spring.io/docs/feature-guides/batch/monitoring) instructions  to register, run and monitor the task sample using Influx, Wavefront or Prometheus on the desired platform.

## Docker images

Build and publish docker image

## Fabric8 - Maven plugin
```
./mvnw clean install docker:build
./mvnw docker:push

```

## Jib - Maven plugin
```
./mvnw clean install jib:build
```
or for test purposes build a local image:
```
./mvnw clean install jib:dockerBuild
```

## Dockerfile

```
./mvnw clean install
docker build -t springcloud/task-demo-metrics-wavefront:dockerfile .
```
