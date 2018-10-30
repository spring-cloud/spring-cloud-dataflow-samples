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

import io.spring.cloud.ingest.domain.Person;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * Maps the provided FieldSet into a Person object.
 *
 * @author Chris Schaefer
 */
public class PersonFieldSetMapper implements FieldSetMapper<Person> {
	@Override
	public Person mapFieldSet(FieldSet fieldSet) {
		String firstName = fieldSet.readString(0);
		String lastName = fieldSet.readString(1);

		return new Person(firstName, lastName);
	}
}
