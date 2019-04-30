package io.spring.dataflow.sample.usagecostlogger;

import io.spring.dataflow.sample.domain.UsageCostDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class UsageCostLogger {

	private static final Logger logger = LoggerFactory.getLogger(UsageCostLoggerApplication.class);

	@StreamListener(Sink.INPUT)
	public void process(UsageCostDetail usageCostDetail) {
		logger.info(usageCostDetail.toString());
	}
}
