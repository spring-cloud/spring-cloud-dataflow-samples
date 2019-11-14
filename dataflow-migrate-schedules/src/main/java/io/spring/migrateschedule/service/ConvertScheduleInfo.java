/*
 * Copyright 2019 the original author or authors.
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

package io.spring.migrateschedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.operations.applications.ApplicationHealthCheck;

import org.springframework.cloud.deployer.spi.scheduler.ScheduleInfo;

/**
 * A child implementation of {@link ScheduleInfo} that adds additional attributes
 * required to migrate to the new schedule format.
 *
 * @author Glenn Renfro
 */
public class ConvertScheduleInfo extends ScheduleInfo implements Comparable{

	private List<String> commandLineArgs = new ArrayList<>();

	private String registeredAppName;

	private Map<String, String> appProperties = new HashMap<>();

	private Integer diskInMB;

	private Integer memoryInMB;

	private String javaBuildPack;

	private ApplicationHealthCheck applicationHealthCheck;

	private String healthCheckEndPoint;

	private Integer maximumConcurrentTasks = 20;

	private boolean useSpringApplicationJson;

	private List<String> services;

	private List<String> domains;

	private List<String> routes;

	private List<String> hosts;

	public List<String> getCommandLineArgs() {
		return commandLineArgs;
	}

	public void setCommandLineArgs(List<String> commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}

	public String getRegisteredAppName() {
		return registeredAppName;
	}

	public void setRegisteredAppName(String registeredAppName) {
		this.registeredAppName = registeredAppName;
	}

	public Map<String, String> getAppProperties() {
		return appProperties;
	}

	public void setAppProperties(Map<String, String> appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof ConvertScheduleInfo) {
			return this.getScheduleName().compareTo(((ConvertScheduleInfo) o).getScheduleName());
		}
		throw new IllegalArgumentException("Can only compare Objects of type ConvertScheduleInfo");
	}

	public Integer getDiskInMB() {
		return diskInMB;
	}

	public void setDiskInMB(Integer diskInMB) {
		this.diskInMB = diskInMB;
	}

	public Integer getMemoryInMB() {
		return memoryInMB;
	}

	public void setMemoryInMB(Integer memoryInMB) {
		this.memoryInMB = memoryInMB;
	}

	public String getJavaBuildPack() {
		return javaBuildPack;
	}

	public void setJavaBuildPack(String javaBuildPack) {
		this.javaBuildPack = javaBuildPack;
	}

	public ApplicationHealthCheck getApplicationHealthCheck() {
		return applicationHealthCheck;
	}

	public void setApplicationHealthCheck(ApplicationHealthCheck applicationHealthCheck) {
		this.applicationHealthCheck = applicationHealthCheck;
	}

	public boolean isUseSpringApplicationJson() {
		return useSpringApplicationJson;
	}

	public void setUseSpringApplicationJson(boolean useSpringApplicationJson) {
		this.useSpringApplicationJson = useSpringApplicationJson;
	}

	public String getHealthCheckEndPoint() {
		return healthCheckEndPoint;
	}

	public void setHealthCheckEndPoint(String healthCheckEndPoint) {
		this.healthCheckEndPoint = healthCheckEndPoint;
	}

	public Integer getMaximumConcurrentTasks() {
		return maximumConcurrentTasks;
	}

	public void setMaximumConcurrentTasks(Integer maximumConcurrentTasks) {
		this.maximumConcurrentTasks = maximumConcurrentTasks;
	}

	public List<String> getServices() {
		return services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public List<String> getRoutes() {
		return routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}
}
