/*
 * Copyright 2020 the original author or authors.
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
import io.spring.migrateschedule.service.AppRegistrationRepository;
import io.spring.migrateschedule.service.KubernetesMigrateSchedulerService;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.TaskDefinitionRepository;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesClientFactory;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesDeployerProperties;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesScheduler;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesSchedulerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * @author Glenn Renfro
 */
@Configuration
@EntityScan({
		"org.springframework.cloud.dataflow.core"
})
@Profile("kubernetes")
public class KubernetesMigrateScheduleConfiguration {


	@Bean
	@Primary
	public KubernetesSchedulerProperties kubernetesSchedulerProperties() {
		return new KubernetesSchedulerProperties();
	}


	@Bean
	public KubernetesClient kubernetesClient(KubernetesDeployerProperties kubernetesDeployerProperties) {
		return KubernetesClientFactory.getKubernetesClient(kubernetesDeployerProperties);
	}

	@Bean
	public KubernetesScheduler kubernetesScheduler(KubernetesClient kubernetesClient, KubernetesSchedulerProperties kubernetesSchedulerProperties) {
		return new KubernetesScheduler( kubernetesClient, kubernetesSchedulerProperties);
	}

	@Bean
	public KubernetesMigrateSchedulerService scheduleService( TaskDefinitionRepository taskDefinitionRepository, AppRegistrationRepository appRegistrationRepository, KubernetesClient kubernetesClient, MigrateProperties migrateProperties) {
		return new KubernetesMigrateSchedulerService(taskDefinitionRepository, appRegistrationRepository, kubernetesClient, migrateProperties);
	}

}
