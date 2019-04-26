/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.ingest.processor;

import io.spring.cloud.dataflow.ingest.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

/**
 * Processes the providing record, transforming the data into
 * uppercase characters.
 *
 * @author Chris Schaefer
 */
public class PersonItemProcessor implements ItemProcessor<Person, Person> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonItemProcessor.class);

	@Override
	public Person process(Person person) throws Exception {
		String firstName = person.getFirstName().toUpperCase();
		String lastName = person.getLastName().toUpperCase();

		Person processedPerson = new Person(firstName, lastName);

		LOGGER.info("Processed: " + person + " into: " + processedPerson);

		return processedPerson;
	}
}
