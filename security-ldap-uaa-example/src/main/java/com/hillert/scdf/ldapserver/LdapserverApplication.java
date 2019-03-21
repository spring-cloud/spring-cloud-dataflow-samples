/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hillert.scdf.ldapserver;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
public class LdapserverApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(LdapserverApplication.class, args);
	}

	@Bean
	public ApacheDSContainer apacheDSContainer() throws Exception {
		final File temporaryFolder = Files.createTempDirectory("ldap_server").toFile();
		final String ldapFileName = "testUsers.ldif";

		ApacheDSContainer apacheDSContainer = new ApacheDSContainer("dc=springframework,dc=org",
				"classpath:" + ldapFileName);

		apacheDSContainer.setPort(40000);
		final File workingDir = new File(temporaryFolder, UUID.randomUUID().toString());
		apacheDSContainer.setWorkingDirectory(workingDir);
		return apacheDSContainer;
	}
}
