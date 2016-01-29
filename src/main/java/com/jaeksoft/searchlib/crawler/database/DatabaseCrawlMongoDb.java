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

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class DatabaseCrawlMongoDb extends DatabaseCrawlAbstract {

	private String databaseName;
	private String collectionName;
	private String criteria;
	private String projection;

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager, String name) {
		super(crawlMaster, propertyManager, name);
		databaseName = null;
		collectionName = null;
		criteria = null;
		projection = null;
	}

	public void applyVariables(Variables variables) {
		if (variables == null)
			return;
		databaseName = variables.replace(databaseName);
		collectionName = variables.replace(collectionName);
		criteria = variables.replace(criteria);
		projection = variables.replace(projection);
	}

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager) {
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
		crawl.criteria = this.criteria;
		crawl.projection = this.projection;
	}

	@Override
	public DatabaseCrawlEnum getType() {
		return DatabaseCrawlEnum.DB_MONGO_DB;
	}

	protected final static String DBCRAWL_ATTR_DB_NAME = "databaseName";
	protected final static String DBCRAWL_ATTR_COLLECTION_NAME = "collectionName";
	protected final static String DBCRAWL_NODE_NAME_CRITERIA = "criteria";
	protected final static String DBCRAWL_NODE_NAME_PROJECTION = "projection";

	public DatabaseCrawlMongoDb(DatabaseCrawlMaster crawlMaster, DatabasePropertyManager propertyManager,
			XPathParser xpp, Node item) throws XPathExpressionException {
		super(crawlMaster, propertyManager, xpp, item);
		setDatabaseName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_DB_NAME));
		setCollectionName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_COLLECTION_NAME));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_CRITERIA);
		if (sqlNode != null)
			setCriteria(xpp.getNodeString(sqlNode, true));
		sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_PROJECTION);
		if (sqlNode != null)
			setProjection(xpp.getNodeString(sqlNode, true));
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(), DBCRAWL_ATTR_TYPE, getType().name(),
				DBCRAWL_ATTR_USER, getUser(), DBCRAWL_ATTR_PASSWORD, getPassword(), DBCRAWL_ATTR_URL, getUrl(),
				DBCRAWL_ATTR_LANG, getLang().getCode(), DBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(getBufferSize()),
				DBCRAWL_ATTR_MSSLEEP, Integer.toString(getMsSleep()), DBCRAWL_ATTR_DB_NAME, getDatabaseName(),
				DBCRAWL_ATTR_COLLECTION_NAME, getCollectionName());
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		String criteria = getCriteria();
		if (!StringUtils.isEmpty(criteria)) {
			xmlWriter.startElement(DBCRAWL_NODE_NAME_CRITERIA);
			xmlWriter.textNode(criteria);
			xmlWriter.endElement();
		}
		String projection = getProjection();
		if (!StringUtils.isEmpty(projection)) {
			xmlWriter.startElement(DBCRAWL_NODE_NAME_PROJECTION);
			xmlWriter.textNode(projection);
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
	 * @return the criteria
	 */
	public String getCriteria() {
		return criteria;
	}

	/**
	 * @param criteria
	 *            the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return the projection
	 */
	public String getProjection() {
		return projection;
	}

	/**
	 * @param projection
	 *            the projection to set
	 */
	public void setProjection(String projection) {
		this.projection = projection;
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

	MongoClient getMongoClient() throws URISyntaxException, UnknownHostException {
		String user = getUser();
		String password = getPassword();
		URI uri = new URI(getUrl());
		MongoCredential credential = null;
		if (!StringUtils.isEmpty(user) && !StringUtils.isEmpty(password)) {
			credential = MongoCredential.createMongoCRCredential(user, databaseName, password.toCharArray());
			return new MongoClient(new ServerAddress(uri.getHost(), uri.getPort()), Arrays.asList(credential));
		}
		return new MongoClient(new ServerAddress(uri.getHost(), uri.getPort()));
	}

	MongoCollection<Document> getCollection(MongoClient mongoClient) throws IOException {
		if (StringUtils.isEmpty(databaseName))
			throw new IOException("No database name.");
		MongoDatabase db = mongoClient.getDatabase(databaseName);
		if (StringUtils.isEmpty(collectionName))
			throw new IOException("No collection name.");
		return db.getCollection(collectionName);
	}

	Document getCriteriaObject() {
		if (StringUtils.isEmpty(criteria))
			return null;
		return Document.parse(criteria);
	}

	Document getProjectionObject() {
		if (StringUtils.isEmpty(projection))
			return null;
		return Document.parse(projection);
	}

	@Override
	public String test() throws Exception {
		URI uri = new URI(getUrl());
		StringBuilder sb = new StringBuilder();
		if (!"mongodb".equals(uri.getScheme()))
			throw new SearchLibException(
					"Wrong scheme: " + uri.getScheme() + ". The URL should start with: mongodb://");
		MongoClient mongoClient = null;
		try {
			mongoClient = getMongoClient();
			sb.append("Connection established.");
			sb.append(StringUtils.LF);
			if (!StringUtils.isEmpty(databaseName)) {
				MongoDatabase db = mongoClient.getDatabase(databaseName);
				if (db == null)
					throw new SearchLibException("Database not found: " + databaseName);
				MongoIterable<String> collections = db.listCollectionNames();
				if (collections == null)
					throw new SearchLibException("No collection found.");
				sb.append("Collections found:");
				sb.append(StringUtils.LF);
				for (String collection : collections) {
					sb.append(collection);
					sb.append(StringUtils.LF);
				}
				if (!StringUtils.isEmpty(collectionName)) {
					MongoCollection<?> dbCollection = db.getCollection(collectionName);
					if (dbCollection == null)
						throw new SearchLibException("Collection " + collectionName + " not found.");
					sb.append("Collection " + collectionName + " contains " + dbCollection.count() + " document(s).");
					sb.append(StringUtils.LF);
					if (!StringUtils.isEmpty(criteria)) {
						long count = dbCollection.count(getCriteriaObject());
						sb.append("Query returns " + count + " document(s).");
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
