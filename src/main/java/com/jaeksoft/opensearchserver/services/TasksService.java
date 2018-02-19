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

import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.utils.concurrent.ReadWriteLock;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.NoContentException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TasksService extends StoreService<TaskRecord> {

	private final ReadWriteLock rwl = ReadWriteLock.stamped();

	private final static String TASKS_DIRECTORY = "tasks";

	private final static String ACTIVE_DIRECTORY = "active";
	private final static String ARCHIVE_DIRECTORY = "archived";

	private final Map<Class<? extends TaskRecord>, ProcessingService> tasksProcessors;

	public TasksService(final StoreServiceInterface storeService, final String storeSchema,
			final Map<Class<? extends TaskRecord>, ProcessingService> tasksProcessors) {
		super(storeService, storeSchema, TASKS_DIRECTORY, TaskRecord.class);
		this.tasksProcessors = tasksProcessors;
	}

	private ProcessingService<?, ?> getTasksExecutor(final TaskRecord taskRecord) {
		return tasksProcessors.getOrDefault(taskRecord.getClass(), ProcessingService.DEFAULT);
	}

	public void saveActiveTask(final TaskRecord taskRecord) throws IOException {
		rwl.writeEx(() -> {
			if (super.read(ARCHIVE_DIRECTORY, taskRecord.getTaskId()) != null)
				throw new NotAllowedException("The task has already been archived");
			super.save(ACTIVE_DIRECTORY, taskRecord);
			getTasksExecutor(taskRecord).checkIsRunning(taskRecord);
		});
	}

	public synchronized void archiveActiveTask(final String taskId) throws IOException {
		rwl.writeEx(() -> {
			final TaskRecord taskRecord = super.read(ACTIVE_DIRECTORY, taskId);
			if (taskRecord == null)
				throw new NoContentException("Task not found");
			if (getTasksExecutor(taskRecord).isRunning(taskId))
				throw new NotAllowedException(
						"Can't archive the task because it is running. It should be stopped first.");
			super.save(ARCHIVE_DIRECTORY, taskRecord);
			super.remove(ACTIVE_DIRECTORY, taskId);
		});
	}

	public TaskRecord getActiveTask(final String taskId) throws IOException {
		return rwl.readEx(() -> super.read(ACTIVE_DIRECTORY, taskId));
	}

	public int collectActiveTasks(final int start, final int rows, final Consumer<TaskRecord> recordConsumer)
			throws IOException {
		return rwl.readEx(() -> super.collect(ACTIVE_DIRECTORY, start, rows, null, recordConsumer));
	}

	public int collectActiveTasks(final int start, final int rows, final UUID crawlUuid,
			final Consumer<TaskRecord> recordConsumer) throws IOException {
		final String crawlUuidString = crawlUuid.toString();
		return rwl.readEx(() -> super.collect(ACTIVE_DIRECTORY, start, rows, name -> name.startsWith(crawlUuidString),
				recordConsumer));
	}

	public TaskRecord getArchivedTask(final String taskId) throws IOException {
		return rwl.readEx(() -> super.read(ARCHIVE_DIRECTORY, taskId));
	}

	public int getArchivedTasks(final int start, final int rows, final Consumer<TaskRecord> recordConsumer)
			throws IOException {
		return rwl.readEx(() -> super.collect(ARCHIVE_DIRECTORY, start, rows, null, recordConsumer));
	}

	@Override
	protected String getStoreName(final TaskRecord record) {
		return record.getTaskId();
	}

}
