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
import com.datastax.driver.core.Row;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;
import com.qwazr.library.cassandra.CassandraCluster;
import com.qwazr.library.cassandra.CassandraSession;
import com.qwazr.utils.ObjectMappers;

import java.io.Closeable;
import java.io.IOException;
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

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		try (final CassandraCluster cluster = databaseCrawl.getCluster()) {
			try (final CassandraSession session = databaseCrawl.getKeySpace() == null ?
					cluster.getSession() :
					cluster.getSession(databaseCrawl.getKeySpace())) {
				final DatabaseCrawlCassandra.ComplexQuery complexQuery =
						ObjectMappers.JSON.readValue(databaseCrawl.getCqlQuery(),
								DatabaseCrawlCassandra.ComplexQuery.class);
				setStatus(CrawlStatus.CRAWL);
				try (final ResultSetConsumer resultSetConsumer = new ResultSetConsumer(session)) {
					resultSetConsumer.execute(null, complexQuery);
				}
				if (updatedIndexDocumentCount > 0 || updatedDeleteDocumentCount > 0)
					client.reload();
			}
		}
	}
	
	class ResultSetConsumer extends DatabaseCrawlCassandra.ResultSetConsumer implements Closeable {

		private final DatabaseCassandraFieldMap fieldMap;
		private final List<IndexDocument> indexDocumentList;
		private final LanguageEnum lang;
		private final FieldMapContext fieldMapContext;
		private final String uniqueField;
		private final Set<String> filePathSet;

		volatile ColumnDefinitions columnDefinitions;

		ResultSetConsumer(CassandraSession session) throws SearchLibException {
			super(session, databaseCrawl.getBufferSize());
			fieldMap = (DatabaseCassandraFieldMap) databaseCrawl.getFieldMap();
			indexDocumentList = new ArrayList<>(0);
			lang = databaseCrawl.getLang();
			fieldMapContext = new FieldMapContext(client, lang);
			uniqueField = client.getSchema().getUniqueField();
			filePathSet = new TreeSet<>();
		}

		@Override
		void columns(ColumnDefinitions columnsDef) {
			columnDefinitions = columnsDef;
		}

		@Override
		boolean row(Row row) throws Exception {
			if (isAborted())
				return false;
			IndexDocument indexDocument = new IndexDocument(lang);
			fieldMap.mapRow(fieldMapContext, row, columnDefinitions, indexDocument, filePathSet);
			if (uniqueField != null && !indexDocument.hasContent(uniqueField)) {
				rwl.w.lock();
				try {
					ignoredDocumentCount++;
				} finally {
					rwl.w.unlock();
				}
				return true;
			}
			indexDocumentList.add(indexDocument);
			rwl.w.lock();
			try {
				pendingIndexDocumentCount++;
			} finally {
				rwl.w.unlock();
			}
			if (index(indexDocumentList, bufferSize))
				setStatus(CrawlStatus.CRAWL);
			return false;
		}

		public void close() throws IOException {
			try {
				index(indexDocumentList, 0);
			} catch (SearchLibException | InterruptedException e) {
				throw new IOException(e);
			}
		}
	}

}
