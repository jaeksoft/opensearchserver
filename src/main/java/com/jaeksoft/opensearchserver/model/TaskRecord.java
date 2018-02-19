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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE,
		creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(WebCrawlTaskRecord.class) })
public abstract class TaskRecord {

	public enum Status {
		STARTED, ERROR, DONE, PAUSED
	}

	public final String taskId;
	public final Long creationTime;
	public final Status status;
	public final Long statusTime;

	protected TaskRecord(final String taskId, final Long creationTime, final Status status, final Long statusTime) {
		this.taskId = taskId;
		this.creationTime = creationTime;
		this.status = status;
		this.statusTime = statusTime;
	}

	protected TaskRecord(TaskBuilder builder) {
		this(builder.taskId, builder.creationTime, builder.status, builder.statusTime);
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public String getTaskId() {
		return taskId;
	}

	public abstract String getType();

	public Status getStatus() {
		return status;
	}

	public Long getStatusTime() {
		return statusTime;
	}

	public boolean isPausable() {
		return status == null || status == Status.STARTED;
	}

	public boolean isStartable() {
		return status == Status.PAUSED;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof TaskRecord))
			return false;
		if (o == this)
			return true;
		final TaskRecord r = (TaskRecord) o;
		return Objects.equals(taskId, r.taskId) && Objects.equals(creationTime, r.creationTime) &&
				Objects.equals(status, r.status) && Objects.equals(statusTime, r.statusTime);
	}

	public static abstract class TaskBuilder<R, B extends TaskBuilder<R, ?>> {

		private final Class<B> builderClass;

		private final String taskId;
		private final Long creationTime;

		private Status status;
		private Long statusTime;

		/**
		 * Constructor to use when updating TaskRecord
		 *
		 * @param taskRecord
		 */
		protected TaskBuilder(final Class<B> builderClass, TaskRecord taskRecord) {
			this.builderClass = builderClass;
			taskId = taskRecord.getTaskId();
			creationTime = taskRecord.getCreationTime();
			status = taskRecord.getStatus();
			statusTime = taskRecord.getStatusTime();
		}

		/**
		 * Constructor to use on a new TaskRecord
		 *
		 * @param builderClass
		 * @param taskId
		 */
		protected TaskBuilder(final Class<B> builderClass, final String taskId) {
			this.builderClass = builderClass;
			this.taskId = taskId;
			this.creationTime = System.currentTimeMillis();
		}

		public B status(final Status status) {
			this.status = status;
			this.statusTime = System.currentTimeMillis();
			return me();
		}

		protected B me() {
			return builderClass.cast(this);
		}

		public abstract R build();
	}
}