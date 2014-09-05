/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawlSql extends DatabaseCrawlAbstract {

	public static enum SqlUpdateMode {
		NO_CALL, ONE_CALL_PER_PRIMARY_KEY, PRIMARY_KEY_LIST, PRIMARY_KEY_CHAR_LIST;

		public static SqlUpdateMode find(String label) {
			for (SqlUpdateMode v : values())
				if (v.name().equals(label))
					return v;
			return NO_CALL;
		}

		public static String[] getNameList() {
			String[] list = new String[values().length];
			int i = 0;
			for (SqlUpdateMode item : values())
				list[i++] = item.name();
			return list;
		}
	}

	private String driverClass;

	private IsolationLevelEnum isolationLevel;

	private String sqlSelect;

	private String sqlUpdate;

	private SqlUpdateMode sqlUpdateMode;

	private String primaryKey;

	private String uniqueKeyDeleteField;

	public DatabaseCrawlSql(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, String name) {
		super(crawlMaster, propertyManager, name);
		driverClass = null;
		isolationLevel = IsolationLevelEnum.TRANSACTION_NONE;
		sqlSelect = null;
		sqlUpdate = null;
		sqlUpdateMode = SqlUpdateMode.NO_CALL;
		primaryKey = null;
		uniqueKeyDeleteField = null;
	}

	public void applyVariables(Variables variables) {
		if (variables == null)
			return;
		sqlSelect = variables.replace(sqlSelect);
		sqlUpdate = variables.replace(sqlUpdate);
	}

	public DatabaseCrawlSql(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager) {
		this(crawlMaster, propertyManager, null);
	}

	protected DatabaseCrawlSql(DatabaseCrawlSql crawl) {
		super((DatabaseCrawlMaster) crawl.threadMaster, crawl.propertyManager);
		crawl.copyTo(this);
	}

	@Override
	public DatabaseCrawlAbstract duplicate() {
		return new DatabaseCrawlSql(this);
	}

	@Override
	public void copyTo(DatabaseCrawlAbstract crawlAbstract) {
		super.copyTo(crawlAbstract);
		DatabaseCrawlSql crawl = (DatabaseCrawlSql) crawlAbstract;
		crawl.driverClass = this.driverClass;
		crawl.isolationLevel = this.isolationLevel;
		crawl.sqlSelect = this.sqlSelect;
		crawl.sqlUpdate = this.sqlUpdate;
		crawl.sqlUpdateMode = this.sqlUpdateMode;
		crawl.primaryKey = this.primaryKey;
		crawl.uniqueKeyDeleteField = this.uniqueKeyDeleteField;
	}

	@Override
	public DatabaseCrawlEnum getType() {
		return DatabaseCrawlEnum.DB_SQL;
	}

	/**
	 * @return the driverClass
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * @param driverClass
	 *            the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * @param sql
	 *            the sqlSelect to set
	 */
	public void setSqlSelect(String sql) {
		this.sqlSelect = sql;
	}

	/**
	 * @return the sqlUpdate
	 */
	public String getSqlSelect() {
		return sqlSelect;
	}

	/**
	 * @param sql
	 *            the sqlSelect to set
	 */
	public void setSqlUpdate(String sql) {
		this.sqlUpdate = sql;
	}

	/**
	 * @return the sqlUpdate
	 */
	public String getSqlUpdate() {
		return sqlUpdate;
	}

	/**
	 * @return the sqlUpdateMode
	 */
	public SqlUpdateMode getSqlUpdateMode() {
		return sqlUpdateMode;
	}

	/**
	 * @param sqlUpdateMode
	 *            the sqlUpdateMode to set
	 */
	public void setSqlUpdateMode(SqlUpdateMode sqlUpdateMode) {
		this.sqlUpdateMode = sqlUpdateMode;
	}

	/**
	 * @param primaryKey
	 *            the primaryKey to set
	 */
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return the primaryKey
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	protected final static String DBCRAWL_ATTR_DRIVER_CLASS = "driverClass";
	protected final static String DBCRAWL_ATTR_ISOLATION_LEVEL = "isolationLevel";
	protected final static String DBCRAWL_ATTR_PRIMARY_KEY = "primaryKey";
	protected final static String DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD = "uniqueKeyDeleteField";
	protected final static String DBCRAWL_NODE_NAME_SQL_SELECT = "sql";
	protected final static String DBCRAWL_NODE_NAME_SQL_UPDATE = "sqlUpdate";
	protected final static String DBCRAWL_ATTR__NAME_SQL_UPDATE_MODE = "mode";

	public DatabaseCrawlSql(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, XPathParser xpp, Node item)
			throws XPathExpressionException {
		super(crawlMaster, propertyManager, xpp, item);
		setDriverClass(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DRIVER_CLASS));
		setIsolationLevel(IsolationLevelEnum.find(XPathParser
				.getAttributeString(item, DBCRAWL_ATTR_ISOLATION_LEVEL)));
		setPrimaryKey(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PRIMARY_KEY));
		setUniqueKeyDeleteField(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD));
		setBufferSize(XPathParser.getAttributeValue(item,
				DBCRAWL_ATTR_BUFFER_SIZE));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL_SELECT);
		if (sqlNode != null)
			setSqlSelect(xpp.getNodeString(sqlNode, true));
		sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL_UPDATE);
		if (sqlNode != null) {
			setSqlUpdate(xpp.getNodeString(sqlNode, true));
			setSqlUpdateMode(SqlUpdateMode.find(DomUtils.getAttributeText(
					sqlNode, DBCRAWL_ATTR__NAME_SQL_UPDATE_MODE)));
		}
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(),
				DBCRAWL_ATTR_TYPE, getType().name(), DBCRAWL_ATTR_DRIVER_CLASS,
				getDriverClass(), DBCRAWL_ATTR_ISOLATION_LEVEL,
				isolationLevel != null ? isolationLevel.name() : null,
				DBCRAWL_ATTR_USER, getUser(), DBCRAWL_ATTR_PASSWORD,
				getPassword(), DBCRAWL_ATTR_URL, getUrl(), DBCRAWL_ATTR_LANG,
				getLang().getCode(), DBCRAWL_ATTR_PRIMARY_KEY, primaryKey,
				DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD, uniqueKeyDeleteField,
				DBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(getBufferSize()),
				DBCRAWL_ATTR_MSSLEEP, Integer.toString(getMsSleep()));
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		// SQL Select Node
		xmlWriter.startElement(DBCRAWL_NODE_NAME_SQL_SELECT);
		xmlWriter.textNode(getSqlSelect());
		xmlWriter.endElement();
		// SQL Update Node
		xmlWriter.startElement(DBCRAWL_NODE_NAME_SQL_UPDATE,
				DBCRAWL_ATTR__NAME_SQL_UPDATE_MODE, getSqlUpdateMode().name());
		xmlWriter.textNode(getSqlUpdate());
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

	/**
	 * @return the isolationLevel
	 */
	public IsolationLevelEnum getIsolationLevel() {
		return isolationLevel;
	}

	/**
	 * @param isolationLevel
	 *            the isolationLevel to set
	 */
	public void setIsolationLevel(IsolationLevelEnum isolationLevel) {
		this.isolationLevel = isolationLevel;
	}

	/**
	 * @return the uniqueKeyDeleteField
	 */
	public String getUniqueKeyDeleteField() {
		return uniqueKeyDeleteField;
	}

	/**
	 * @param uniqueKeyDeleteField
	 *            the uniqueKeyDeleteField to set
	 */
	public void setUniqueKeyDeleteField(String uniqueKeyDeleteField) {
		this.uniqueKeyDeleteField = uniqueKeyDeleteField;
	}

	public JDBCConnection getNewJdbcConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		JDBCConnection jdbcCnx = new JDBCConnection();
		jdbcCnx.setDriver(driverClass);
		jdbcCnx.setUrl(getUrl());
		String user = getFinalUser();
		if (!StringUtils.isEmpty(user))
			jdbcCnx.setUsername(user);
		String password = getFinalPassword();
		if (!StringUtils.isEmpty(password))
			jdbcCnx.setPassword(password);
		return jdbcCnx;
	}

	public Transaction getNewTransaction(JDBCConnection jdbcCnx)
			throws SQLException {
		return jdbcCnx.getNewTransaction(false, isolationLevel.value);
	}

	public String checkSqlSelect() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		JDBCConnection jdbcCnx = getNewJdbcConnection();
		Transaction transaction = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			transaction = getNewTransaction(jdbcCnx);
			Query query = transaction.prepare(sqlSelect);
			ResultSet resultSet = query.getResultSet();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			pw.print("Found ");
			pw.print(columnCount);
			pw.println(" column(s)");
			for (int i = 1; i <= columnCount; i++) {
				pw.print(i);
				pw.print(": ");
				pw.println(metaData.getColumnLabel(i));
			}
			return sw.toString();
		} finally {
			IOUtils.close(pw, sw);
			if (transaction != null)
				transaction.close();
		}
	}

}
