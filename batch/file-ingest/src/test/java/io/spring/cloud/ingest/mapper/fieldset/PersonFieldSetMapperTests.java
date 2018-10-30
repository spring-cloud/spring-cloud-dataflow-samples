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

package io.spring.cloud.ingest.mapper.fieldset;

import org.junit.Test;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import io.spring.cloud.ingest.domain.Person;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for PersonFieldSetMapper.
 *
 * @author Chris Schaefer
 */
public class PersonFieldSetMapperTests {
	private static final String[] TOKENS = new String[] { "jane", "doe" };
	private static final String[] NAMES = new String[] { "firstName", "lastName" };

	@Test
	public void testPersonFieldMapping() throws Exception {
		FieldSet fieldSet = new DefaultFieldSet(TOKENS, NAMES);

		FieldSetMapper<Person> fieldSetMapper = new PersonFieldSetMapper();
		Person person = fieldSetMapper.mapFieldSet(fieldSet);

		assertNotNull("Received null Person", person);
		assertNotNull("Received null first name", person.getFirstName());
		assertNotNull("Received null last name", person.getLastName());
		assertEquals("Received wrong first name", TOKENS[0], person.getFirstName());
		assertEquals("Received wrong last name", TOKENS[1], person.getLastName());
	}
}
