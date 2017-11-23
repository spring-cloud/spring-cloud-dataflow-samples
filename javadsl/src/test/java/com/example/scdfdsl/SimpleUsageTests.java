/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.scdfdsl;

import org.junit.Test;
import org.springframework.cloud.dataflow.rest.client.DataFlowOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.dsl.Stream;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamBuilder;
import org.springframework.cloud.dataflow.rest.client.dsl.StreamDefinition;

import java.net.URI;

/**
 * @author Mark Pollack
 */
public class SimpleUsageTests {

	@Test
	public void doNothing() {

	}

	public void doStuff() {
		URI dataFlowUri = URI.create("http://localhost:9393");
		DataFlowOperations dataFlowOperations = new DataFlowTemplate(dataFlowUri);
		StreamDefinition streamDefinition = Stream.builder(dataFlowOperations).name("ticktock").definition("time | log").create();
		Stream stream = streamDefinition.deploy();
	}


}
