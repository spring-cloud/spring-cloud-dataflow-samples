/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.migrateschedule.configuration;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.MigrateScheduleService;
import io.spring.migrateschedule.service.KubernetesMigrateSchedulerService;
import io.spring.migrateschedule.service.TaskDefinitionRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.scheduler.kubernetes.KubernetesScheduler;
import org.springframework.cloud.deployer.spi.scheduler.kubernetes.KubernetesSchedulerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Glenn Renfro
 */
@Configuration
@Profile("kubernetes")
@EntityScan({
		"org.springframework.cloud.dataflow.core"
})
public class KubernetesMigrateScheduleConfiguration {

	@Bean
	public MigrateScheduleService scheduleService(MigrateProperties migrateProperties,
			TaskDefinitionRepository taskDefinitionRepository, MavenProperties mavenProperties) {
		return new KubernetesMigrateSchedulerService(migrateProperties,
				taskDefinitionRepository,
				mavenProperties);
	}
	@Bean
	@ConditionalOnMissingBean
	public KubernetesSchedulerProperties kubernetesSchedulerProperties() {
		return new KubernetesSchedulerProperties();
	}

	@Bean
	public KubernetesScheduler scheduler(KubernetesClient kubernetesClient, KubernetesSchedulerProperties kubernetesSchedulerProperties) {
		return new KubernetesScheduler(kubernetesClient, kubernetesSchedulerProperties);
	}

	@Bean
	@ConfigurationProperties
	public MavenProperties mavenProperties() {
		return new MavenProperties();
	}
}
