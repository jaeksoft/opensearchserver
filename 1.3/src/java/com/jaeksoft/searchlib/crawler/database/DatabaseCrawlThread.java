/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawl.SqlUpdateMode;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class DatabaseCrawlThread extends CrawlThreadAbstract {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private DatabaseCrawl databaseCrawl;

	private Client client;

	private long pendingIndexDocumentCount;

	private long updatedIndexDocumentCount;

	private long pendingDeleteDocumentCount;

	private long updatedDeleteDocumentCount;

	private TaskLog taskLog;

	public DatabaseCrawlThread(Client client, DatabaseCrawlMaster crawlMaster,
			DatabaseCrawl databaseCrawl, TaskLog taskLog) {
		super(client, crawlMaster);
		this.databaseCrawl = databaseCrawl;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		this.taskLog = taskLog;
	}

	public String getCountInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append(getUpdatedIndexDocumentCount());
		sb.append(" (");
		sb.append(getPendingIndexDocumentCount());
		sb.append(") / ");
		sb.append(getUpdatedDeleteDocumentCount());
		sb.append(" (");
		sb.append(getPendingDeleteDocumentCount());
		sb.append(')');
		return sb.toString();
	}

	final public long getPendingIndexDocumentCount() {
		rwl.r.lock();
		try {
			return pendingIndexDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getUpdatedIndexDocumentCount() {
		rwl.r.lock();
		try {
			return updatedIndexDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getPendingDeleteDocumentCount() {
		rwl.r.lock();
		try {
			return pendingDeleteDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getUpdatedDeleteDocumentCount() {
		rwl.r.lock();
		try {
			return updatedDeleteDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	private boolean index(Transaction transaction,
			List<IndexDocument> indexDocumentList, int limit,
			List<String> pkList) throws NoSuchAlgorithmException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.INDEXATION);
		client.updateDocuments(indexDocumentList);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount -= i;
			updatedIndexDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}

		update(transaction, pkList);
		pkList.clear();
		indexDocumentList.clear();
		if (taskLog != null)
			taskLog.setInfo(updatedIndexDocumentCount + " document(s) indexed");
		return true;
	}

	final private String toIdList(List<String> pkList, boolean quote) {
		StringBuffer sb = new StringBuffer();
		boolean b = false;
		for (String uk : pkList) {
			if (b)
				sb.append(',');
			else
				b = true;
			if (quote) {
				sb.append('\'');
				sb.append(uk.replace("'", "''"));
				sb.append('\'');
			} else
				sb.append(uk);
		}
		return sb.toString();
	}

	private final static String PRIMARY_KEY_VARIABLE_NAME = "$PK";

	private void update(Transaction transaction, List<String> pkList)
			throws SearchLibException {
		SqlUpdateMode sqlUpdateMode = databaseCrawl.getSqlUpdateMode();
		if (sqlUpdateMode == SqlUpdateMode.NO_CALL)
			return;
		String sqlUpdate = databaseCrawl.getSqlUpdate();
		String lastSql = null;
		try {
			if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY) {
				for (String uk : pkList) {
					lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME, uk);
					transaction.update(lastSql);
				}
				transaction.commit();
			} else if (sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_LIST) {
				lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME,
						toIdList(pkList, false));
				transaction.update(lastSql);
			} else if (sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST) {
				lastSql = sqlUpdate.replace(PRIMARY_KEY_VARIABLE_NAME,
						toIdList(pkList, true));
				transaction.update(lastSql);
			}
		} catch (SQLException e) {
			throw new SearchLibException("SQL Failed: " + lastSql);
		}
	}

	private boolean delete(Transaction transaction,
			List<String> deleteDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int i = deleteDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.DELETION);
		client.deleteDocuments(deleteDocumentList);
		rwl.w.lock();
		try {
			pendingDeleteDocumentCount -= i;
			updatedDeleteDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}

		update(transaction, deleteDocumentList);

		deleteDocumentList.clear();
		if (taskLog != null)
			taskLog.setInfo(updatedDeleteDocumentCount + " document(s) deleted");
		return true;
	}

	public DatabaseCrawl getDatabaseCrawl() {
		return databaseCrawl;
	}

	final private void runner_update(Transaction transaction,
			ResultSet resultSet, TreeSet<String> columns)
			throws NoSuchAlgorithmException, SQLException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError, InterruptedException {
		String dbPrimaryKey = databaseCrawl.getPrimaryKey();
		DatabaseFieldMap databaseFieldMap = databaseCrawl.getFieldMap();
		int bf = databaseCrawl.getBufferSize();

		IndexDocument indexDocument = null;
		IndexDocument lastFieldContent = null;
		boolean merge = false;
		String lastPrimaryKey = null;

		List<IndexDocument> indexDocumentList = new ArrayList<IndexDocument>(0);
		List<String> pkList = new ArrayList<String>(0);

		while (resultSet.next()) {

			if (dbPrimaryKey != null && dbPrimaryKey.length() == 0)
				dbPrimaryKey = null;

			if (dbPrimaryKey != null) {
				merge = false;
				String pKey = resultSet.getString(dbPrimaryKey);
				if (pKey != null && lastPrimaryKey != null)
					if (pKey.equals(lastPrimaryKey))
						merge = true;
				lastPrimaryKey = pKey;
			}
			if (!merge) {
				if (index(transaction, indexDocumentList, bf, pkList))
					setStatus(CrawlStatus.CRAWL);
				indexDocument = new IndexDocument(databaseCrawl.getLang());
				indexDocumentList.add(indexDocument);
				pendingIndexDocumentCount++;
				pkList.add(lastPrimaryKey);
			}
			LanguageEnum lang = databaseCrawl.getLang();
			IndexDocument newFieldContents = new IndexDocument(lang);
			databaseFieldMap.mapResultSet(client.getWebCrawlMaster(),
					client.getParserSelector(), lang, resultSet, columns,
					newFieldContents);
			if (merge && lastFieldContent != null) {
				indexDocument.addIfNotAlreadyHere(newFieldContents);
			} else
				indexDocument.add(newFieldContents);
			lastFieldContent = newFieldContents;
		}
		index(transaction, indexDocumentList, 0, pkList);
		if (updatedIndexDocumentCount > 0 || updatedDeleteDocumentCount > 0) {
			if (databaseFieldMap.isUrl()) {
				setStatus(CrawlStatus.OPTIMIZATION);
				client.getUrlManager().reload(true, null);
			}
		}
	}

	final private void runner_delete(Transaction transaction,
			ResultSet resultSet, TreeSet<String> columns)
			throws NoSuchAlgorithmException, SQLException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		List<String> deleteKeyList = new ArrayList<String>(0);
		String uniqueKeyDeleteField = databaseCrawl.getUniqueKeyDeleteField();
		int bf = databaseCrawl.getBufferSize();

		while (resultSet.next()) {
			if (delete(transaction, deleteKeyList, bf))
				setStatus(CrawlStatus.CRAWL);
			String uKey = resultSet.getString(uniqueKeyDeleteField);
			if (uKey != null) {
				deleteKeyList.add(uKey);
				pendingDeleteDocumentCount++;
			}
		}
		delete(transaction, deleteKeyList, 0);
	}

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		JDBCConnection connectionManager = new JDBCConnection();
		connectionManager.setDriver(databaseCrawl.getDriverClass());
		connectionManager.setUrl(databaseCrawl.getUrl());
		connectionManager.setUsername(databaseCrawl.getUser());
		connectionManager.setPassword(databaseCrawl.getPassword());
		String sqlUpdate = databaseCrawl.getSqlUpdate();
		if (sqlUpdate != null && sqlUpdate.length() == 0)
			sqlUpdate = null;

		Transaction transaction = null;
		try {
			transaction = connectionManager.getNewTransaction(false,
					databaseCrawl.getIsolationLevel().value);
			Query query = transaction.prepare(databaseCrawl.getSqlSelect());
			ResultSet resultSet = query.getResultSet();

			setStatus(CrawlStatus.CRAWL);

			// Store the list of columns in a treeset
			ResultSetMetaData metaData = resultSet.getMetaData();
			TreeSet<String> columns = new TreeSet<String>();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(metaData.getColumnLabel(i));

			if (databaseCrawl.getUniqueKeyDeleteField() != null)
				runner_delete(transaction, resultSet, columns);
			else
				runner_update(transaction, resultSet, columns);

			if (updatedIndexDocumentCount > 0 || updatedDeleteDocumentCount > 0)
				client.reload();
		} finally {
			if (transaction != null)
				transaction.close();
		}

	}

	@Override
	protected String getCurrentInfo() {
		return "";
	}

}
