/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.TaskExecutionRecord;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.utils.LoggerUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobService implements Closeable {

	private final static Logger LOGGER = LoggerUtils.getLogger(JobService.class);

	private final ConfigService configService;
	private final ScheduledExecutorService scheduler;
	private final AccountsService accountsService;
	private final TaskExecutionService taskExecutionService;
	private final TasksService tasksService;
	private final IndexesService indexesService;

	public JobService(final ConfigService configService, final AccountsService accountsService,
			final TasksService tasksService, final IndexesService indexesService,
			final TaskExecutionService taskExecutionService) {
		this.configService = configService;
		this.accountsService = accountsService;
		this.tasksService = tasksService;
		this.indexesService = indexesService;
		this.taskExecutionService = taskExecutionService;
		scheduler = Executors.newScheduledThreadPool(2);
	}

	@Override
	public void close() throws IOException {
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	private void runSessions(final AccountRecord account) {
		int taskLeft = account.getTasksNumberLimit();
		final List<TaskExecutionRecord> taskExecutions = taskExecutionService.getNextTaskExecutions(account.getId());
		if (taskExecutions == null)
			return;
		for (TaskExecutionRecord taskExecution : taskExecutions) {
			if (taskLeft <= 0)
				return;
			if (taskExecution.nextExecutionTime > System.currentTimeMillis())
				return;
			// Non active tasks can be removed from the queue
			final TaskRecord taskRecord = tasksService.getTask(taskExecution.taskId);
			if (taskRecord == null || taskRecord.getStatus() != TaskRecord.Status.ACTIVE) {
				taskExecutionService.removeTaskExecution(account.getId(), taskExecution.taskId);
				continue;
			}
			taskLeft--;
			// Is the task already running ?
			final TaskProcessor<?> taskProcessor = taskExecutionService.getTasksProcessor(taskRecord.type);
			if (taskProcessor.isRunning(taskExecution.taskId))
				continue;
			// Let's start the next run
			taskExecutionService.upsertTaskExecution(taskRecord);
			try {
				if (taskProcessor.runSession(taskRecord) == TaskRecord.Status.DONE) {
					// If the task was done, we can plan a new run with a new SessionTimeId
					tasksService.nextSession(taskRecord.taskId);
				}
			} catch (Exception e) {
				// If any error occured, let's update the status
				tasksService.updateStatus(taskRecord.taskId, TaskRecord.Status.ERROR);
			}
		}

	}

	void checkAccountsTasks() {
		try {
			LOGGER.info("Check account tasks");
			final int count = accountsService.forEachActiveAccount(account -> {
				if (scheduler.isShutdown())
					return;
				runSessions(account);
			});
			LOGGER.info(count + " tasks checked");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "CheckAccountTasks failed", e);
		}
	}

	void startAccountTaskRun() {
		scheduler.scheduleWithFixedDelay(this::checkAccountsTasks, 0, configService.getJobCrawlPeriodSeconds(),
				TimeUnit.SECONDS);
	}

	void startExpireIndex() {
		scheduler.scheduleWithFixedDelay(indexesService::removeExpired, 0, 1, TimeUnit.MINUTES);
	}

	public void startTasks() {
		startAccountTaskRun();
		startExpireIndex();
	}
}
