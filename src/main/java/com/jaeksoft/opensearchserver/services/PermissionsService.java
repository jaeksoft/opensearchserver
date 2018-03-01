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

import com.jaeksoft.opensearchserver.model.PermissionLevel;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;
import com.qwazr.server.ServerException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

public class PermissionsService {

	private final AnnotatedTableService<PermissionRecord> permissions;

	public PermissionsService(final TableServiceInterface tableServiceInterface)
			throws NoSuchMethodException, URISyntaxException {
		permissions = new AnnotatedTableService<>(tableServiceInterface, PermissionRecord.class);
		permissions.createUpdateTable();
		permissions.createUpdateFields();
	}

	public TableRequestResultRecords<PermissionRecord> getPermissionsByUser(final UUID userId, final int start,
			final int rows) {
		try {
			return permissions.queryRows(TableRequest.from(start, rows)
					.column(PermissionRecord.COLUMNS)
					.query(new TableQuery.StringTerm("userId",
							Objects.requireNonNull(userId, "The userID is null)").toString()))
					.build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get permissions for user " + userId, e);
		}
	}

	public TableRequestResultRecords<PermissionRecord> getPermissionsByAccount(final UUID accountId, final int start,
			final int rows) {
		try {
			return permissions.queryRows(TableRequest.from(start, rows)
					.column(PermissionRecord.COLUMNS)
					.query(new TableQuery.StringTerm("accountId",
							Objects.requireNonNull(accountId, "The accountID is null)").toString()))
					.build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get permissions for account " + accountId, e);
		}
	}

	public PermissionRecord getPermission(final UUID userId, final UUID accountId) {
		try {
			final PermissionRecord permissionFinder = PermissionRecord.of(userId, accountId).build();
			return permissions.getRow(permissionFinder.id, PermissionRecord.COLUMNS_SET);
		} catch (ServerException e) {
			if (e.getStatusCode() == 404)
				return null;
			throw e;
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() == 404)
				return null;
			throw e;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get permission: " + e.getMessage(), e);
		}
	}

	public boolean setPermission(final UUID userId, final UUID accountId, final PermissionLevel level) {
		final PermissionRecord existingPermission = getPermission(userId, accountId);
		if (existingPermission != null && existingPermission.getLevel() == level)
			return false;
		final PermissionRecord newPermission = PermissionRecord.of(userId, accountId).level(level).build();
		permissions.upsertRow(newPermission.id, newPermission);
		return true;
	}

	public boolean removePermission(final UUID userId, final UUID accountId) {
		final PermissionRecord existingPermission = getPermission(userId, accountId);
		if (existingPermission == null)
			return false;
		final PermissionRecord permission = PermissionRecord.of(userId, accountId).build();
		permissions.deleteRow(permission.id);
		return true;
	}

}
