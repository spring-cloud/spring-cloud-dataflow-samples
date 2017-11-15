package com.example.scdfdsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.dsl.Stream;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamApplication;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ScdfdslApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScdfdslApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		URI dataFlowUri = URI.create("http://localhost:9393");
		DataFlowOperations dataFlowOperations = new DataFlowTemplate(dataFlowUri);

		dataFlowOperations.appRegistryOperations().importFromResource(
				"http://bit.ly/Celsius-RC1-stream-applications-rabbit-maven", true);

		Map<String, String> deploymentProperties = new HashMap<>();
		deploymentProperties.put("app.splitter.producer.partitionKeyExpression", "payload");
		deploymentProperties.put("deployer.log.count", "2");

		System.out.println("Deploying stream.");
		Stream woodchuck = Stream.Builder(dataFlowOperations).name("woodchuck").definition(
				"http --server.port=9900 | splitter --expression=payload.split(' ') | log")
				.create().deploy(deploymentProperties);

		while(!woodchuck.getStatus().equals("deployed")){
			System.out.println("Wating for deployment of stream.");
			Thread.sleep(5000);
		}

		System.out.println("Letting the stream run for 2 minutes.");
		// Let woodchuck run for 2 minutes
		Thread.sleep(120000);

		System.out.println("Destroying stream");
		woodchuck.destroy();

		// Fluent
		Stream woodchuck2 = Stream.Builder(dataFlowOperations).name("woodchuck")
				.source(new StreamApplication("http").addProperty("server.port", 9900))
				.processor(new StreamApplication("splitter")
						.addProperty("producer.partitionKeyExpression", "payload"))
				.sink(new StreamApplication("log")
						.addDeploymentProperty("count", 2))
				.create().deploy();

		while(!woodchuck2.getStatus().equals("deployed")){
			System.out.println("Wating for deployment of stream.");
			Thread.sleep(5000);
		}

		System.out.println("Letting the stream run for 2 minutes.");
		// Let woodchuck run for 2 minutes
		Thread.sleep(120000);

		System.out.println("Destroying stream");
		woodchuck2.destroy();

	}
}
