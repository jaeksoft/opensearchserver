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
import com.qwazr.utils.concurrent.ConsumerEx;
import com.qwazr.utils.concurrent.ReadWriteLock;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.NoContentException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TasksService extends StoreService<TaskRecord> {

	private final ReadWriteLock rwl = ReadWriteLock.reentrant(true);

	private final static String TASKS_DIRECTORY = "tasks";

	private final static String ACTIVE_DIRECTORY = "active";
	private final static String ARCHIVE_DIRECTORY = "archived";

	private final Map<Class<? extends TaskRecord>, ProcessingService> tasksProcessors;

	public TasksService(final StoreServiceInterface storeService,
			final Map<Class<? extends TaskRecord>, ProcessingService> tasksProcessors) {
		super(storeService, TASKS_DIRECTORY, TaskRecord.class);
		this.tasksProcessors = tasksProcessors;
	}

	private ProcessingService<?, ?> getTasksProcessor(final TaskRecord taskRecord) {
		return tasksProcessors.getOrDefault(taskRecord.getClass(), ProcessingService.DEFAULT);
	}

	public void saveActiveTask(final String storeSchema, final TaskRecord taskRecord) throws IOException {
		rwl.writeEx(() -> {
			if (super.read(storeSchema, ARCHIVE_DIRECTORY, taskRecord.getTaskId()) != null)
				throw new NotAllowedException("The task has already been archived");
			super.save(storeSchema, ACTIVE_DIRECTORY, taskRecord);
			if (taskRecord.getStatus() != TaskRecord.Status.PAUSED)
				getTasksProcessor(taskRecord).checkIsRunning(storeSchema, taskRecord);
		});
	}

	public synchronized void archiveActiveTask(final String storeSchema, final String taskId) throws IOException {
		rwl.writeEx(() -> {
			final TaskRecord taskRecord = super.read(storeSchema, ACTIVE_DIRECTORY, taskId);
			if (taskRecord == null)
				throw new NoContentException("Task not found");
			if (getTasksProcessor(taskRecord).isRunning(taskId))
				throw new NotAllowedException(
						"Can't archive the task because it is running. It should be stopped first.");
			super.save(storeSchema, ARCHIVE_DIRECTORY, taskRecord);
			super.remove(storeSchema, ACTIVE_DIRECTORY, taskId);
		});
	}

	public TaskRecord getActiveTask(final String storeSchema, final String taskId) throws IOException {
		return rwl.readEx(() -> super.read(storeSchema, ACTIVE_DIRECTORY, taskId));
	}

	public int collectActiveTasks(final String storeSchema, final int start, final int rows,
			final ConsumerEx<TaskRecord, IOException> recordConsumer) throws IOException {
		return rwl.readEx(() -> super.collect(storeSchema, ACTIVE_DIRECTORY, start, rows, null, recordConsumer));
	}

	public int collectActiveTasks(final String storeSchema, final int start, final int rows, final UUID crawlUuid,
			final ConsumerEx<TaskRecord, IOException> recordConsumer) throws IOException {
		final String crawlUuidString = crawlUuid.toString();
		return rwl.readEx(() -> super.collect(storeSchema, ACTIVE_DIRECTORY, start, rows,
				name -> name.startsWith(crawlUuidString), recordConsumer));
	}

	public TaskRecord getArchivedTask(final String storeSchema, final String taskId) throws IOException {
		return rwl.readEx(() -> super.read(storeSchema, ARCHIVE_DIRECTORY, taskId));
	}

	public int getArchivedTasks(final String storeSchema, final int start, final int rows,
			final ConsumerEx<TaskRecord, IOException> recordConsumer) throws IOException {
		return rwl.readEx(() -> super.collect(storeSchema, ARCHIVE_DIRECTORY, start, rows, null, recordConsumer));
	}

	@Override
	protected String getStoreName(final TaskRecord record) {
		return record.getTaskId();
	}

	private TaskRecord checkExistingTaskRecord(final String storeSchema, final String taskId) throws IOException {
		final TaskRecord taskRecord = getActiveTask(storeSchema, taskId);
		if (taskRecord != null)
			return taskRecord;
		throw new NotFoundException("Task not found: " + taskId);
	}

	public void pause(final String storeSchema, final String taskId) throws IOException {
		final TaskRecord taskRecord = checkExistingTaskRecord(storeSchema, taskId);
		if (!taskRecord.isPausable())
			throw new NotAcceptableException("This task cannot be paused: " + taskId);
		getTasksProcessor(taskRecord).abort(taskId);
		saveActiveTask(storeSchema, taskRecord.from().status(TaskRecord.Status.PAUSED).build());
	}

	public void start(final String storeSchema, final String taskId) throws IOException {
		final TaskRecord taskRecord = checkExistingTaskRecord(storeSchema, taskId);
		if (!taskRecord.isStartable())
			throw new NotAcceptableException("This task cannot be started: " + taskId);
		final TaskRecord newTaskRecord = taskRecord.from().status(TaskRecord.Status.STARTED).build();
		saveActiveTask(storeSchema, newTaskRecord);
		getTasksProcessor(taskRecord).checkIsRunning(storeSchema, newTaskRecord);
	}
}
