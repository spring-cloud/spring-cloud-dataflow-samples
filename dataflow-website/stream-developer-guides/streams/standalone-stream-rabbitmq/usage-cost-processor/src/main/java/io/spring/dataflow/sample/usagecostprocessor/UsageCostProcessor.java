package io.spring.dataflow.sample.usagecostprocessor;

import io.spring.dataflow.sample.domain.UsageCostDetail;
import io.spring.dataflow.sample.domain.UsageDetail;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

@EnableBinding(Processor.class)
public class UsageCostProcessor {

	private double ratePerSecond = 0.1;

	private double ratePerMB = 0.05;

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public UsageCostDetail processUsageCost(UsageDetail usageDetail) {
		UsageCostDetail usageCostDetail = new UsageCostDetail();
		usageCostDetail.setUserId(usageDetail.getUserId());
		usageCostDetail.setCallCost(usageDetail.getDuration() * this.ratePerSecond);
		usageCostDetail.setDataCost(usageDetail.getData() * this.ratePerMB);
		return usageCostDetail;
	}
}
