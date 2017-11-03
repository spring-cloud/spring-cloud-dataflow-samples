package org.springframework.ingest.domain;

/**
 * Domain object representing data about a Person.
 *
 * @author Chris Schaefer
 */
public class Person {
	private final String firstName;
	private final String lastName;

	public Person(final String firstName, final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@Override
	public String toString() {
		return "First name: " + firstName + " , last name: " + lastName;
	}
}
