/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2015-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.database;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;
import com.qwazr.library.cassandra.CassandraCluster;
import com.qwazr.library.cassandra.CassandraSession;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DatabaseCrawlCassandraThread extends DatabaseCrawlThread {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final DatabaseCrawlCassandra databaseCrawl;

	public DatabaseCrawlCassandraThread(Client client, DatabaseCrawlMaster crawlMaster,
			DatabaseCrawlCassandra databaseCrawl, Variables variables, InfoCallback infoCallback) {
		super(client, crawlMaster, databaseCrawl, infoCallback);
		this.databaseCrawl = (DatabaseCrawlCassandra) databaseCrawl.duplicate();
		this.databaseCrawl.applyVariables(variables);
	}

	private void runnerUpdate(ResultSet resultSet)
			throws SearchLibException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			IOException, ParseException, SyntaxError, URISyntaxException, InterruptedException {
		final int limit = databaseCrawl.getBufferSize();
		final DatabaseCassandraFieldMap fieldMap = (DatabaseCassandraFieldMap) databaseCrawl.getFieldMap();
		final List<IndexDocument> indexDocumentList = new ArrayList<>(0);
		final LanguageEnum lang = databaseCrawl.getLang();
		final FieldMapContext fieldMapContext = new FieldMapContext(client, lang);
		final String uniqueField = client.getSchema().getUniqueField();
		final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
		final Set<String> filePathSet = new TreeSet<>();

		for (final Row row : resultSet) {
			if (isAborted())
				break;
			IndexDocument indexDocument = new IndexDocument(lang);
			fieldMap.mapRow(fieldMapContext, row, columnDefinitions, indexDocument, filePathSet);
			if (uniqueField != null && !indexDocument.hasContent(uniqueField)) {
				rwl.w.lock();
				try {
					ignoredDocumentCount++;
				} finally {
					rwl.w.unlock();
				}
				continue;
			}
			indexDocumentList.add(indexDocument);
			rwl.w.lock();
			try {
				pendingIndexDocumentCount++;
			} finally {
				rwl.w.unlock();
			}
			if (index(indexDocumentList, limit))
				setStatus(CrawlStatus.CRAWL);

		}
		index(indexDocumentList, 0);
	}

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		try (final CassandraCluster cluster = databaseCrawl.getCluster()) {
			try (final CassandraSession session = databaseCrawl.getKeySpace() == null ?
					cluster.getSession() :
					cluster.getSession(databaseCrawl.getKeySpace())) {
				final ResultSet resultSet =
						session.executeWithFetchSize(databaseCrawl.getCqlQuery(), databaseCrawl.getBufferSize());
				setStatus(CrawlStatus.CRAWL);
				if (resultSet != null)
					runnerUpdate(resultSet);
				if (updatedIndexDocumentCount > 0 || updatedDeleteDocumentCount > 0)
					client.reload();
			}
		}
	}
}
