/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.DatabaseUtils;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.utils.Variables;

public class DatabaseCrawlSqlThread extends DatabaseCrawlThread {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final DatabaseCrawlSql databaseCrawl;

	public DatabaseCrawlSqlThread(Client client,
			DatabaseCrawlMaster crawlMaster, DatabaseCrawlSql databaseCrawl,
			Variables variables, InfoCallback infoCallback) {
		super(client, crawlMaster, databaseCrawl, infoCallback);
		this.databaseCrawl = (DatabaseCrawlSql) databaseCrawl.duplicate();
		this.databaseCrawl.applyVariables(variables);
	}

	private boolean index(Transaction transaction,
			List<IndexDocument> indexDocumentList, int limit,
			List<String> pkList) throws NoSuchAlgorithmException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
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

		DatabaseUtils.update(transaction, pkList, null,
				databaseCrawl.getSqlUpdateMode(), databaseCrawl.getSqlUpdate());
		pkList.clear();
		indexDocumentList.clear();
		if (infoCallback != null)
			infoCallback.setInfo(updatedIndexDocumentCount
					+ " document(s) indexed");
		return true;
	}

	private boolean delete(Transaction transaction,
			List<String> deleteDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		int i = deleteDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.DELETION);
		client.deleteDocuments(null, deleteDocumentList);
		rwl.w.lock();
		try {
			pendingDeleteDocumentCount -= i;
			updatedDeleteDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}

		DatabaseUtils.update(transaction, deleteDocumentList, null,
				databaseCrawl.getSqlUpdateMode(), databaseCrawl.getSqlUpdate());

		deleteDocumentList.clear();
		if (infoCallback != null)
			infoCallback.setInfo(updatedDeleteDocumentCount
					+ " document(s) deleted");
		return true;
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
		JDBCConnection connectionManager = databaseCrawl.getNewJdbcConnection();
		String sqlUpdate = databaseCrawl.getSqlUpdate();
		if (sqlUpdate != null && sqlUpdate.length() == 0)
			sqlUpdate = null;

		Transaction transaction = null;
		try {
			transaction = databaseCrawl.getNewTransaction(connectionManager);
			Query query = transaction.prepare(databaseCrawl.getSqlSelect());
			ResultSet resultSet = query.getResultSet();

			setStatus(CrawlStatus.CRAWL);

			// Store the list of columns in a treeset
			ResultSetMetaData metaData = resultSet.getMetaData();
			TreeSet<String> columns = new TreeSet<String>();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(metaData.getColumnLabel(i));

			String ukDeleteField = databaseCrawl.getUniqueKeyDeleteField();
			if (ukDeleteField != null && ukDeleteField.length() == 0)
				ukDeleteField = null;

			if (ukDeleteField != null)
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

}
