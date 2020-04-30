package com.example.logsink;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;

@Configuration
public class LogConsumerConfiguration {
	public LogConsumerConfiguration() {
	}

	@Bean
	IntegrationFlow logConsumerFlow() {
		return IntegrationFlows.from(MessageConsumer.class, (gateway) -> gateway.beanName("logConsumer"))
				.handle((payload, headers) -> {
					if (payload instanceof byte[]) {
						return new String((byte[]) payload);
					}
					return payload;
				})
				.log(LoggingHandler.Level.INFO, "log-consumer", "payload")
				.get();
	}

	private interface MessageConsumer extends Consumer<Message<?>> {}
}
