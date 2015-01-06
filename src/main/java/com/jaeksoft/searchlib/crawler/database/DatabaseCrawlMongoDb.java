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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class DatabaseCrawlMongoDb extends DatabaseCrawlAbstract {

	private String databaseName;
	private String collectionName;
	private String refQuery;
	private String keyQuery;

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, String name) {
		super(crawlMaster, propertyManager, name);
		databaseName = null;
		collectionName = null;
		refQuery = null;
		keyQuery = null;
	}

	public void applyVariables(Variables variables) {
		if (variables == null)
			return;
		databaseName = variables.replace(databaseName);
		collectionName = variables.replace(collectionName);
		refQuery = variables.replace(refQuery);
		keyQuery = variables.replace(keyQuery);
	}

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager) {
		this(crawlMaster, propertyManager, null);
	}

	protected DatabaseCrawlMongoDb(DatabaseCrawlMongoDb crawl) {
		super((DatabaseCrawlMaster) crawl.threadMaster, crawl.propertyManager);
		crawl.copyTo(this);
	}

	@Override
	public DatabaseCrawlAbstract duplicate() {
		return new DatabaseCrawlMongoDb(this);
	}

	@Override
	public void copyTo(DatabaseCrawlAbstract crawlAbstract) {
		super.copyTo(crawlAbstract);
		DatabaseCrawlMongoDb crawl = (DatabaseCrawlMongoDb) crawlAbstract;
		crawl.databaseName = this.databaseName;
		crawl.collectionName = this.collectionName;
		crawl.refQuery = this.refQuery;
		crawl.keyQuery = this.keyQuery;
	}

	@Override
	public DatabaseCrawlEnum getType() {
		return DatabaseCrawlEnum.DB_MONGO_DB;
	}

	protected final static String DBCRAWL_ATTR_DB_NAME = "databaseName";
	protected final static String DBCRAWL_ATTR_COLLECTION_NAME = "collectionName";
	protected final static String DBCRAWL_NODE_NAME_REF_QUERY = "refQuery";
	protected final static String DBCRAWL_NODE_NAME_KEY_QUERY = "keyQuery";

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, XPathParser xpp, Node item)
			throws XPathExpressionException {
		super(crawlMaster, propertyManager, xpp, item);
		setDatabaseName(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DB_NAME));
		setCollectionName(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_COLLECTION_NAME));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_REF_QUERY);
		if (sqlNode != null)
			setRefQuery(xpp.getNodeString(sqlNode, true));
		sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_KEY_QUERY);
		if (sqlNode != null)
			setKeyQuery(xpp.getNodeString(sqlNode, true));
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(),
				DBCRAWL_ATTR_TYPE, getType().name(), DBCRAWL_ATTR_USER,
				getUser(), DBCRAWL_ATTR_PASSWORD, getPassword(),
				DBCRAWL_ATTR_URL, getUrl(), DBCRAWL_ATTR_LANG, getLang()
						.getCode(), DBCRAWL_ATTR_BUFFER_SIZE, Integer
						.toString(getBufferSize()), DBCRAWL_ATTR_MSSLEEP,
				Integer.toString(getMsSleep()), DBCRAWL_ATTR_DB_NAME,
				getDatabaseName(), DBCRAWL_ATTR_COLLECTION_NAME,
				getCollectionName());
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		// Ref query
		if (!StringUtils.isEmpty(getRefQuery())) {
			xmlWriter.startElement(DBCRAWL_NODE_NAME_REF_QUERY);
			xmlWriter.textNode(getRefQuery());
			xmlWriter.endElement();
		}
		// Key query
		if (!StringUtils.isEmpty(getKeyQuery())) {
			xmlWriter.startElement(DBCRAWL_NODE_NAME_KEY_QUERY);
			xmlWriter.textNode(getKeyQuery());
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName
	 *            the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @return the refQuery
	 */
	public String getRefQuery() {
		return refQuery;
	}

	/**
	 * @param refQuery
	 *            the refQuery to set
	 */
	public void setRefQuery(String refQuery) {
		this.refQuery = refQuery;
	}

	/**
	 * @return the keyQuery
	 */
	public String getKeyQuery() {
		return keyQuery;
	}

	/**
	 * @param keyQuery
	 *            the keyQuery to set
	 */
	public void setKeyQuery(String keyQuery) {
		this.keyQuery = keyQuery;
	}

	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @param collectionName
	 *            the collectionName to set
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	MongoClient getMongoClient() throws URISyntaxException,
			UnknownHostException {
		String user = getUser();
		String password = getPassword();
		URI uri = new URI(getUrl());
		MongoCredential credential = null;
		if (!StringUtils.isEmpty(user) && !StringUtils.isEmpty(password)) {
			credential = MongoCredential.createMongoCRCredential(user,
					databaseName, password.toCharArray());
			return new MongoClient(new ServerAddress(uri.getHost(),
					uri.getPort()), Arrays.asList(credential));
		}
		return new MongoClient(new ServerAddress(uri.getHost(), uri.getPort()));
	}

	DBCollection getCollection(MongoClient mongoClient) throws IOException {
		if (StringUtils.isEmpty(databaseName))
			throw new IOException("No database name.");
		DB db = mongoClient.getDB(databaseName);
		if (StringUtils.isEmpty(collectionName))
			throw new IOException("No collection name.");
		return db.getCollection(collectionName);
	}

	DBObject getRefObject() {
		if (StringUtils.isEmpty(refQuery))
			return null;
		return (DBObject) JSON.parse(refQuery);
	}

	DBObject getKeyObject() {
		if (StringUtils.isEmpty(keyQuery))
			return null;
		return (DBObject) JSON.parse(keyQuery);
	}

	@Override
	public String test() throws Exception {
		URI uri = new URI(getUrl());
		StringBuilder sb = new StringBuilder();
		if (!"mongodb".equals(uri.getScheme()))
			throw new SearchLibException("Wrong scheme: " + uri.getScheme()
					+ ". The URL should start with: mongodb://");
		MongoClient mongoClient = null;
		try {
			mongoClient = getMongoClient();
			sb.append("Connection established.");
			sb.append(StringUtils.LF);
			if (!StringUtils.isEmpty(databaseName)) {
				DB db = mongoClient.getDB(databaseName);
				if (db == null)
					throw new SearchLibException("Database not found: "
							+ databaseName);
				Set<String> collections = db.getCollectionNames();
				if (collections == null)
					throw new SearchLibException("No collection found.");
				sb.append("Collections found:");
				sb.append(StringUtils.LF);
				for (String collection : collections) {
					sb.append(collection);
					sb.append(StringUtils.LF);
				}
				if (!StringUtils.isEmpty(collectionName)) {
					DBCollection dbCollection = db
							.getCollection(collectionName);
					if (dbCollection == null)
						throw new SearchLibException("Collection "
								+ collectionName + " not found.");
					sb.append("Collection " + collectionName + " contains "
							+ dbCollection.count() + " document(s).");
					sb.append(StringUtils.LF);
					if (!StringUtils.isEmpty(refQuery)) {
						DBCursor cursor = dbCollection.find(getRefObject(),
								getKeyObject());
						try {
							sb.append("Query returns " + cursor.count()
									+ " document(s).");
							sb.append(StringUtils.LF);
						} finally {
							cursor.close();
						}
					}
				}
			}
		} finally {
			if (mongoClient != null)
				mongoClient.close();
		}
		return sb.toString();
	}

}
