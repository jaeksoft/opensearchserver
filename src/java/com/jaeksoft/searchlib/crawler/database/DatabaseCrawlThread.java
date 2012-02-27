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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class DatabaseCrawlThread extends CrawlThreadAbstract {

	private DatabaseCrawl databaseCrawl;

	private Client client;

	private long pendingIndexDocumentCount;

	private long updatedIndexDocumentCount;

	private TaskLog taskLog;

	public DatabaseCrawlThread(Client client, DatabaseCrawlMaster crawlMaster,
			DatabaseCrawl databaseCrawl, TaskLog taskLog) {
		super(client, crawlMaster);
		this.databaseCrawl = databaseCrawl;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		this.taskLog = taskLog;
	}

	public String getCountInfo() {
		return getUpdatedIndexDocumentCount() + " ("
				+ getPendingIndexDocumentCount() + ")";
	}

	public long getPendingIndexDocumentCount() {
		return pendingIndexDocumentCount;
	}

	public long getUpdatedIndexDocumentCount() {
		return updatedIndexDocumentCount;
	}

	private boolean index(List<IndexDocument> indexDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.INDEXATION);
		client.updateDocuments(indexDocumentList);
		pendingIndexDocumentCount -= i;
		updatedIndexDocumentCount += i;
		indexDocumentList.clear();
		if (taskLog != null)
			taskLog.setInfo(updatedIndexDocumentCount + " document(s) indexed");
		return true;
	}

	public DatabaseCrawl getDatabaseCrawl() {
		return databaseCrawl;
	}

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		JDBCConnection connectionManager = new JDBCConnection();
		connectionManager.setDriver(databaseCrawl.getDriverClass());
		connectionManager.setUrl(databaseCrawl.getUrl());
		connectionManager.setUsername(databaseCrawl.getUser());
		connectionManager.setPassword(databaseCrawl.getPassword());

		Transaction transaction = null;
		try {
			transaction = connectionManager.getNewTransaction(false);
			Query query = transaction.prepare(databaseCrawl.getSql());
			ResultSet resultSet = query.getResultSet();
			List<IndexDocument> indexDocumentList = new ArrayList<IndexDocument>();
			IndexDocument indexDocument = null;
			IndexDocument lastFieldContent = null;
			String lastPrimaryKey = null;
			String dbPrimaryKey = databaseCrawl.getPrimaryKey();
			if (dbPrimaryKey != null && dbPrimaryKey.length() == 0)
				dbPrimaryKey = null;
			boolean merge = false;
			setStatus(CrawlStatus.CRAWL);

			DatabaseFieldMap databaseFieldMap = databaseCrawl.getFieldMap();

			// Store the list of columns in a treeset
			ResultSetMetaData metaData = resultSet.getMetaData();
			TreeSet<String> columns = new TreeSet<String>();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(metaData.getColumnLabel(i));

			while (resultSet.next()) {
				if (dbPrimaryKey != null) {
					merge = false;
					String pKey = resultSet.getString(dbPrimaryKey);
					if (pKey != null && lastPrimaryKey != null)
						if (pKey.equals(lastPrimaryKey))
							merge = true;
					lastPrimaryKey = pKey;
				}
				if (!merge) {
					if (index(indexDocumentList, 100))
						setStatus(CrawlStatus.CRAWL);
					indexDocument = new IndexDocument(databaseCrawl.getLang());
					indexDocumentList.add(indexDocument);
					pendingIndexDocumentCount++;
				}
				IndexDocument newFieldContents = new IndexDocument(
						databaseCrawl.getLang());
				databaseFieldMap.mapResultSet(client.getWebCrawlMaster(),
						client.getParserSelector(), resultSet, columns,
						newFieldContents);
				if (merge && lastFieldContent != null) {
					indexDocument.addIfNotAlreadyHere(newFieldContents);
				} else
					indexDocument.add(newFieldContents);
				lastFieldContent = newFieldContents;
			}
			index(indexDocumentList, 0);
			if (updatedIndexDocumentCount > 0) {
				if (databaseFieldMap.isUrl()) {
					setStatus(CrawlStatus.OPTIMIZATION);
					client.getUrlManager().reload(true);
				}
				client.reload();
			}
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
