package com.example.scdfdsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.dsl.Stream;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamApplication;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamBuilder;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ScdfdslApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScdfdslApplication.class, args);
	}

	@Autowired
	private StreamApplication source;

	@Autowired
	private StreamApplication processor;

	@Autowired
	private StreamApplication sink;

	// Using @Bean defintions makes it easier to reuse an application in multiple streams
	@Bean
	public StreamApplication source() {
		return new StreamApplication("http").addProperty("server.port", 9900);
	}

	@Bean
	public StreamApplication processor() {
		return new StreamApplication("splitter")
				.addProperty("producer.partitionKeyExpression", "payload");
	}

	@Bean
	public StreamApplication sink() {
		return new StreamApplication("log")
				.addDeploymentProperty("count", 2);
	}

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		DataFlowOperations dataFlowOperations = createDataFlowOperations(applicationArguments);

		if (applicationArguments.containsOption("style")) {
			String style = applicationArguments.getOptionValues("style").get(0);
			if (style.equalsIgnoreCase("definition")) {
				// DEFINITION STYLE
				definitionStyle(dataFlowOperations);
			} else if (style.equalsIgnoreCase("fluent")) {
				// FLUENT STYLE
				fluentStyle(dataFlowOperations);
			} else {
				System.out.println("Style [" + style + "] not supported");
			}
		} else {
			definitionStyle(dataFlowOperations);
		}
	}


	private void definitionStyle(DataFlowOperations dataFlowOperations) throws InterruptedException {
		Map<String, String> deploymentProperties = createDeploymentProperties();

		System.out.println("Deploying stream.");

		Stream woodchuck = Stream.builder(dataFlowOperations)
				.name("woodchuck")
				.definition(
				"http --server.port=9900 | splitter --expression=payload.split(' ') | log")
				.create()
				.deploy(deploymentProperties);

		waitAndDestroy(woodchuck);
	}



	private void fluentStyle(DataFlowOperations dataFlowOperations) throws InterruptedException {

		System.out.println("Deploying stream.");

		Stream woodchuck = Stream.builder(dataFlowOperations).name("woodchuck")
				.source(source)
				.processor(processor)
				.sink(sink)
				.create()
				.deploy();

		waitAndDestroy(woodchuck);
	}

	private Map<String, String> createDeploymentProperties() {
		Map<String, String> deploymentProperties = new HashMap<>();
		deploymentProperties.put("app.splitter.producer.partitionKeyExpression", "payload");
		deploymentProperties.put("deployer.log.count", "2");
		return deploymentProperties;
	}


	private DataFlowOperations createDataFlowOperations(ApplicationArguments applicationArguments) {
		DataFlowOperations dataFlowOperations;
		if (applicationArguments.containsOption("uri")) {
			URI dataFlowUri = URI.create(applicationArguments.getOptionValues("uri").get(0));
			dataFlowOperations = new DataFlowTemplate(dataFlowUri);
		} else {
			dataFlowOperations = new DataFlowTemplate(URI.create("http://localhost:9393"));
		}

		dataFlowOperations.appRegistryOperations().importFromResource(
				"http://bit.ly/Celsius-RC1-stream-applications-rabbit-maven", true);
		return dataFlowOperations;
	}

	private void waitAndDestroy(Stream stream) throws InterruptedException {
		while(!stream.getStatus().equals("deployed")){
			System.out.println("Wating for deployment of stream.");
			Thread.sleep(5000);
		}

		System.out.println("Letting the stream run for 2 minutes.");
		// Let woodchuck run for 2 minutes
		Thread.sleep(120000);

		System.out.println("Destroying stream");
		stream.destroy();
	}

}
