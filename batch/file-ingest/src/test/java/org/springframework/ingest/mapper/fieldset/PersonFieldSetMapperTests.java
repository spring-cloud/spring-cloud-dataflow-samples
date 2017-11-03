package org.springframework.ingest.mapper.fieldset;

import org.junit.Test;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import org.springframework.ingest.domain.Person;

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
