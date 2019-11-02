# Stream Apps Monitoring

Ensure that the POM contains:

```xml
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-rabbit</artifactId>
		</dependency>


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
		...
	</dependencies>
```

For Kafka binder replace `spring-cloud-starter-stream-rabbit` by `spring-cloud-starter-stream-kafka` instead
