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
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table("users")
public class UserRecord implements Principal {

	@TableColumn(name = TableDefinition.ID_COLUMN_NAME)
	private final String id;

	@TableColumn(name = "status", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.INTEGER)
	private final Integer status;

	@TableColumn(name = "name", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	private final String name;

	@TableColumn(name = "email", mode = ColumnDefinition.Mode.INDEXED, type = ColumnDefinition.Type.STRING)
	private final String email;

	@TableColumn(name = "password", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	private final String password;

	@TableColumn(name = "accountId", mode = ColumnDefinition.Mode.STORED, type = ColumnDefinition.Type.STRING)
	private final Set<String> accountIds;

	public static final String[] COLUMNS =
			new String[] { TableDefinition.ID_COLUMN_NAME, "status", "email", "password", "accountIds" };

	public static final Set<String> COLUMNS_SET = new HashSet<>(Arrays.asList(COLUMNS));

	public UserRecord() {
		id = email = name = password = null;
		accountIds = null;
		status = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserRecord))
			return false;
		if (o == this)
			return true;
		final UserRecord u = (UserRecord) o;
		return Objects.equals(id, u.id) && Objects.equals(email, u.email) && Objects.equals(name, u.name) &&
				Objects.equals(password, u.password) && Objects.equals(status, u.status) &&
				CollectionsUtils.unorderedEquals(accountIds, u.accountIds);
	}

	private UserRecord(final Builder builder) {
		id = builder.id;
		status = builder.status;
		name = builder.name;
		email = builder.email;
		password = builder.password;
		accountIds = builder.accountIds == null ?
				null :
				Collections.unmodifiableSet(new LinkedHashSet<>(builder.accountIds));
	}

	public boolean isActivated() {
		return status != null && status == 1;
	}

	public boolean isNoPassword() {
		return StringUtils.isEmpty(password);
	}

	public boolean matchPassword(final String appSalt, final String clearPassword) {
		return !StringUtils.isBlank(password) && !StringUtils.isBlank(clearPassword) &&
				password.equals(digestPassword(appSalt, id, clearPassword));
	}

	public String getId() {
		return id;
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

	public Set<String> getAccountIds() {
		return accountIds;
	}

	public Integer getStatus() {
		return status;
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

		private String id;

		private Integer status;

		private String name;

		private String email;

		private String password;

		private Set<String> accountIds;

		private Builder() {
			id = HashUtils.newTimeBasedUUID().toString();
		}

		private Builder(final UserRecord user) {
			id = user.id;
			status = user.status;
			email = user.email;
			password = user.password;
			accountIds = user.accountIds == null ? null : new LinkedHashSet<>(user.accountIds);
		}

		public Builder email(final String email) {
			this.email = email;
			return this;
		}

		public Builder name(final String name) {
			this.name = name;
			return this;
		}

		public Builder status(final Integer status) {
			this.status = status;
			return this;
		}

		public Builder password(final String appSalt, final String clearPassword) {
			this.password = digestPassword(appSalt, id, clearPassword);
			return this;
		}

		public Builder accountIds(final Collection<String> accountIds) {
			if (accountIds == null || accountIds.isEmpty()) {
				this.accountIds = null;
				return this;
			}
			if (this.accountIds == null)
				this.accountIds = new LinkedHashSet<>();
			else
				this.accountIds.clear();
			this.accountIds.addAll(accountIds);
			return this;
		}

		public UserRecord build() {
			return new UserRecord(this);
		}
	}
}
