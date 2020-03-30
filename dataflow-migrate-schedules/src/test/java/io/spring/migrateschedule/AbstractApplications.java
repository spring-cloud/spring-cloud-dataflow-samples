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

package io.spring.migrateschedule;

import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.operations.applications.ApplicationEvent;
import org.cloudfoundry.operations.applications.ApplicationSshEnabledRequest;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.Applications;
import org.cloudfoundry.operations.applications.CopySourceApplicationRequest;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.DisableApplicationSshRequest;
import org.cloudfoundry.operations.applications.EnableApplicationSshRequest;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.ListApplicationTasksRequest;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.operations.applications.PushApplicationManifestRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.RenameApplicationRequest;
import org.cloudfoundry.operations.applications.RestageApplicationRequest;
import org.cloudfoundry.operations.applications.RestartApplicationInstanceRequest;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.applications.RunApplicationTaskRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.cloudfoundry.operations.applications.SetApplicationHealthCheckRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.cloudfoundry.operations.applications.Task;
import org.cloudfoundry.operations.applications.TerminateApplicationTaskRequest;
import org.cloudfoundry.operations.applications.UnsetEnvironmentVariableApplicationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractApplications implements Applications {
	@Override
	public Mono<Void> copySource(CopySourceApplicationRequest copySourceApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> delete(DeleteApplicationRequest deleteApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> disableSsh(DisableApplicationSshRequest disableApplicationSshRequest) {
		return null;
	}

	@Override
	public Mono<Void> enableSsh(EnableApplicationSshRequest enableApplicationSshRequest) {
		return null;
	}

	@Override
	public Flux<ApplicationEvent> getEvents(GetApplicationEventsRequest getApplicationEventsRequest) {
		return null;
	}

	@Override
	public Flux<ApplicationSummary> list() {
		return null;
	}

	@Override
	public Flux<Task> listTasks(ListApplicationTasksRequest listApplicationTasksRequest) {
		return null;
	}

	@Override
	public Flux<LogMessage> logs(LogsRequest logsRequest) {
		return null;
	}

	@Override
	public Mono<Void> push(PushApplicationRequest pushApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> pushManifest(PushApplicationManifestRequest pushApplicationManifestRequest) {
		return null;
	}

	@Override
	public Mono<Void> rename(RenameApplicationRequest renameApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> restage(RestageApplicationRequest restageApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> restart(RestartApplicationRequest restartApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> restartInstance(RestartApplicationInstanceRequest restartApplicationInstanceRequest) {
		return null;
	}

	@Override
	public Mono<Task> runTask(RunApplicationTaskRequest runApplicationTaskRequest) {
		return null;
	}

	@Override
	public Mono<Void> terminateTask(TerminateApplicationTaskRequest terminateApplicationTaskRequest) {
		return null;
	}

	@Override
	public Mono<Void> scale(ScaleApplicationRequest scaleApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> setEnvironmentVariable(SetEnvironmentVariableApplicationRequest setEnvironmentVariableApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> setHealthCheck(SetApplicationHealthCheckRequest setApplicationHealthCheckRequest) {
		return null;
	}

	@Override
	public Mono<Boolean> sshEnabled(ApplicationSshEnabledRequest applicationSshEnabledRequest) {
		return null;
	}

	@Override
	public Mono<Void> start(StartApplicationRequest startApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> stop(StopApplicationRequest stopApplicationRequest) {
		return null;
	}

	@Override
	public Mono<Void> unsetEnvironmentVariable(UnsetEnvironmentVariableApplicationRequest unsetEnvironmentVariableApplicationRequest) {
		return null;
	}
}
