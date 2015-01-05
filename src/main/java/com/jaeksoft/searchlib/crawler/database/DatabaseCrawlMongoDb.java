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

import java.net.URI;
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
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class DatabaseCrawlMongoDb extends DatabaseCrawlAbstract {

	private String databaseName;
	private String collectionName;
	private String collectionQuery;

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, String name) {
		super(crawlMaster, propertyManager, name);
		databaseName = null;
		collectionName = null;
		collectionQuery = null;
	}

	public void applyVariables(Variables variables) {
		if (variables == null)
			return;
		databaseName = variables.replace(databaseName);
		collectionName = variables.replace(collectionName);
		collectionQuery = variables.replace(collectionQuery);
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
		crawl.collectionQuery = this.collectionQuery;
	}

	@Override
	public DatabaseCrawlEnum getType() {
		return DatabaseCrawlEnum.DB_MONGO_DB;
	}

	protected final static String DBCRAWL_ATTR_DB_NAME = "databaseName";
	protected final static String DBCRAWL_ATTR_COLLECTION_NAME = "collectionName";
	protected final static String DBCRAWL_NODE_NAME_COLLECTION_QUERY = "collectionQuery";

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster,
			DatabasePropertyManager propertyManager, XPathParser xpp, Node item)
			throws XPathExpressionException {
		super(crawlMaster, propertyManager, xpp, item);
		setDatabaseName(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DB_NAME));
		setCollectionName(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_COLLECTION_NAME));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_COLLECTION_QUERY);
		if (sqlNode != null)
			setCollectionQuery(xpp.getNodeString(sqlNode, true));
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
				getDatabaseName());
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		// Collection query
		if (!StringUtils.isEmpty(getCollectionQuery())) {
			xmlWriter.startElement(DBCRAWL_NODE_NAME_COLLECTION_QUERY);
			xmlWriter.textNode(getCollectionQuery());
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
	 * @return the collectionQuery
	 */
	public String getCollectionQuery() {
		return collectionQuery;
	}

	/**
	 * @param collectionQuery
	 *            the collectionQuery to set
	 */
	public void setCollectionQuery(String collectionQuery) {
		this.collectionQuery = collectionQuery;
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

	@Override
	public String test() throws Exception {
		String user = getUser();
		String password = getPassword();
		URI uri = new URI(getUrl());
		StringBuilder sb = new StringBuilder();
		if (!"mongodb".equals(uri.getScheme()))
			throw new SearchLibException("Wrong scheme: " + uri.getScheme()
					+ ". The URL should start with: mongodb://");
		MongoCredential credential = null;
		MongoClient mongoClient = null;
		try {
			if (!StringUtils.isEmpty(user) && !StringUtils.isEmpty(password)) {
				credential = MongoCredential.createMongoCRCredential(user,
						databaseName, password.toCharArray());
				mongoClient = new MongoClient(new ServerAddress(uri.getHost(),
						uri.getPort()), Arrays.asList(credential));
			} else
				mongoClient = new MongoClient(new ServerAddress(uri.getHost(),
						uri.getPort()));
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
					if (!StringUtils.isEmpty(collectionQuery)) {
						DBObject queryObject = (DBObject) JSON
								.parse(collectionQuery);
						dbCollection.count(queryObject);
						sb.append("Query returns "
								+ dbCollection.count(queryObject)
								+ " document(s).");
						sb.append(StringUtils.LF);
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
