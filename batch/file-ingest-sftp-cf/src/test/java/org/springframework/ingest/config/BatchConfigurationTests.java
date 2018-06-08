package org.springframework.ingest.config;

import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

import org.apache.commons.io.FileUtils;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BatchConfiguration test cases
 *
 * @author Chris Schaefer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@EnableConfigurationProperties(BatchConfigurationProperties.class)
public class BatchConfigurationTests {
	private static int port;
	private static SshServer server;
	private static AnnotationConfigApplicationContext context;

	private static final String SFTP_USER = "user";
	private static final String SFTP_PASS = "pass";
	private static final String SFTP_HOST = "127.0.0.1";
	private static final String REMOTE_FILE = "people.csv";
	private static final String HOST_KEY_FILE = "hostkey.ser";

	@ClassRule
	public static final TemporaryFolder remoteTemporaryFolder = new TemporaryFolder();

	@BeforeClass
	public static void createServer() throws Exception {
		File createdFile = remoteTemporaryFolder.newFile(REMOTE_FILE);
		FileUtils.writeStringToFile(createdFile, "Jill,Doe\nJoe,Doe\nJustin,Doe\nJane,Doe\nJohn,Doe");

		server = SshServer.setUpDefaultServer();
		server.setPasswordAuthenticator((username, password, session) -> true);
		server.setPort(0);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(HOST_KEY_FILE)));
		server.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystemFactory()));
		server.setFileSystemFactory(new VirtualFileSystemFactory(remoteTemporaryFolder.getRoot().toPath()));
		server.start();

		port = server.getPort();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stop();

		File hostkey = new File(HOST_KEY_FILE);

		if (hostkey.exists()) {
			hostkey.delete();
		}
	}

	@Before
	public void createContext() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("sftp_host", SFTP_HOST);
		properties.put("sftp_port", port);
		properties.put("sftp_username", SFTP_USER);
		properties.put("sftp_password", SFTP_PASS);

		ConfigurableEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().addFirst(new MapPropertySource("sftpProperties", properties));

		context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class, DataSourceConfiguration.class);
		context.setEnvironment(environment);
		context.refresh();
	}

	@After
	public void closeContext() {
		context.close();
	}

	@Test
	public void testBatchConfigurationSuccess() throws Exception {
		JobExecution jobExecution = testJob(REMOTE_FILE);

		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());
	}

	@Test
	public void testBatchConfigurationFail() throws Exception {
		JobExecution jobExecution = testJob("missing-people-file.csv");

		assertEquals("Incorrect batch status", BatchStatus.FAILED, jobExecution.getStatus());
	}

	@Test
	public void testBatchDataProcessing() throws Exception {
		JobExecution jobExecution = testJob(REMOTE_FILE);

		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

		JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getBean(DataSource.class));
		List<Map<String, Object>> peopleList = jdbcTemplate.queryForList("select first_name, last_name from people");

		assertEquals("Incorrect number of results", 5, peopleList.size());

		for(Map<String, Object> person : peopleList) {
			assertNotNull("Received null person", person);

			String firstName = (String) person.get("first_name");
			assertEquals("Invalid first name: " + firstName, firstName.toUpperCase(), firstName);

			String lastName = (String) person.get("last_name");
			assertEquals("Invalid last name: " + lastName, lastName.toUpperCase(), lastName);
		}
	}

	private JobExecution testJob(String filePath) throws Exception {
		Job job = context.getBean(Job.class);
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		File localFile = File.createTempFile("local", ".csv");
		localFile.deleteOnExit();

		JobParameters jobParameters = new JobParametersBuilder()
			.addString("localFilePath", localFile.getAbsolutePath())
			.addString("remoteFilePath", filePath)
			.toJobParameters();

		return jobLauncher.run(job, jobParameters);
	}

	@Configuration
	public static class DataSourceConfiguration {
		@Autowired
		private ResourceLoader resourceLoader;

		@PostConstruct
		protected void initialize() {
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			populator.addScript(resourceLoader.getResource(ClassUtils.addResourcePathToPackagePath(Step.class, "schema-hsqldb.sql")));
			populator.addScript(resourceLoader.getResource("classpath:schema-all.sql"));
			populator.setContinueOnError(true);
			DatabasePopulatorUtils.execute(populator, dataSource());
		}

		@Bean
		public DataSource dataSource() {
			return new EmbeddedDatabaseFactory().getDatabase();
		}
	}
}
