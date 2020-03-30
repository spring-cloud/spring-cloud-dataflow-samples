/*
 * Copyright 2020 the original author or authors.
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
import org.springframework.core.io.Resource;

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

	private Map<String, String> deployerProperties = new HashMap();

	private Integer diskInMB;

	private Integer memoryInMB;

	private String javaBuildPack;

	private ApplicationHealthCheck applicationHealthCheck;

	private String healthCheckEndPoint;

	private boolean useSpringApplicationJson;

	private List<String> services;

	private List<String> domains;

	private List<String> routes;

	private List<String> hosts;

	private Resource taskResource;

	private boolean isCTR;

	private String ctrDSL;

	public List<String> getCommandLineArgs() {
		return commandLineArgs;
	}

	public void setCommandLineArgs(List<String> commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}

	public String getRegisteredAppName() {
		return this.registeredAppName;
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
		return this.diskInMB;
	}

	public void setDiskInMB(Integer diskInMB) {
		this.diskInMB = diskInMB;
	}

	public Integer getMemoryInMB() {
		return this.memoryInMB;
	}

	public void setMemoryInMB(Integer memoryInMB) {
		this.memoryInMB = memoryInMB;
	}

	public String getJavaBuildPack() {
		return this.javaBuildPack;
	}

	public void setJavaBuildPack(String javaBuildPack) {
		this.javaBuildPack = javaBuildPack;
	}

	public ApplicationHealthCheck getApplicationHealthCheck() {
		return this.applicationHealthCheck;
	}

	public void setApplicationHealthCheck(ApplicationHealthCheck applicationHealthCheck) {
		this.applicationHealthCheck = applicationHealthCheck;
	}

	public boolean isUseSpringApplicationJson() {
		return this.useSpringApplicationJson;
	}

	public void setUseSpringApplicationJson(boolean useSpringApplicationJson) {
		this.useSpringApplicationJson = useSpringApplicationJson;
	}

	public String getHealthCheckEndPoint() {
		return this.healthCheckEndPoint;
	}

	public void setHealthCheckEndPoint(String healthCheckEndPoint) {
		this.healthCheckEndPoint = healthCheckEndPoint;
	}

	public List<String> getServices() {
		return this.services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public List<String> getDomains() {
		return this.domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public List<String> getRoutes() {
		return this.routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}

	public List<String> getHosts() {
		return this.hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public Resource getTaskResource() {
		return this.taskResource;
	}

	public void setTaskResource(Resource taskResource) {
		this.taskResource = taskResource;
	}

	public boolean isCTR() {
		return this.isCTR;
	}

	public void setCTR(boolean CTR) {
		isCTR = CTR;
	}

	public String getCtrDSL() {
		return this.ctrDSL;
	}

	public void setCtrDSL(String ctrDSL) {
		this.ctrDSL = ctrDSL;
	}

	public Map<String, String> getDeployerProperties() {
		return deployerProperties;
	}

	public void setDeployerProperties(Map<String, String> deployerProperties) {
		this.deployerProperties = deployerProperties;
	}
}
