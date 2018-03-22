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

@Table("permissions")
public class PermissionRecord {

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	public final String id;

	@TableColumn(name = "level", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer level;

	@TableColumn(name = "userId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	private final String userId;

	@TableColumn(name = "accountId", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	private final String accountId;

	private volatile UUID userUuid;
	private volatile UUID accountUuid;

	public PermissionRecord() {
		id = null;
		level = null;
		userId = null;
		accountId = null;
	}

	private PermissionRecord(final Builder builder) {
		id = builder.userUuid + "_" + builder.accountUuid;
		level = builder.level == null ? null : builder.level.value;
		userId = Objects.requireNonNull(builder.userUuid, "The userID is null").toString();
		accountId = Objects.requireNonNull(builder.accountUuid, "The accountID is null").toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PermissionRecord))
			return false;
		if (o == this)
			return true;
		final PermissionRecord r = (PermissionRecord) o;
		return Objects.equals(id, r.id) && Objects.equals(level, r.level) && Objects.equals(userId, r.userId) &&
				Objects.equals(accountId, r.accountId);
	}

	public synchronized UUID getUserId() {
		if (userUuid != null)
			return userUuid;
		return userUuid = userId == null ? null : UUID.fromString(userId);
	}

	public synchronized UUID getAccountId() {
		if (accountUuid != null)
			return accountUuid;
		return accountUuid = accountId == null ? null : UUID.fromString(accountId);
	}

	public PermissionLevel getLevel() {
		return PermissionLevel.resolve(level);
	}

	public Builder from() {
		return new Builder(this);
	}

	public static Builder of(UUID userId, UUID accountId) {
		return new Builder(Objects.requireNonNull(userId, "The userID is null"),
				Objects.requireNonNull(accountId, "The accountID is null"));
	}

	public static class Builder {

		private PermissionLevel level;

		private final UUID userUuid;

		private final UUID accountUuid;

		private Builder(final UUID userUuid, final UUID accountUuid) {
			this.userUuid = userUuid;
			this.accountUuid = accountUuid;
		}

		private Builder(final PermissionRecord permission) {
			userUuid = permission.getUserId();
			accountUuid = permission.getAccountId();
			level = PermissionLevel.resolve(permission.level);
		}

		public Builder level(final PermissionLevel level) {
			this.level = level;
			return this;
		}

		public PermissionRecord build() {
			return new PermissionRecord(this);
		}

	}

}
