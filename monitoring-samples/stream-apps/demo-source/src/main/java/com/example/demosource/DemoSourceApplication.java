package com.example.demosource;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@EnableBinding(Source.class)
@SpringBootApplication
public class DemoSourceApplication {

	@Autowired
	private Source source;

	@Scheduled(fixedDelay = 1000)
	public void sendGreetingEvents() {
		this.source.output().send(MessageBuilder.withPayload("Greeting: " + new Date().toString()).build());
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoSourceApplication.class, args);
	}

}
