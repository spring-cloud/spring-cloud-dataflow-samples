package com.example.scdfdsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.dsl.Stream;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ScdfdslApplication implements CommandLineRunner {

	@Autowired
	private Stream.StreamNameBuilder builder;

	public static void main(String[] args) {
		SpringApplication.run(ScdfdslApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		System.out.println(builder);

		Map<String, String> deploymentProperties = new HashMap<>();
		deploymentProperties.put("app.splitter.producer.partitionKeyExpression",
				"payload");
		deploymentProperties.put("deployer.log.count", "2");

		Stream woodchuck = builder.name("woodchuck").definition(
				"http --server.port=9900 | splitter --expression=payload.split(' ') | log")
				.create().deploy(deploymentProperties);

		while(!woodchuck.getStatus().equals("deployed")){
			Thread.sleep(5000);
		}

		woodchuck.destroy();
	}
}
