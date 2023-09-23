
# How to monitor Spring Cloud Task

The `task-demo-metrics-prometheus` project creates a sample Spring Cloud Task (and Spring Batch) with the monitoring infrastructure, so it can be monitored in Spring Cloud Data Flow.  

## Create custom Spring Cloud Task
If you use the provided `task-demo-metrics-prometheus` source code you can skip this section. 

Otherwise, follow the instructions below to build your own `monitorable` Task from scratch.  

Follow the [Task development instructions](https://docs.spring.io/spring-cloud-task/docs/2.3.0/reference/#getting-started-developing-first-task) and then: 

* Set the parent POM version of Boot to 2.7.16 or latest

```xml
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.7.16</version>
	<relativePath/>
</parent>
``` 
* Make sure that `spring-cloud-task-dependencies` version `2.4.6` or newer are imported: 

```xml
	<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-task-dependencies</artifactId>
			<version>2.4.6</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```

* Enable Spring Batch and Spring Cloud Task 

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
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <scope>runtime</scope>
</dependency>
``` 

* Add dependencies to configure Micrometer integration: 

To enable Prometheus metrics collection add:

```xml
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
	<groupId>io.micrometer.prometheus</groupId>
	<artifactId>prometheus-rsocket-spring</artifactId>
	<version>1.5.2</version>
</dependency>
```
 
* Configure the Application metadata generation

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

```xml
<plugin>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dataflow-apps-metadata-plugin</artifactId>
    <version>1.0.7</version>
    <configuration>
        <storeFilteredMetadata>true</storeFilteredMetadata>
        <metadataFilter>
            <names>
            </names>
            <sourceTypes>
                <filter>io.spring.task.taskdemometrics.TaskDemoMetricsProperties</filter>
            </sourceTypes>
        </metadataFilter>
    </configuration>
    <executions>
        <execution>
            <id>aggregate-metadata</id>
            <phase>compile</phase>
            <goals>
                <goal>aggregate-metadata</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>properties-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>read-project-properties</goal>
            </goals>
            <configuration>
                <files>
                    <file>${project.build.outputDirectory}/META-INF/spring-configuration-metadata-encoded.properties</file>
                </files>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>0.33.0</version>
    <configuration>
        <images>
            <image>
                <name>springcloudtask/${project.artifactId}</name>
                <build>
                    <tags>
                        <tag>latest</tag>
                        <tag>${project.version}</tag>
                    </tags>
                    <from>springcloud/baseimage:1.0.0</from>
                    <volumes>
                        <volume>/tmp</volume>
                    </volumes>
                    <labels>
                        <org.springframework.cloud.dataflow.spring-configuration-metadata.json>
                            ${org.springframework.cloud.dataflow.spring.configuration.metadata.json}
                        </org.springframework.cloud.dataflow.spring-configuration-metadata.json>
                    </labels>
                    <entryPoint>
                        <exec>
                            <arg>java</arg>
                            <arg>-jar</arg>
                            <arg>/maven/task-demo-metrics-prometheus.jar</arg>
                        </exec>
                    </entryPoint>
                    <assembly>
                        <descriptor>assembly.xml</descriptor>
                    </assembly>
                </build>
            </image>
        </images>
    </configuration>
</plugin>

```

## Build

Run 
```
./mvnw clean install
```

Will produce `task-demo-metrics-prometheus-2.0.0-SNAPSHOT.jar` task application under the `target` folder.

## Spring Cloud Data FLow server

Follow the [Task Monitoring](https://dataflow.spring.io/docs/feature-guides/batch/monitoring) instructions  to register, run and monitor the task sample using Influx or Prometheus on the desired platform.

## Docker images

Build and publish docker image

```
./mvnw clean install docker:build
./mvnw docker:push
```
