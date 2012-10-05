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

import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.helpers.DOMUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawl implements Comparable<DatabaseCrawl> {

	public static enum SqlUpdateMode {
		NO_CALL, ONE_CALL_PER_PRIMARY_KEY, PRIMARY_KEY_LIST, PRIMARY_KEY_CHAR_LIST;

		public static SqlUpdateMode find(String label) {
			for (SqlUpdateMode v : values())
				if (v.name().equals(label))
					return v;
			return NO_CALL;
		}
	}

	private String name;

	private DatabaseCrawlMaster databaseCrawlMaster;

	private DatabaseCrawlThread lastCrawlThread;

	private String url;

	private String driverClass;

	private IsolationLevelEnum isolationLevel;

	private String user;

	private String password;

	private String sqlSelect;

	private String sqlUpdate;

	private SqlUpdateMode sqlUpdateMode;

	private LanguageEnum lang;

	private DatabaseFieldMap fieldMap;

	private String primaryKey;

	private String uniqueKeyDeleteField;

	private int bufferSize;

	public DatabaseCrawl(DatabaseCrawlMaster databaseCrawlMaster, String name) {
		this.name = name;
		this.databaseCrawlMaster = databaseCrawlMaster;
		url = null;
		driverClass = null;
		isolationLevel = IsolationLevelEnum.TRANSACTION_NONE;
		user = null;
		password = null;
		sqlSelect = null;
		sqlUpdate = null;
		sqlUpdateMode = SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY;
		lang = LanguageEnum.UNDEFINED;
		fieldMap = new DatabaseFieldMap();
		lastCrawlThread = null;
		primaryKey = null;
		uniqueKeyDeleteField = null;
		bufferSize = 100;
	}

	public DatabaseCrawl(DatabaseCrawlMaster databaseCrawlMaster) {
		this(databaseCrawlMaster, (String) null);
	}

	public DatabaseCrawl(DatabaseCrawlMaster databaseCrawlMaster,
			DatabaseCrawl crawl) {
		this(databaseCrawlMaster);
		crawl.copyTo(this);
	}

	public void copyTo(DatabaseCrawl crawl) {
		crawl.setName(this.getName());
		crawl.url = this.url;
		crawl.driverClass = this.driverClass;
		crawl.isolationLevel = this.isolationLevel;
		crawl.user = this.user;
		crawl.password = this.password;
		crawl.sqlSelect = this.sqlSelect;
		crawl.sqlUpdate = this.sqlUpdate;
		crawl.sqlUpdateMode = this.sqlUpdateMode;
		crawl.lang = this.lang;
		crawl.lastCrawlThread = this.lastCrawlThread;
		crawl.primaryKey = this.primaryKey;
		crawl.uniqueKeyDeleteField = this.uniqueKeyDeleteField;
		crawl.bufferSize = this.bufferSize;
		this.fieldMap.copyTo(crawl.fieldMap);
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	/**
	 * @return the fieldMap
	 */
	public DatabaseFieldMap getFieldMap() {
		return fieldMap;
	}

	public boolean isCrawlThread() {
		return databaseCrawlMaster.isDatabaseCrawlThread(this);
	}

	public DatabaseCrawlThread getLastCrawlThread() {
		return lastCrawlThread;
	}

	public void setCrawlThread(DatabaseCrawlThread lastCrawlThread) {
		this.lastCrawlThread = lastCrawlThread;
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

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	protected final static String DBCRAWL_NODE_NAME = "databaseCrawl";
	protected final static String DBCRAWL_ATTR_NAME = "name";
	protected final static String DBCRAWL_ATTR_DRIVER_CLASS = "driverClass";
	protected final static String DBCRAWL_ATTR_USER = "user";
	protected final static String DBCRAWL_ATTR_ISOLATION_LEVEL = "isolationLevel";
	protected final static String DBCRAWL_ATTR_PASSWORD = "password";
	protected final static String DBCRAWL_ATTR_URL = "url";
	protected final static String DBCRAWL_ATTR_LANG = "lang";
	protected final static String DBCRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String DBCRAWL_ATTR_PRIMARY_KEY = "primaryKey";
	protected final static String DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD = "uniqueKeyDeleteField";
	protected final static String DBCRAWL_NODE_NAME_SQL_SELECT = "sql";
	protected final static String DBCRAWL_NODE_NAME_SQL_UPDATE = "sqlUpdate";
	protected final static String DBCRAWL_ATTR__NAME_SQL_UPDATE_MODE = "mode";
	protected final static String DBCRAWL_NODE_NAME_MAP = "map";

	public static DatabaseCrawl fromXml(DatabaseCrawlMaster dcm,
			XPathParser xpp, Node item) throws XPathExpressionException {
		DatabaseCrawl crawl = new DatabaseCrawl(dcm);
		crawl.setName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_NAME));
		crawl.setDriverClass(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DRIVER_CLASS));
		crawl.setIsolationLevel(IsolationLevelEnum.find(XPathParser
				.getAttributeString(item, DBCRAWL_ATTR_ISOLATION_LEVEL)));
		crawl.setUser(XPathParser.getAttributeString(item, DBCRAWL_ATTR_USER));
		crawl.setPassword(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PASSWORD));
		crawl.setUrl(XPathParser.getAttributeString(item, DBCRAWL_ATTR_URL));
		crawl.setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				item, DBCRAWL_ATTR_LANG)));
		crawl.setPrimaryKey(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PRIMARY_KEY));
		crawl.setUniqueKeyDeleteField(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD));
		crawl.setBufferSize(XPathParser.getAttributeValue(item,
				DBCRAWL_ATTR_BUFFER_SIZE));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL_SELECT);
		if (sqlNode != null)
			crawl.setSqlSelect(xpp.getNodeString(sqlNode, true));
		sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL_UPDATE);
		if (sqlNode != null) {
			crawl.setSqlUpdate(xpp.getNodeString(sqlNode, true));
			crawl.setSqlUpdateMode(SqlUpdateMode.find(DOMUtils.getAttribute(
					sqlNode, DBCRAWL_ATTR__NAME_SQL_UPDATE_MODE)));
		}
		Node mapNode = xpp.getNode(item, DBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			crawl.fieldMap.load(xpp, mapNode);
		return crawl;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(),
				DBCRAWL_ATTR_DRIVER_CLASS, getDriverClass(),
				DBCRAWL_ATTR_ISOLATION_LEVEL,
				isolationLevel != null ? isolationLevel.name() : null,
				DBCRAWL_ATTR_USER, getUser(), DBCRAWL_ATTR_PASSWORD,
				getPassword(), DBCRAWL_ATTR_URL, getUrl(), DBCRAWL_ATTR_LANG,
				getLang().getCode(), DBCRAWL_ATTR_PRIMARY_KEY, primaryKey,
				DBCRAWL_ATTR_UNIQUE_KEY_DELETE_FIELD, uniqueKeyDeleteField,
				DBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(bufferSize));
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		fieldMap.store(xmlWriter);
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(DatabaseCrawl o) {
		return getName().compareTo(o.getName());
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

}
