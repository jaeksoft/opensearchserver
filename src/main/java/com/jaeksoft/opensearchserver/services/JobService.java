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

import com.qwazr.utils.LoggerUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobService implements Closeable {

	private final static Logger LOGGER = LoggerUtils.getLogger(JobService.class);

	private final ScheduledExecutorService scheduler;
	private final AccountsService accountsService;
	private final TasksService tasksService;
	private final IndexesService indexesService;

	public JobService(final AccountsService accountsService, final TasksService tasksService,
			final IndexesService indexesService) {
		this.accountsService = accountsService;
		this.tasksService = tasksService;
		this.indexesService = indexesService;
		scheduler = Executors.newScheduledThreadPool(10);
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

	void checkAccountTasks() {
		try {
			LOGGER.info("Check account tasks");
			accountsService.forEachActiveAccount(account -> {
				if (!scheduler.isShutdown())
					tasksService.collectActiveTasks(account.id, 0, account.getCrawlNumberLimit(), task -> {
						if (!scheduler.isShutdown() && task.isStartable())
							tasksService.start(account.id, task.taskId);
					});
			});
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "CheckAccountTasks failed", e);
		}
	}

	public void startAccountTaskRun() {
		scheduler.scheduleWithFixedDelay(this::checkAccountTasks, 0, 1, TimeUnit.MINUTES);
	}

	public void startExpireIndex() {
		scheduler.scheduleWithFixedDelay(indexesService::removeExpired, 0, 1, TimeUnit.MINUTES);
	}

	public void startTasks() {
		startAccountTaskRun();
		startExpireIndex();
	}
}
