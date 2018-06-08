package org.springframework.ingest.processor;

import org.junit.Test;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.ingest.domain.Person;

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
