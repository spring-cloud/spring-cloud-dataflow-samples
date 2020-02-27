package io.spring.dataflow.sample.usagecostlogger;

import java.util.function.Consumer;

import io.spring.dataflow.sample.UsageCostDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsageCostLogger {

	private static final Logger logger = LoggerFactory.getLogger(UsageCostLoggerApplication.class);

	@Bean
	public Consumer<UsageCostDetail> process() {
		return usageCostDetail -> {
			logger.info(usageCostDetail.toString());
		};
	}
}