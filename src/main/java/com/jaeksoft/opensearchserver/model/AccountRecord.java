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
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.lang3.CharUtils;

import javax.ws.rs.NotAcceptableException;
import java.util.Objects;
import java.util.UUID;

@Table("accounts")
public class AccountRecord {

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	public final String id;

	private volatile UUID uuid;

	@TableColumn(name = "status", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.INTEGER)
	private final Integer status;

	@TableColumn(name = "name", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	private final String name;

	@TableColumn(name = "indexNumberLimit", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer indexNumberLimit;

	@TableColumn(name = "recordNumberLimit", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.LONG)
	private final Long recordNumberLimit;

	@TableColumn(name = "storageLimit", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.LONG)
	private final Long storageLimit;

	@TableColumn(name = "crawlNumberLimit", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer crawlNumberLimit;

	@TableColumn(name = "tasksNumberLimit", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer tasksNumberLimit;
	
	public AccountRecord() {
		id = name = null;
		status = null;
		indexNumberLimit = crawlNumberLimit = tasksNumberLimit = null;
		storageLimit = recordNumberLimit = null;
	}

	private AccountRecord(final Builder builder) {
		id = builder.uuid == null ? null : builder.uuid.toString();
		status = builder.status == null ? null : builder.status.value;
		name = builder.name;
		indexNumberLimit = builder.indexNumberLimit;
		crawlNumberLimit = builder.crawlNumberLimit;
		tasksNumberLimit = builder.tasksNumberLimit;
		storageLimit = builder.storageLimit;
		recordNumberLimit = builder.recordNumberLimit;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof AccountRecord))
			return false;
		if (o == this)
			return true;
		final AccountRecord r = (AccountRecord) o;
		return Objects.equals(id, r.id) && Objects.equals(status, r.status) && Objects.equals(name, r.name) &&
				Objects.equals(indexNumberLimit, r.indexNumberLimit) &&
				Objects.equals(recordNumberLimit, r.recordNumberLimit) &&
				Objects.equals(tasksNumberLimit, r.tasksNumberLimit) &&
				Objects.equals(crawlNumberLimit, r.crawlNumberLimit) && Objects.equals(storageLimit, r.storageLimit);
	}

	public synchronized UUID getId() {
		if (uuid != null)
			return uuid;
		return uuid = id == null ? null : UUID.fromString(id);
	}

	public Long getCreationTime() {
		return HashUtils.getTimeFromUUID(UUID.fromString(id));
	}

	public String getName() {
		return name;
	}

	public ActiveStatus getStatus() {
		return ActiveStatus.resolve(status);
	}

	public int getIndexNumberLimit() {
		return indexNumberLimit == null ? 0 : indexNumberLimit;
	}

	public long getRecordNumberLimit() {
		return recordNumberLimit == null ? 0 : recordNumberLimit;
	}

	public long getStorageLimit() {
		return storageLimit == null ? 0 : storageLimit;
	}

	public int getStorageLimitMb() {
		return (int) (getStorageLimit() / 1024 / 1024);
	}

	public int getCrawlNumberLimit() {
		return crawlNumberLimit == null ? 0 : crawlNumberLimit;
	}

	public int getTasksNumberLimit() {
		return tasksNumberLimit == null ? 0 : tasksNumberLimit;
	}

	public static Builder of(final AccountRecord account) {
		return new Builder(account);
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private UUID uuid;

		private ActiveStatus status;

		private String name;

		private int indexNumberLimit;

		private long recordNumberLimit;

		private long storageLimit;

		private int crawlNumberLimit;

		private int tasksNumberLimit;

		private Builder() {
			uuid = HashUtils.newTimeBasedUUID();
		}

		private Builder(final AccountRecord account) {
			uuid = account.getId();
			status = account.getStatus();
			name = account.name;
			indexNumberLimit = account.getIndexNumberLimit();
			recordNumberLimit = account.getRecordNumberLimit();
			storageLimit = account.getStorageLimit();
			crawlNumberLimit = account.getCrawlNumberLimit();
			tasksNumberLimit = account.getTasksNumberLimit();
		}

		public Builder name(final String name) {
			this.name = checkValidName(name);
			return this;
		}

		public Builder status(final ActiveStatus status) {
			this.status = status;
			return this;
		}

		public Builder indexNumberLimit(final int indexNumberLimit) {
			this.indexNumberLimit = indexNumberLimit;
			return this;
		}

		public Builder recordNumberLimit(final long recordNumberLimit) {
			this.recordNumberLimit = recordNumberLimit;
			return this;
		}

		public Builder storageLimit(final long storageLimit) {
			this.storageLimit = storageLimit;
			return this;
		}

		public Builder crawlNumberLimit(final int crawlNumberLimit) {
			this.crawlNumberLimit = crawlNumberLimit;
			return this;
		}

		public Builder tasksNumberLimit(final int tasksNumberLimit) {
			this.tasksNumberLimit = tasksNumberLimit;
			return this;
		}

		public AccountRecord build() {
			return new AccountRecord(this);
		}

	}

	public static String checkValidName(String name) {
		if (StringUtils.isBlank(name))
			throw new NotAcceptableException("The name cannot be empty.");
		name = name.trim().toLowerCase();
		if (name.contains("--"))
			throw new NotAcceptableException("The name slhould only contains single hyphens.");
		if (name.length() < 3)
			throw new NotAcceptableException("The name should contains at least 3 characters.");
		if (name.startsWith("-") || name.endsWith("-"))
			throw new NotAcceptableException("The name cannot start or end with an hyphen.");
		if (!name.chars().allMatch(value -> CharUtils.isAsciiAlphanumeric((char) value) || '-' == value))
			throw new NotAcceptableException("The name should contains only alpha numeric characters.");
		return name;
	}
}
