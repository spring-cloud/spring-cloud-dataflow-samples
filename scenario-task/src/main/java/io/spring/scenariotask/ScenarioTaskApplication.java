package io.spring.scenariotask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
@EnableTask
public class ScenarioTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScenarioTaskApplication.class, args);
	}

}
