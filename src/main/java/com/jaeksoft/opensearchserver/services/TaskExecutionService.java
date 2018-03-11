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

import com.jaeksoft.opensearchserver.model.TaskExecutionRecord;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskExecutionService {

	private final AnnotatedTableService<TaskExecutionRecord> taskExecutions;
	private final Map<String, TaskProcessor> tasksProcessors;

	public TaskExecutionService(final TableServiceInterface tableServiceInterface,
			final Map<String, TaskProcessor> tasksProcessors) throws NoSuchMethodException, URISyntaxException {
		this.tasksProcessors = tasksProcessors;
		taskExecutions = new AnnotatedTableService<>(tableServiceInterface, TaskExecutionRecord.class);
		taskExecutions.createUpdateTable();
		taskExecutions.createUpdateFields();
	}

	TaskProcessor<?> getTasksProcessor(final String taskType) {
		return tasksProcessors.getOrDefault(taskType, TaskProcessor.DEFAULT);
	}

	List<TaskExecutionRecord> getNextTaskExecutions(final UUID accountId) {
		final TableRequestResultRecords<TaskExecutionRecord> results;
		try {
			results = taskExecutions.queryRows(TableRequest.from(0, 1000)
					.column(TaskExecutionRecord.COLUMNS)
					.query(new TableQuery.StringTerm("accountId", accountId.toString()))
					.build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException(e);
		}
		if (results == null || results.records == null || results.records.isEmpty())
			return null;
		final List<TaskExecutionRecord> sortableRecords = new ArrayList<>(results.records);
		sortableRecords.sort(Comparator.comparingLong(r -> r.nextExecutionTime));
		return sortableRecords;
	}

	void upsertTaskExecution(final TaskRecord taskRecord) {
		final TaskExecutionRecord taskExecutionRecord =
				TaskExecutionRecord.of(taskRecord.getAccountId(), taskRecord.getTaskId())
						.nextExecutiontime(System.currentTimeMillis())
						.build();
		taskExecutions.upsertRow(taskExecutionRecord.id, taskExecutionRecord);
	}

	boolean removeTaskExecution(final UUID accountId, final String taskId) {
		return taskExecutions.deleteRow(TaskExecutionRecord.of(accountId, taskId).build().id);
	}

	public boolean checkTaskStatus(final TaskRecord taskRecord) {
		switch (taskRecord.getStatus()) {
		case PAUSED:
			getTasksProcessor(taskRecord.type).abort(taskRecord.taskId);
			return removeTaskExecution(taskRecord.getAccountId(), taskRecord.taskId);
		case ACTIVE:
			upsertTaskExecution(taskRecord);
			return true;
		}
		return false;
	}
}
