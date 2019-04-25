/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.billrun.model;

public class Usage {

	private Long id;

	private String firstName;

	private String lastName;

	private Long minutes;

	private Long dataUsage;

	public Usage() {
	}

	public Usage(Long id, String firstName, String lastName, Long minutes, Long dataUsage) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.minutes = minutes;
		this.dataUsage = dataUsage;
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getDataUsage() {
		return dataUsage;
	}

	public void setDataUsage(Long dataUsage) {
		this.dataUsage = dataUsage;
	}

	public Long getMinutes() {
		return minutes;
	}

	public void setMinutes(Long minutes) {
		this.minutes = minutes;
	}

	@Override
	public String toString() {
		return "Usage{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", minutes=" + minutes +
				", dataUsage=" + dataUsage +
				'}';
	}
}
