package com.example.scdfdsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.dsl.DeploymentPropertiesBuilder;
import org.springframework.cloud.dataflow.rest.client.dsl.Stream;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamApplication;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamBuilder;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ScdfdslApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScdfdslApplication.class, args);
	}

	private final Logger logger = LoggerFactory.getLogger(ScdfdslApplication.class);

	@Autowired
	private StreamApplication source;

	@Autowired
	private StreamApplication processor;

	@Autowired
	private StreamApplication sink;

	@Autowired
	private DataFlowOperations dataFlowOperations;

	@Autowired
	private StreamBuilder builder;

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
//		dataFlowOperations.appRegistryOperations().importFromResource(
//				"http://bit.ly/Celsius-SR1-stream-applications-rabbit-maven", true);
		if (applicationArguments.containsOption("style")) {
			String style = applicationArguments.getOptionValues("style").get(0);
			if (style.equalsIgnoreCase("definition")) {
				// DEFINITION STYLE
				definitionStyle(dataFlowOperations);
			} else if (style.equalsIgnoreCase("fluent")) {
				// FLUENT STYLE
				fluentStyle(dataFlowOperations);
			} else {
				logger.info("Style [" + style + "] not supported");
			}
		} else {
			definitionStyle(dataFlowOperations);
		}
	}


	private void definitionStyle(DataFlowOperations dataFlowOperations) throws InterruptedException {
		Map<String, String> deploymentProperties = createDeploymentProperties();

		logger.info("Deploying stream.");

		Stream woodchuck = builder
				.name("woodchuck")
				.definition(
				"http --server.port=9900 | splitter --expression=payload.split(' ') | log")
				.create()
				.deploy(deploymentProperties);

		waitAndDestroy(woodchuck);
	}



	private void fluentStyle(DataFlowOperations dataFlowOperations) throws InterruptedException {

		logger.info("Deploying stream.");

		Stream woodchuck = builder
				.name("woodchuck")
				.source(source)
				.processor(processor)
				.sink(sink)
				.create()
				.deploy();

		waitAndDestroy(woodchuck);
	}

	private Map<String, String> createDeploymentProperties() {
		DeploymentPropertiesBuilder propertiesBuilder = new DeploymentPropertiesBuilder();
		propertiesBuilder.memory("log", 512);
		propertiesBuilder.count("log",2);
		propertiesBuilder.put("app.splitter.producer.partitionKeyExpression", "payload");
		return propertiesBuilder.build();
	}


	private void waitAndDestroy(Stream stream) throws InterruptedException {
		while(!stream.getStatus().equals("deployed")){
			logger.info("Wating for deployment of stream.");
			Thread.sleep(5000);
		}

		logger.info("Letting the stream run for 2 minutes.");
		// Let woodchuck run for 2 minutes
		Thread.sleep(120000);

		logger.info("Destroying stream");
		stream.destroy();
	}

}
