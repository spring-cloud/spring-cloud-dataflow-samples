package org.springframework.ingest.config;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.ingest.domain.Person;
import org.springframework.ingest.mapper.fieldset.PersonFieldSetMapper;
import org.springframework.ingest.processor.PersonItemProcessor;
import org.springframework.ingest.resource.RemoteResource;
import org.springframework.ingest.resource.sftp.SftpRemoteResource;

/**
 * Class used to configure the batch job related beans.
 *
 * @author Chris Schaefer
 */
@Configuration
@EnableBatchProcessing
@EnableConfigurationProperties(BatchConfigurationProperties.class)
public class BatchConfiguration {
	private final DataSource dataSource;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final BatchConfigurationProperties batchConfigurationProperties;

	@Autowired
	public BatchConfiguration(final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
							  final StepBuilderFactory stepBuilderFactory,
							  final BatchConfigurationProperties batchConfigurationProperties) {
		this.dataSource = dataSource;
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.batchConfigurationProperties = batchConfigurationProperties;
	}

	@Bean
	@StepScope
	public StepExecutionListener ingestStepExecutionListener(@Value("#{jobParameters['remoteFilePath']}") String remoteFilePath,
								@Value("#{jobParameters['localFilePath']}") String localFilePath) {
			return new StepExecutionListener() {
				@Override
				public void beforeStep(StepExecution stepExecution) {
					try {
						Resource fetchedResource = remoteResource().getResource(remoteFilePath);
						FileUtils.copyInputStreamToFile(fetchedResource.getInputStream(), new File(localFilePath));
					}
					catch (Exception e) {
						throw new RuntimeException("Could not write remote file to local disk", e);
					}
				}

				@Override
				public ExitStatus afterStep(StepExecution stepExecution) {
					return null;
				}
			};
	}

	@Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['localFilePath']}") String localFilePath) throws Exception {
		return new FlatFileItemReaderBuilder<Person>()
			.name("reader")
			.resource(new FileSystemResource(localFilePath))
			.delimited()
			.names(new String[] {"firstName", "lastName"})
			.fieldSetMapper(new PersonFieldSetMapper())
			.build();
	}

	@Bean
	public ItemProcessor<Person, Person> processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public ItemWriter<Person> writer() {
		return new JdbcBatchItemWriterBuilder<Person>()
			.beanMapped()
			.dataSource(this.dataSource)
			.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
			.build();
	}

	@Bean
	public Job ingestJob() throws Exception {
		return jobBuilderFactory.get("ingestJob")
			.incrementer(new RunIdIncrementer())
			.flow(step1())
			.end()
			.build();
	}

	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("ingest")
			.<Person, Person>chunk(10)
			.reader(reader(null))
			.processor(processor())
			.writer(writer())
			.listener(ingestStepExecutionListener(null, null))
			.build();
	}

	@Bean
	public RemoteResource remoteResource() {
		return new SftpRemoteResource(batchConfigurationProperties.getSftpHost(), batchConfigurationProperties.getSftpPort(),
				batchConfigurationProperties.getSftpUsername(), batchConfigurationProperties.getSftpPassword());
	}
}
