package com.example.task.taskdemometrics;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableTask
@EnableBatchProcessing
public class TaskDemoMetricsApplication {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	private Random random = new Random();

	public static void main(String[] args) {
		SpringApplication.run(TaskDemoMetricsApplication.class, args);
	}

	@Bean
	public Job job1() {
		return this.jobBuilderFactory.get("job1")
				.start(step1())
				.next(step2())
				.incrementer(new RunIdIncrementer())
				.build();
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.<Integer, Integer>chunk(10)
				.reader(new ListItemReader<>(IntStream.rangeClosed(0, this.random.nextInt(100))
						.boxed().collect(Collectors.toList())))
				.writer(list -> list.forEach(e -> {
					if ((e % 100) == 0) {
						System.out.println(e);
					}
				})).build();
	}

	@Bean
	public Step step2() {
		return this.stepBuilderFactory.get("step2")
				.tasklet((contribution, context) -> {
					Thread.sleep(2 * 60 * 1000 + this.random.nextInt(10000));
					return RepeatStatus.FINISHED;
				}).build();
	}

	@Bean
	public Job job2() {
		return jobBuilderFactory.get("job2")
				.start(stepBuilderFactory.get("job2step1")
						.tasklet((contribution, chunkContext) -> {
							Thread.sleep(this.random.nextInt(10000));
							return RepeatStatus.FINISHED;
						})
						.build())
				.incrementer(new RunIdIncrementer())
				.build();
	}
}
