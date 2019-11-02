# Stream Apps Monitoring

Add `org.springframework.cloud:spring-cloud-stream` and `org.springframework.cloud:spring-cloud-starter-stream-rabbit` to your Spring Boot application 
POM to turn it into Stream Cloud Stream Application:

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    </dependency>
```

To replace Rabbit by Kafka binder use the `org.springframework.cloud:spring-cloud-starter-stream-kafka` dependency instead instead.

To enable SCDF Prometheus Proxy monitoring for your application you would need to add the following additional additional dependencies: 

```xml
    <dependency>
        <groupId>org.springframework.cloud.stream.app</groupId>
        <artifactId>app-starters-micrometer-common</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>    
    <dependency>
        <groupId>io.micrometer.prometheus</groupId>
        <artifactId>prometheus-rsocket-spring</artifactId>
        <version>0.9.0</version>
    </dependency>
```

The `micrometer-registry-prometheus` amd `prometheus-rsocket-spring` activate the Micrometer Prometheus support for RSocket Proxy meter registry.
The `app-starters-micrometer-common` would inject some common metrics Tags such as `stream.name`, `stream.app.label`, `application.type` and `application.guid`.
Later are used in the SCDF Grafna Dashboards to visualize the Stream Application rates, throughput and so on.  

To find more about the Spring Cloud Data Flow Monitoring architecture visit the [Stream Monitoring with Prometheus and InfluxDB](https://dataflow.spring.io/docs/2.3.0.SNAPSHOT/feature-guides/streams/monitoring/) documentation. 
