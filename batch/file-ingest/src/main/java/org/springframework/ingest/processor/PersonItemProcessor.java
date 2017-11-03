package org.springframework.ingest.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.ingest.domain.Person;

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
