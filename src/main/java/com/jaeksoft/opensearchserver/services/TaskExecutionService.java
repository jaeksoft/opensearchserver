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
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TaskExecutionService {

	private final AnnotatedTableService<TaskExecutionRecord> taskExecutions;

	public TaskExecutionService(final TableServiceInterface tableServiceInterface)
			throws NoSuchMethodException, URISyntaxException {
		taskExecutions = new AnnotatedTableService<>(tableServiceInterface, TaskExecutionRecord.class);
		taskExecutions.createUpdateTable();
		taskExecutions.createUpdateFields();
	}

	public List<TaskExecutionRecord> nextTaskExecutions(UUID accountId, int count)
			throws IOException, ReflectiveOperationException {
		final TableRequestResultRecords<TaskExecutionRecord> results = taskExecutions.queryRows(
				TableRequest.from(0, 1000)
						.column(TaskExecutionRecord.COLUMNS)
						.query(new TableQuery.StringTerm("accountId", accountId.toString()))
						.build());
		if (results == null || results.records == null || results.records.isEmpty())
			return null;

		results.records.sort(Comparator.comparingLong(r -> r.nextExecutionTime));
		if (results.records.size() <= count)
			return results.records;
		return results.records.subList(0, count);
	}

	public void addTaskExecution(final UUID accountId, final UUID taskId) {
		final TaskExecutionRecord taskExecutionRecord =
				TaskExecutionRecord.of(accountId, taskId).nextExecutiontime(System.currentTimeMillis()).build();
		taskExecutions.upsertRow(taskExecutionRecord.id, taskExecutionRecord);
	}

	public boolean removeTaskExecution(final UUID accountId, final UUID taskId) {
		return taskExecutions.deleteRow(TaskExecutionRecord.of(accountId, taskId).build().id);
	}

}
