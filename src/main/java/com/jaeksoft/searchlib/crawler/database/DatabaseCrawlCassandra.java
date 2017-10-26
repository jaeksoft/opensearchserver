/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.qwazr.library.cassandra.CassandraCluster;
import com.qwazr.library.cassandra.CassandraSession;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.ObjectMappers;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DatabaseCrawlCassandra extends DatabaseCrawlAbstract {

	private String keySpace;
	private String cqlQuery;

	public DatabaseCrawlCassandra(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager,
			String name) {
		super(crawlMaster, propertyManager, name, new DatabaseCassandraFieldMap());
		keySpace = null;
		cqlQuery = null;
	}

	public void applyVariables(Variables variables) {
		if (variables == null)
			return;
		keySpace = variables.replace(keySpace);
		cqlQuery = variables.replace(cqlQuery);
	}

	public DatabaseCrawlCassandra(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager) {
		this(crawlMaster, propertyManager, null);
	}

	protected DatabaseCrawlCassandra(DatabaseCrawlCassandra crawl) {
		this((DatabaseCrawlMaster) crawl.threadMaster, crawl.propertyManager);
		crawl.copyTo(this);
	}

	@Override
	public DatabaseCrawlAbstract duplicate() {
		return new DatabaseCrawlCassandra(this);
	}

	@Override
	public void copyTo(DatabaseCrawlAbstract crawlAbstract) {
		super.copyTo(crawlAbstract);
		DatabaseCrawlCassandra crawl = (DatabaseCrawlCassandra) crawlAbstract;
		crawl.keySpace = this.keySpace;
		crawl.cqlQuery = this.cqlQuery;
	}

	@Override
	public DatabaseCrawlEnum getType() {
		return DatabaseCrawlEnum.DB_CASSANDRA;
	}

	protected final static String DBCRAWL_ATTR_KEY_SPACE = "keySpace";
	protected final static String DBCRAWL_NODE_NAME_CQL_QUERY = "cqlQuery";

	public DatabaseCrawlCassandra(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager,
			XPathParser xpp, Node item) throws XPathExpressionException {
		super(crawlMaster, propertyManager, xpp, item, new DatabaseCassandraFieldMap());
		setKeySpace(XPathParser.getAttributeString(item, DBCRAWL_ATTR_KEY_SPACE));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_CQL_QUERY);
		if (sqlNode != null)
			setCqlQuery(xpp.getNodeString(sqlNode, true));
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(), DBCRAWL_ATTR_TYPE, getType().name(),
				DBCRAWL_ATTR_USER, getUser(), DBCRAWL_ATTR_PASSWORD, getPassword(), DBCRAWL_ATTR_URL, getUrl(),
				DBCRAWL_ATTR_LANG, getLang().getCode(), DBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(getBufferSize()),
				DBCRAWL_ATTR_MSSLEEP, Integer.toString(getMsSleep()), DBCRAWL_ATTR_KEY_SPACE, keySpace);
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.writeSubTextNodeIfAny(DBCRAWL_NODE_NAME_CQL_QUERY, cqlQuery);
		xmlWriter.endElement();
	}

	/**
	 * @return the KeySpace
	 */
	public String getKeySpace() {
		return keySpace;
	}

	/**
	 * @param keySpace the KeySpace
	 */
	public void setKeySpace(String keySpace) {
		this.keySpace = keySpace;
	}

	/**
	 * @return the CQL query
	 */
	public String getCqlQuery() {
		return cqlQuery;
	}

	/**
	 * @param cqlQuery the CQL query to set
	 */
	public void setCqlQuery(String cqlQuery) {
		this.cqlQuery = cqlQuery;
	}

	CassandraCluster getCluster() throws URISyntaxException, UnknownHostException {
		return new CassandraCluster(getFinalUser(), getFinalPassword(),
				Arrays.asList(Objects.requireNonNull(getUrl(), "The host:port is missing")), 60000, 300000, null, null);
	}

	public String test() throws Exception {
		final StringBuilder sb = new StringBuilder();
		try (final CassandraCluster cluster = getCluster()) {
			try (final CassandraSession session = StringUtils.isBlank(keySpace) ?
					cluster.getSession() :
					cluster.getSession(keySpace)) {
				sb.append("Connection established.");
				if (!StringUtils.isBlank(cqlQuery)) {
					sb.append(StringUtils.LF);
					new TestConsumer(session, getBufferSize(), sb).execute(cqlQuery);
				}
			}
		}
		return sb.toString();
	}

	class TestConsumer extends ResultSetConsumer {

		final StringBuilder sb;

		TestConsumer(CassandraSession session, int bufferSize, StringBuilder sb) {
			super(session, bufferSize);
			this.sb = sb;
		}

		@Override
		boolean abort() {
			return true;
		}

		@Override
		void index(Map<Row, ColumnDefinitions> rows) {
			rows.forEach((row, columnDefinitions) -> columnDefinitions.forEach(columnDefinition -> {
				sb.append(columnDefinition.getName());
				sb.append(" -> ");
				sb.append(columnDefinition.getType().getName());
				sb.append(StringUtils.LF);
			}));
		}
	}

	static abstract class ResultSetConsumer {

		final CassandraSession session;
		final int bufferSize;

		ResultSetConsumer(final CassandraSession session, int bufferSize) {
			this.session = session;
			this.bufferSize = bufferSize;
		}

		abstract void index(Map<Row, ColumnDefinitions> rows) throws Exception;

		abstract boolean abort();

		void execute(final String query) throws Exception {
			LinkedHashMap<Row, ColumnDefinitions> rowStack = new LinkedHashMap<>();
			final ComplexQuery complexQuery = ObjectMappers.JSON.readValue(query, ComplexQuery.class);
			execute(null, complexQuery, rowStack);
		}

		private void execute(final Object joinColumnValue, final ComplexQuery complexQuery,
				final LinkedHashMap<Row, ColumnDefinitions> rowStack) throws Exception {

			final ResultSet resultSet = joinColumnValue == null || StringUtils.isBlank(joinColumnValue.toString()) ?
					session.executeWithFetchSize(complexQuery.cql, bufferSize) :
					session.executeWithFetchSize(complexQuery.cql, bufferSize, joinColumnValue);
			if (resultSet == null)
				return;
			final ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
			for (final Row row : resultSet) {
				rowStack.put(row, columnDefinitions);
				if (complexQuery.join != null) {
					FunctionUtils.forEachEx(complexQuery.join, (column, queries) -> {
						final Object columnValue = row.getObject(column);
						if (queries != null)
							for (ComplexQuery query : queries)
								execute(columnValue, query, rowStack);
					});
				}
				if (complexQuery.index != null && complexQuery.index)
					index(rowStack);
				rowStack.remove(row);
				if (abort())
					break;
			}
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ComplexQuery {

		public final String cql;
		public final Map<String, List<ComplexQuery>> join;
		public final Boolean index;

		@JsonCreator
		ComplexQuery(@JsonProperty("cql") final String cql,
				@JsonProperty("join") final Map<String, List<ComplexQuery>> join,
				@JsonProperty("index") final Boolean index) {
			this.cql = cql;
			this.join = join;
			this.index = index;
		}

	}

}
