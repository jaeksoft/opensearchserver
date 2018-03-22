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

package com.jaeksoft.opensearchserver.model;

import com.qwazr.database.annotations.Table;
import com.qwazr.database.annotations.TableColumn;
import com.qwazr.database.model.ColumnDefinition;
import com.qwazr.database.model.TableDefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table("tasksExecutionQueues")
public class TaskExecutionRecord {

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	public final String id;

	@TableColumn(name = "accountId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	public final String accountId;

	@TableColumn(name = "taskId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	public final String taskId;

	@TableColumn(name = "nextExecutionTime", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.LONG)
	public final Long nextExecutionTime;

	public TaskExecutionRecord() {
		id = null;
		accountId = null;
		taskId = null;
		nextExecutionTime = null;
	}

	TaskExecutionRecord(Builder builder) {
		accountId = Objects.requireNonNull(builder.accountId, "The accountId is missing").toString();
		taskId = Objects.requireNonNull(builder.taskId, "The taskId is missing");
		id = accountId + '_' + taskId;
		nextExecutionTime = builder.nextExecutionTime;
	}

	public static Builder of(final UUID accountId, final String taskId) {
		return new Builder(accountId, taskId);
	}

	public static class Builder {

		private final UUID accountId;
		private final String taskId;
		private Long nextExecutionTime;

		Builder(UUID accountId, String taskId) {
			this.accountId = accountId;
			this.taskId = taskId;
		}

		public Builder nextExecutiontime(final Long nextExecutionTime) {
			this.nextExecutionTime = nextExecutionTime;
			return this;
		}

		public TaskExecutionRecord build() {
			return new TaskExecutionRecord(this);
		}
	}
}
