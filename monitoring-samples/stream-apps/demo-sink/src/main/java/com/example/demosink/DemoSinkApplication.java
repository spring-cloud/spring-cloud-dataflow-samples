package com.example.demosink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@SpringBootApplication
@EnableBinding(Sink.class)
public class DemoSinkApplication {

	private static final Logger logger = LoggerFactory.getLogger(DemoSinkApplication.class);

	@StreamListener(Sink.INPUT)
	public void myMessageHandler(String payload) {
		logger.info("Received: " + payload);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoSinkApplication.class, args);
	}

}
