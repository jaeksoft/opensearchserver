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

import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.ActiveStatus;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ConsumerEx;
import org.apache.commons.lang3.CharUtils;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class AccountsService {

	private final AnnotatedTableService<AccountRecord> accounts;

	public AccountsService(final TableServiceInterface tableServiceInterface)
			throws NoSuchMethodException, URISyntaxException {
		accounts = new AnnotatedTableService<>(tableServiceInterface, AccountRecord.class);
		accounts.createUpdateTable();
		accounts.createUpdateFields();
	}

	public Integer getCount() {
		return accounts.getTableStatus().getNumRows();
	}

	public TableRequestResultRecords<AccountRecord> getAccounts(final int start, final int rows) {
		try {
			return accounts.queryRows(TableRequest.from(start, rows).column(AccountRecord.COLUMNS).build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account list", e);
		}
	}

	public AccountRecord getAccountById(final UUID accountId) {
		try {
			return accounts.getRow(Objects.requireNonNull(accountId, "The account UUID is null").toString(),
					AccountRecord.COLUMNS_SET);
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account by id", e);
		}
	}

	public Map<AccountRecord, PermissionRecord> getAccountsByIds(
			final TableRequestResultRecords<PermissionRecord> permissions) {
		try {
			if (permissions == null || permissions.records == null)
				return Collections.emptyMap();
			final Set<String> idSet = new LinkedHashSet<>();
			permissions.records.forEach(permission -> idSet.add(permission.getAccountId().toString()));
			final List<AccountRecord> accountList = accounts.getRows(AccountRecord.COLUMNS_SET, idSet);
			if (accountList == null || accountList.isEmpty())
				return Collections.emptyMap();
			final Map<AccountRecord, PermissionRecord> results = new LinkedHashMap<>();
			final Iterator<PermissionRecord> permissionsIterator = permissions.records.iterator();
			accountList.forEach(account -> results.put(account, permissionsIterator.next()));
			return results;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account by id", e);
		}
	}

	public AccountRecord getAccountByName(final String name) {
		if (StringUtils.isBlank(name))
			return null;
		try {
			final TableRequestResultRecords<AccountRecord> result = accounts.queryRows(TableRequest.from(0, 1)
					.column(AccountRecord.COLUMNS)
					.query(new TableQuery.StringTerm("name", name))
					.build());
			return result != null && result.count != null && result.count == 1 ? result.records.get(0) : null;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account by name", e);
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

	public synchronized UUID createAccount(final String name) {
		final String validName = checkValidName(name);
		final AccountRecord existingAcount = getAccountByName(validName);
		if (existingAcount != null)
			throw new NotAcceptableException("This name is already taken");
		final AccountRecord newAccount = AccountRecord.of().name(validName).build();
		accounts.upsertRow(newAccount.id, newAccount);
		return newAccount.getId();
	}

	public AccountRecord getExistingAccount(final UUID accountId) {
		final AccountRecord account = getAccountById(accountId);
		if (account == null)
			throw new NotFoundException("Account not found: " + accountId);
		return account;
	}

	public boolean updateStatus(final UUID accountId, final ActiveStatus status) {
		final AccountRecord oldAccount = getExistingAccount(accountId);
		if (oldAccount.getStatus() != null && oldAccount.getStatus() == status)
			return false;
		final AccountRecord newAccount = AccountRecord.of(oldAccount).status(status).build();
		accounts.upsertRow(newAccount.id, newAccount);
		return true;
	}

	public boolean updateName(final UUID accountId, final String name) {
		final String validName = checkValidName(name);
		final AccountRecord alreadyExistingAccount = getAccountByName(validName);
		if (alreadyExistingAccount != null && !accountId.equals(alreadyExistingAccount.getId()))
			throw new NotAcceptableException("This name is already taken");
		final AccountRecord oldAccount = getExistingAccount(accountId);
		if (Objects.equals(oldAccount.getName(), validName))
			return false;
		final AccountRecord newAccount = AccountRecord.of(oldAccount).name(validName).build();
		accounts.upsertRow(newAccount.id, newAccount);
		return true;
	}

	public TableRequestResultRecords<AccountRecord> getActiveAccounts(final int start, final int rows)
			throws IOException, ReflectiveOperationException {
		return accounts.queryRows(TableRequest.from(start, rows)
				.query(new TableQuery.IntegerTerm("status", ActiveStatus.ENABLED.value))
				.column(AccountRecord.COLUMNS)
				.build());
	}

	public int forEachActiveAccount(final ConsumerEx<AccountRecord, IOException> accountConsumer) {
		int start = 0;
		final int rows = 20;
		try {
			for (; ; ) {
				final List<AccountRecord> accounts = getActiveAccounts(start, rows).getRecords();
				if (accounts == null || accounts.isEmpty())
					break;
				start += accounts.size();
				if (accountConsumer != null)
					for (AccountRecord account : accounts)
						accountConsumer.accept(account);
			}
			return start;
		} catch (IOException | ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

}
