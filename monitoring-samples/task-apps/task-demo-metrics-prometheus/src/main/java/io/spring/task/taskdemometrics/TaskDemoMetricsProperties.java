/*
 * Copyright 2021 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.spring.task.taskdemometrics;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Tzolov
 */
@ConfigurationProperties(prefix = "task.demo")
public class TaskDemoMetricsProperties {

	private int range = 100;
	private Delay delay = new Delay(Duration.ofMinutes(1), Duration.ofSeconds(10));

	public static class Delay {

		private Duration fixed = Duration.ofSeconds(0);
		private Duration random = Duration.ofSeconds(1);

		public Delay(Duration fixed, Duration random) {
			this.fixed = fixed;
			this.random = random;
		}

		public Duration getFixed() {
			return fixed;
		}

		public void setFixed(Duration fixed) {
			this.fixed = fixed;
		}

		public Duration getRandom() {
			return random;
		}

		public void setRandom(Duration random) {
			this.random = random;
		}
	}

	public int getRange() {
		return range;
	}

	public Delay getDelay() {
		return delay;
	}
}
