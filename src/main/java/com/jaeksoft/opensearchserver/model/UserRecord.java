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
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table("users")
public class UserRecord implements Principal {

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	public final String id;

	private volatile UUID uuid;

	@TableColumn(name = "status", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer status;

	@TableColumn(name = "name", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	private final String name;

	@TableColumn(name = "email", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	private final String email;

	@TableColumn(name = "password", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	private final String password;

	public UserRecord() {
		id = email = name = password = null;
		status = 0;
	}

	private UserRecord(final Builder builder) {
		id = builder.uuid == null ? null : builder.uuid.toString();
		status = builder.status == null ? null : builder.status.value;
		name = builder.name;
		email = builder.email;
		password = builder.password;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserRecord))
			return false;
		if (o == this)
			return true;
		final UserRecord u = (UserRecord) o;
		return Objects.equals(id, u.id) && Objects.equals(email, u.email) && Objects.equals(name, u.name) &&
				Objects.equals(password, u.password) && Objects.equals(status, u.status);
	}

	public boolean matchPassword(final String appSalt, final String clearPassword) {
		return !StringUtils.isBlank(password) && !StringUtils.isBlank(clearPassword) &&
				password.equals(digestPassword(appSalt, id, clearPassword));
	}

	public synchronized UUID getId() {
		if (uuid != null)
			return uuid;
		return uuid = id == null ? null : UUID.fromString(id);
	}

	public Long getCreationTime() {
		return HashUtils.getTimeFromUUID(UUID.fromString(id));
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String getName() {
		return name;
	}

	public ActiveStatus getStatus() {
		return ActiveStatus.resolve(status);
	}

	private static String digestPassword(final String appSalt, String id, String clearPassword) {
		return new HmacUtils(HmacAlgorithms.HMAC_SHA_512, clearPassword + id + appSalt).hmacHex(clearPassword);
	}

	public static Builder of(UserRecord user) {
		return new Builder(user);
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private UUID uuid;

		private ActiveStatus status;

		private String name;

		private String email;

		private String password;

		private Builder() {
			uuid = HashUtils.newTimeBasedUUID();
		}

		private Builder(final UserRecord user) {
			uuid = user.getId();
			status = user.getStatus();
			email = user.email;
			password = user.password;
		}

		public Builder email(final String email) {
			this.email = email;
			return this;
		}

		public Builder name(final String name) {
			this.name = name;
			return this;
		}

		public Builder status(final ActiveStatus status) {
			this.status = status;
			return this;
		}

		public Builder password(final String appSalt, final String clearPassword) {
			this.password = digestPassword(appSalt, uuid.toString(), clearPassword);
			return this;
		}

		public UserRecord build() {
			return new UserRecord(this);
		}

	}
}
