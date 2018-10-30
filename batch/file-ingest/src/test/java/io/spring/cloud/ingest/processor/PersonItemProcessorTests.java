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

package io.spring.cloud.ingest.processor;

import io.spring.cloud.ingest.domain.Person;
import org.junit.Test;

import org.springframework.batch.item.ItemProcessor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for PersonItemProcessor.
 *
 * @author Chris Schaefer
 */
public class PersonItemProcessorTests {
	private static final String FIRST_NAME = "jane";
	private static final String LAST_NAME = "doe";

	@Test
	public void testPersonProcessing() throws Exception {
		Person person = new Person(FIRST_NAME, LAST_NAME);

		ItemProcessor<Person, Person> personItemProcessor = new PersonItemProcessor();
		Person transformedPerson = personItemProcessor.process(person);

		assertNotNull("Received null Person", transformedPerson);
		assertNotNull("Received null first name", transformedPerson.getFirstName());
		assertNotNull("Received null last name", transformedPerson.getLastName());
		assertEquals("Invalid first name processing, should be uppercase",
					 person.getFirstName().toUpperCase(), transformedPerson.getFirstName());
		assertEquals("Invalid last name processing, should be uppercase",
					 person.getLastName().toUpperCase(), transformedPerson.getLastName());
	}
}
