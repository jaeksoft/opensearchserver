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

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class DatabaseCrawlAbstract
		extends
		FieldMapCrawlItem<DatabaseCrawlAbstract, DatabaseCrawlThread, DatabaseCrawlMaster> {

	private String name;

	private String url;

	private String user;

	private String password;

	private LanguageEnum lang;

	private int bufferSize;

	protected DatabaseCrawlAbstract(DatabaseCrawlMaster crawlMaster, String name) {
		super(crawlMaster, new DatabaseFieldMap());
		this.name = name;
		url = null;
		user = null;
		password = null;
		lang = LanguageEnum.UNDEFINED;
		bufferSize = 100;
	}

	protected DatabaseCrawlAbstract(DatabaseCrawlMaster crawlMaster) {
		this(crawlMaster, null);
	}

	protected DatabaseCrawlAbstract(DatabaseCrawlAbstract crawl) {
		super((DatabaseCrawlMaster) crawl.threadMaster, new DatabaseFieldMap());
		crawl.copyTo(this);
	}

	public abstract DatabaseCrawlAbstract duplicate();

	@Override
	public void copyTo(DatabaseCrawlAbstract crawl) {
		super.copyTo(crawl);
		crawl.setName(this.getName());
		crawl.url = this.url;
		crawl.user = this.user;
		crawl.password = this.password;
		crawl.lang = this.lang;
		crawl.bufferSize = this.bufferSize;
	}

	public abstract DatabaseCrawlEnum getType();

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
	@Override
	public DatabaseFieldMap getFieldMap() {
		return (DatabaseFieldMap) super.getFieldMap();
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
	protected final static String DBCRAWL_ATTR_TYPE = "type";
	protected final static String DBCRAWL_ATTR_NAME = "name";
	protected final static String DBCRAWL_ATTR_USER = "user";
	protected final static String DBCRAWL_ATTR_PASSWORD = "password";
	protected final static String DBCRAWL_ATTR_URL = "url";
	protected final static String DBCRAWL_ATTR_LANG = "lang";
	protected final static String DBCRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String DBCRAWL_NODE_NAME_MAP = "map";

	protected DatabaseCrawlAbstract(DatabaseCrawlMaster crawlMaster,
			XPathParser xpp, Node item) throws XPathExpressionException {
		this(crawlMaster);
		setName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_NAME));
		setUser(XPathParser.getAttributeString(item, DBCRAWL_ATTR_USER));
		setPassword(XPathParser.getAttributeString(item, DBCRAWL_ATTR_PASSWORD));
		setUrl(XPathParser.getAttributeString(item, DBCRAWL_ATTR_URL));
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_LANG)));
		setBufferSize(XPathParser.getAttributeValue(item,
				DBCRAWL_ATTR_BUFFER_SIZE));
		Node mapNode = xpp.getNode(item, DBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			getFieldMap().load(xpp, mapNode);
	}

	public abstract void writeXml(XmlWriter xmlWriter) throws SAXException;

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
	public int compareTo(DatabaseCrawlAbstract o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return getName();
	}

}
