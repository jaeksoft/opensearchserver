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
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TasksService {

	private final AnnotatedTableService<TaskRecord> tasks;
	private final Map<String, TaskProcessingService> tasksProcessors;

	public TasksService(final TableServiceInterface tableService,
			final Map<String, TaskProcessingService> tasksProcessors) throws NoSuchMethodException, URISyntaxException {
		this.tasksProcessors = tasksProcessors;
		tasks = new AnnotatedTableService<>(tableService, TaskRecord.class);
		tasks.createUpdateTable();
		tasks.createUpdateFields();
	}

	public TaskProcessingService<?> getTasksProcessor(final TaskRecord taskRecord) {
		return tasksProcessors.getOrDefault(taskRecord.type, TaskProcessingService.DEFAULT);
	}

	public TaskRecord getTask(final String taskId) {
		try {
			return tasks.getRow(Objects.requireNonNull(taskId, "The taskId is null"), TaskRecord.COLUMNS_SET);
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get task by id", e);
		}
	}

	private void saveTask(final TaskRecord taskRecord) {
		tasks.upsertRow(taskRecord.taskId, taskRecord);
	}

	private long collectTasks(final TableQuery tableQuery, final int start, final int rows,
			final Collection<TaskRecord> taskRecords) {
		try {
			final TableRequestResultRecords<TaskRecord> result =
					tasks.queryRows(TableRequest.from(start, rows).query(tableQuery).build());
			if (result == null || result.records == null)
				return 0L;
			if (taskRecords != null)
				taskRecords.addAll(result.records);
			return result.count == null ? 0L : result.count;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException(e);
		}
	}

	public long collectAccountTasks(final UUID accountId, final int start, final int rows,
			Collection<TaskRecord> taskRecords) {
		return collectTasks(new TableQuery.StringTerm("accountId", accountId.toString()), start, rows, taskRecords);
	}

	public long collectCustomTasks(final UUID customId, final int start, final int rows,
			Collection<TaskRecord> taskRecords) {
		return collectTasks(new TableQuery.StringTerm("customId", customId.toString()), start, rows, taskRecords);
	}

	private TaskRecord checkExistingTask(final String taskId) {
		final TaskRecord taskRecord = getTask(taskId);
		if (taskRecord != null)
			return taskRecord;
		throw new NotFoundException("Task not found: " + taskId);
	}

	public boolean updateStatus(final String taskId, final TaskRecord.Status nextStatus) throws IOException {
		final TaskRecord taskRecord = checkExistingTask(taskId);
		if (taskRecord.getStatus() == nextStatus || nextStatus == null)
			return false;
		if (taskRecord.getStatus() == TaskRecord.Status.PAUSED)
			getTasksProcessor(taskRecord).abort(taskId);
		saveTask(taskRecord.from().status(nextStatus).build());
		return true;

	}

}
