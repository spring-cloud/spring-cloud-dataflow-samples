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

package io.spring.migrateschedule.service;

import java.util.List;

import org.springframework.cloud.dataflow.core.AppRegistration;
import org.springframework.cloud.dataflow.core.ApplicationType;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for retrieving instances of the the {@link AppRegistration} class.
 * @author Glenn Renfro
 */
@Transactional
public interface AppRegistrationRepository extends KeyValueRepository<AppRegistration, Long> {

	AppRegistration findAppRegistrationByNameAndTypeAndDefaultVersionIsTrue(String name, ApplicationType type);

}
