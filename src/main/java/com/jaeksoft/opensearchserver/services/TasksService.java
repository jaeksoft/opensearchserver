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

import com.jaeksoft.opensearchserver.model.TaskDefinition;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.ObjectMappers;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class TasksService {

	private final AnnotatedTableService<TaskRecord> tasks;
	private final TaskExecutionService taskExecutionService;

	public TasksService(final TableServiceInterface tableService, final TaskExecutionService taskExecutionService)
			throws NoSuchMethodException, URISyntaxException {
		this.taskExecutionService = taskExecutionService;
		tasks = new AnnotatedTableService<>(tableService, TaskRecord.class);
		tasks.createUpdateTable();
		tasks.createUpdateFields();
	}

	public TaskRecord getTask(final String taskId) {
		return ErrorWrapper.bypass(() -> {
			try {
				return tasks.getRow(Objects.requireNonNull(taskId, "The taskId is null"), TaskRecord.COLUMNS_SET);
			} catch (IOException | ReflectiveOperationException e) {
				throw new InternalServerErrorException("Cannot get task by id", e);
			}
		}, 404);
	}

	private void saveTask(final TaskRecord taskRecord) {
		tasks.upsertRow(taskRecord.taskId, taskRecord);
	}

	private long collectTasks(final TableQuery tableQuery, final int start, final int rows,
			final Collection<TaskRecord> taskRecords) {
		try {
			final TableRequestResultRecords<TaskRecord> result = tasks.queryRows(
					TableRequest.from(start, rows).column(TaskRecord.COLUMNS).query(tableQuery).build());
			if (result == null || result.records == null)
				return 0L;
			if (taskRecords != null)
				taskRecords.addAll(result.records);
			return result.count == null ? 0L : result.count;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException(e);
		}
	}

	public long collectTasksByAccount(final UUID accountId, final int start, final int rows,
			Collection<TaskRecord> taskRecords) {
		return collectTasks(new TableQuery.StringTerm("accountId", accountId.toString()), start, rows, taskRecords);
	}

	public long collectTasksByDefinition(final UUID definitionId, final int start, final int rows,
			Collection<TaskRecord> taskRecords) {
		return collectTasks(new TableQuery.StringTerm("definitionId", definitionId.toString()), start, rows,
				taskRecords);
	}

	public void updateDefinitions(final UUID definitionId,
			final Function<TaskDefinition, TaskDefinition> definitionSupplier) {
		final Collection<TaskRecord> taskRecords = new ArrayList<>();
		collectTasksByDefinition(definitionId, 0, 1000, taskRecords);
		taskRecords.forEach(taskRecord -> {
			try {
				final TaskDefinition oldTaskDefinition =
						ObjectMappers.JSON.readValue(taskRecord.definition, TaskDefinition.class);
				final TaskDefinition newTaskDefinition = definitionSupplier.apply(oldTaskDefinition);
				saveTask(taskRecord.from().definition(newTaskDefinition).build());
			} catch (IOException e) {
				throw new InternalServerErrorException(e);
			}
		});
	}

	public void createTask(final TaskRecord taskRecord) {
		if (getTask(taskRecord.getTaskId()) != null)
			throw new NotAcceptableException("The task already exist");
		saveTask(taskRecord);
		taskExecutionService.checkTaskStatus(taskRecord);
	}

	private TaskRecord checkExistingTask(final String taskId) {
		final TaskRecord taskRecord = getTask(taskId);
		if (taskRecord != null)
			return taskRecord;
		throw new NotFoundException("Task not found: " + taskId);
	}

	public boolean updateStatus(final String taskId, final TaskRecord.Status nextStatus) {
		final TaskRecord oldTask = checkExistingTask(taskId);
		if (oldTask.getStatus() == nextStatus || nextStatus == null)
			return false;
		final TaskRecord newTask = oldTask.from().status(nextStatus).build();
		saveTask(newTask);
		taskExecutionService.checkTaskStatus(newTask);
		return true;
	}

	public void nextSession(final String taskId) {
		final TaskRecord oldTask = checkExistingTask(taskId);
		final TaskRecord newTask = oldTask.from().nextSession().build();
		saveTask(newTask);
	}

	public boolean removeTask(final String taskId) {
		updateStatus(taskId, TaskRecord.Status.PAUSED);
		return tasks.deleteRow(taskId);
	}
}
