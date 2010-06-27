/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;

public class DatabaseCrawlThread extends CrawlThreadAbstract {

	private DatabaseCrawl databaseCrawl;

	private Client client;

	private long pendingIndexDocumentCount;

	private long updatedIndexDocumentCount;

	public DatabaseCrawlThread(Client client, DatabaseCrawlMaster crawlMaster,
			DatabaseCrawl databaseCrawl) {
		super(client, crawlMaster);
		this.databaseCrawl = databaseCrawl;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
	}

	@Override
	protected String getCurrentInfo() {
		return config.getIndexDirectory().getName() + " "
				+ databaseCrawl.getName();
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

	@Override
	public void release() {
	}

	private void index(List<IndexDocument> indexDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return;
		client.updateDocuments(indexDocumentList);
		pendingIndexDocumentCount -= i;
		updatedIndexDocumentCount += i;
		indexDocumentList.clear();
	}

	public DatabaseCrawl getDatabaseCrawl() {
		return databaseCrawl;
	}

	@Override
	public void runner() throws Exception {
		JDBCConnection connectionManager = new JDBCConnection();
		connectionManager.setDriver(databaseCrawl.getDriverClass());
		connectionManager.setUrl(databaseCrawl.getUrl());
		connectionManager.setUsername(databaseCrawl.getUser());
		connectionManager.setPassword(databaseCrawl.getPassword());

		Transaction transaction = null;
		try {
			transaction = connectionManager.getNewTransaction(false,
					java.sql.Connection.TRANSACTION_READ_COMMITTED);
			Query query = transaction.prepare(databaseCrawl.getSql());
			ResultSet resultSet = query.getResultSet();
			List<IndexDocument> indexDocumentList = new ArrayList<IndexDocument>();
			while (resultSet.next()) {
				IndexDocument indexDocument = new IndexDocument(databaseCrawl
						.getLang());
				databaseCrawl.getFieldMap().mapResultSet(resultSet,
						indexDocument);
				indexDocumentList.add(indexDocument);
				pendingIndexDocumentCount++;
				index(indexDocumentList, 1000);
			}
			index(indexDocumentList, 0);
			if (updatedIndexDocumentCount > 0)
				client.reload(null);
		} finally {
			if (transaction != null)
				transaction.close();
		}

	}

}
