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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.database.annotations.Table;
import com.qwazr.database.annotations.TableColumn;
import com.qwazr.database.model.ColumnDefinition;
import com.qwazr.database.model.TableDefinition;
import com.qwazr.utils.ObjectMappers;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table("tasks")
public class TaskRecord {

	public enum Status {

		PAUSED(0, true, false, true),
		ACTIVE(1, false, true, false),
		ERROR(9, true, false, true),
		DONE(10, true, false, true);

		private int code;
		private boolean activable;
		private boolean pausable;
		private boolean removable;

		Status(int code, boolean activable, boolean pausable, boolean removable) {
			this.code = code;
			this.activable = activable;
			this.pausable = pausable;
			this.removable = removable;
		}

		public static Status find(Integer statusCode) {
			if (statusCode == null)
				return null;
			for (Status status : values())
				if (status.code == statusCode)
					return status;
			return null;
		}

		public boolean isActivable() {
			return activable;
		}

		public boolean isPausable() {
			return pausable;
		}

		public boolean isRemovable() {
			return removable;
		}

	}

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	public final String taskId;

	@TableColumn(name = "accountId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	public final String accountId;

	@TableColumn(name = "definition", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	public final String definition;

	@TableColumn(name = "type", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	public final String type;

	@TableColumn(name = "definitionId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	public final String definitionId;

	@TableColumn(name = "sessionTimeId", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.LONG)
	public final Long sessionTimeId;

	@TableColumn(name = "statusCode", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.INTEGER)
	public final Integer statusCode;

	@TableColumn(name = "statusTime", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.LONG)
	public final Long statusTime;

	public static final String[] COLUMNS = new String[] { TableDefinition.ID_COLUMN_NAME,
			"accountId",
			"definition",
			"type",
			"definitionId",
			"sessionTimeId",
			"statusCode",
			"statusTime" };

	public static final Set<String> COLUMNS_SET = new HashSet<>(Arrays.asList(COLUMNS));

	private volatile UUID accountUuid;
	private volatile UUID definitionUuid;
	private volatile TaskDefinition taskDefinition;
	private volatile Status status;

	public TaskRecord() {
		taskId = accountId = definition = type = definitionId = null;
		sessionTimeId = statusTime = null;
		statusCode = null;
	}

	protected TaskRecord(final Builder builder) throws JsonProcessingException {
		Objects.requireNonNull(builder.definition, "The task definition is missing");
		taskId = Objects.requireNonNull(builder.definition.getTaskId(), "The taskId is missing");
		accountId = Objects.requireNonNull(builder.accountId, "The accountId is missing").toString();
		definition = ObjectMappers.JSON.writeValueAsString(builder.definition);
		definitionId = Objects.requireNonNull(builder.definition.getId(), "The definitionId is missing").toString();
		type = builder.definition.type;
		sessionTimeId = Objects.requireNonNull(builder.sessionTimeId, "The sessionTimeId is missing");
		statusCode = Objects.requireNonNull(builder.status, "The status is missing").code;
		statusTime = Objects.requireNonNull(builder.statusTime, "The status time is missing");
	}

	public String getTaskId() {
		return taskId;
	}

	public synchronized UUID getAccountId() {
		if (accountUuid == null && accountId != null)
			accountUuid = UUID.fromString(accountId);
		return accountUuid;
	}

	public synchronized TaskDefinition getDefinition() {
		if (taskDefinition == null && definition != null) {
			try {
				taskDefinition = ObjectMappers.JSON.readValue(definition, TaskDefinition.class);
			} catch (IOException e) {
				throw new InternalServerErrorException(e);
			}
		}
		return taskDefinition;
	}

	public String getType() {
		return type;
	}

	public synchronized UUID getDefinitionId() {
		if (definitionUuid == null && definitionId != null)
			definitionUuid = UUID.fromString(definitionId);
		return accountUuid;
	}

	public Long getSessionTimeId() {
		return sessionTimeId;
	}

	public synchronized Status getStatus() {
		if (status == null && statusCode != null)
			status = Status.find(statusCode);
		return status;
	}

	public Long getStatusTime() {
		return statusTime;
	}

	public Builder from() {
		return new Builder(this);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof TaskRecord))
			return false;
		if (o == this)
			return true;
		final TaskRecord r = (TaskRecord) o;
		return Objects.equals(taskId, r.taskId) && Objects.equals(accountId, r.accountId) &&
				Objects.equals(definition, r.definition) && Objects.equals(type, r.type) &&
				Objects.equals(definitionId, r.definitionId) && Objects.equals(sessionTimeId, r.sessionTimeId) &&
				Objects.equals(statusCode, r.statusCode) && Objects.equals(statusTime, r.statusTime);
	}

	public static Builder of(final UUID accountId) {
		return new Builder(accountId);
	}

	public static class Builder {

		private final UUID accountId;

		private TaskDefinition definition;
		private Long sessionTimeId;
		private Status status;
		private Long statusTime;

		private Builder(final UUID accountId) {
			this.accountId = accountId;
			this.sessionTimeId = System.currentTimeMillis();
		}

		private Builder(final TaskRecord taskRecord) {
			this.accountId = taskRecord.getAccountId();
			this.definition = taskRecord.getDefinition();
			this.sessionTimeId = taskRecord.getSessionTimeId();
			this.status = taskRecord.getStatus();
			this.statusTime = taskRecord.getStatusTime();
		}

		public Builder definition(final TaskDefinition definition) {
			this.definition = definition;
			return this;
		}

		public Builder status(Status status) {
			this.status = status;
			this.statusTime = System.currentTimeMillis();
			return this;
		}

		public Builder nextSession() {
			this.sessionTimeId = System.currentTimeMillis();
			return this;
		}

		public TaskRecord build() {
			try {
				return new TaskRecord(this);
			} catch (JsonProcessingException e) {
				throw new InternalServerErrorException(e);
			}
		}
	}

}