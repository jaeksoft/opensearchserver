/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.UniqueNameItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawl extends UniqueNameItem<DatabaseCrawl> {

	private DatabaseCrawlMaster databaseCrawlMaster;

	private DatabaseCrawlThread lastCrawlThread;

	private String url;

	private String driverClass;

	private String user;

	private String password;

	private String sql;

	private LanguageEnum lang;

	private DatabaseFieldMap fieldMap;

	private String primaryKey;

	private int bufferSize;

	public DatabaseCrawl(DatabaseCrawlMaster databaseCrawlMaster, String name) {
		super(name);
		this.databaseCrawlMaster = databaseCrawlMaster;
		url = null;
		driverClass = null;
		user = null;
		password = null;
		sql = null;
		lang = LanguageEnum.UNDEFINED;
		fieldMap = new DatabaseFieldMap();
		lastCrawlThread = null;
		primaryKey = null;
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
		crawl.user = this.user;
		crawl.password = this.password;
		crawl.sql = this.sql;
		crawl.lang = this.lang;
		crawl.lastCrawlThread = this.lastCrawlThread;
		crawl.primaryKey = this.primaryKey;
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
	 *            the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
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
	protected final static String DBCRAWL_ATTR_PASSWORD = "password";
	protected final static String DBCRAWL_ATTR_URL = "url";
	protected final static String DBCRAWL_ATTR_LANG = "lang";
	protected final static String DBCRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String DBCRAWL_ATTR_PRIMARY_KEY = "primaryKey";
	protected final static String DBCRAWL_NODE_NAME_SQL = "sql";
	protected final static String DBCRAWL_NODE_NAME_MAP = "map";

	public static DatabaseCrawl fromXml(DatabaseCrawlMaster dcm,
			XPathParser xpp, Node item) throws XPathExpressionException {
		DatabaseCrawl crawl = new DatabaseCrawl(dcm);
		crawl.setName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_NAME));
		crawl.setDriverClass(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DRIVER_CLASS));
		crawl.setUser(XPathParser.getAttributeString(item, DBCRAWL_ATTR_USER));
		crawl.setPassword(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PASSWORD));
		crawl.setUrl(XPathParser.getAttributeString(item, DBCRAWL_ATTR_URL));
		crawl.setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				item, DBCRAWL_ATTR_LANG)));
		crawl.setPrimaryKey(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PRIMARY_KEY));
		crawl.setBufferSize(XPathParser.getAttributeValue(item,
				DBCRAWL_ATTR_BUFFER_SIZE));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL);
		if (sqlNode != null)
			crawl.setSql(xpp.getNodeString(sqlNode, true));
		Node mapNode = xpp.getNode(item, DBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			crawl.fieldMap.load(xpp, mapNode);
		return crawl;
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(),
				DBCRAWL_ATTR_DRIVER_CLASS, getDriverClass(), DBCRAWL_ATTR_USER,
				getUser(), DBCRAWL_ATTR_PASSWORD, getPassword(),
				DBCRAWL_ATTR_URL, getUrl(), DBCRAWL_ATTR_LANG, getLang()
						.getCode(), DBCRAWL_ATTR_PRIMARY_KEY, primaryKey,
				DBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(bufferSize));
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		fieldMap.store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.startElement(DBCRAWL_NODE_NAME_SQL);
		xmlWriter.textNode(getSql());
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

}
