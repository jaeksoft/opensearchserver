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
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ConsumerEx;

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
import java.util.function.Consumer;

public class AccountsService extends BaseTableService<AccountRecord> {

	public AccountsService(final TableServiceInterface tableServiceInterface)
			throws NoSuchMethodException, URISyntaxException {
		super(tableServiceInterface, AccountRecord.class);
	}

	public Integer getCount() {
		return tableService.getTableStatus().getNumRows();
	}

	public TableRequestResultRecords<AccountRecord> getAccounts(final int start, final int rows) {
		try {
			return tableService.queryRows(TableRequest.from(start, rows).column(columnsArray).build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account list", e);
		}
	}

	public AccountRecord getAccountById(final UUID accountId) {
		try {
			return tableService.getRow(Objects.requireNonNull(accountId, "The account UUID is null").toString(),
					columnsSet);
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
			final List<AccountRecord> accountList = tableService.getRows(columnsSet, idSet);
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
			final TableRequestResultRecords<AccountRecord> result = tableService.queryRows(TableRequest.from(0, 1)
					.column(columnsArray)
					.query(new TableQuery.StringTerm("name", name))
					.build());
			return result != null && result.count != null && result.count == 1 ? result.records.get(0) : null;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get account by name", e);
		}
	}

	public synchronized UUID createAccount(final String name) {
		final String validName = AccountRecord.checkValidName(name);
		final AccountRecord existingAcount = getAccountByName(validName);
		if (existingAcount != null)
			throw new NotAcceptableException("This name is already taken");
		final AccountRecord newAccount = AccountRecord.of().name(validName).build();
		tableService.upsertRow(newAccount.id, newAccount);
		return newAccount.getId();
	}

	public AccountRecord getExistingAccount(final UUID accountId) {
		final AccountRecord account = getAccountById(accountId);
		if (account == null)
			throw new NotFoundException("Account not found: " + accountId);
		return account;
	}

	public AccountRecord getExistingAccount(final String accountName) {
		final AccountRecord account = getAccountByName(accountName);
		if (account == null)
			throw new NotFoundException("Account not found: " + accountName);
		return account;
	}

	public AccountRecord findExistingAccount(String pathPart) {
		final UUID accountId;
		try {
			accountId = UUID.fromString(pathPart);
		} catch (IllegalArgumentException e) {
			return getExistingAccount(pathPart);
		}
		return getExistingAccount(accountId);
	}

	/**
	 * Update an AccountRecord.
	 *
	 * @param accountId
	 * @param builderConsumer
	 * @return
	 */
	public boolean update(final UUID accountId, final Consumer<AccountRecord.Builder> builderConsumer) {
		final AccountRecord oldAccount = getExistingAccount(accountId);
		final AccountRecord.Builder builder = AccountRecord.of(oldAccount);
		builderConsumer.accept(builder);
		final AccountRecord newAccount = builder.build();
		if (!Objects.equals(oldAccount.getName(), newAccount.getName())) {
			final AccountRecord alreadyExistingAccount = getAccountByName(newAccount.getName());
			if (alreadyExistingAccount != null && !accountId.equals(alreadyExistingAccount.getId()))
				throw new NotAcceptableException("This name is already taken");
		}
		if (newAccount.equals(oldAccount))
			return false;
		tableService.upsertRow(newAccount.id, newAccount);
		return true;
	}

	public TableRequestResultRecords<AccountRecord> getActiveAccounts(final int start, final int rows)
			throws IOException, ReflectiveOperationException {
		return tableService.queryRows(TableRequest.from(start, rows)
				.query(new TableQuery.IntegerTerm("status", ActiveStatus.ENABLED.value))
				.column(columnsArray)
				.build());
	}

	public int forEachActiveAccount(final ConsumerEx<AccountRecord, Exception> accountConsumer) throws Exception {
		int start = 0;
		final int rows = 20;
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
	}

}
