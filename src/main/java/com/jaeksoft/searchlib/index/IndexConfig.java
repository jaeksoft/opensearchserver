/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Similarity;
import org.json.JSONException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexConfig {

	private final AtomicInteger searchCache;

	private final AtomicInteger filterCache;

	private final AtomicInteger fieldCache;

	private final AtomicInteger termVectorCache;

	private volatile URI remoteURI;

	private volatile String keyField;

	private volatile String keyMd5RegExp;

	private volatile String similarityClass;

	private final AtomicInteger maxNumSegments;

	private final AtomicLong writeLockTimeout;

	private final ConcurrentSkipListSet<String> indexSet;

	public IndexConfig(Node node) throws URISyntaxException {
		searchCache = new AtomicInteger(XPathParser.getAttributeValue(node,
				"searchCache"));
		filterCache = new AtomicInteger(XPathParser.getAttributeValue(node,
				"filterCache"));
		int fc = XPathParser.getAttributeValue(node, "fieldCache");
		if (fc == 0)
			fc = XPathParser.getAttributeValue(node, "documentCache");
		fieldCache = new AtomicInteger(fc);
		termVectorCache = new AtomicInteger(XPathParser.getAttributeValue(node,
				"termVectorCache"));
		String s = XPathParser.getAttributeString(node, "remoteURI");
		remoteURI = StringUtils.isEmpty(s) ? null : new URI(s);
		keyField = XPathParser.getAttributeString(node, "keyField");
		keyMd5RegExp = XPathParser.getAttributeString(node, "keyMd5RegExp");
		setSimilarityClass(XPathParser.getAttributeString(node,
				"similarityClass"));
		int mns = XPathParser.getAttributeValue(node, "maxNumSegments");
		if (mns == 0)
			mns = 1;
		maxNumSegments = new AtomicInteger(mns);
		long wlt = XPathParser.getAttributeValue(node, "writeLockTimeout");
		if (wlt == 0)
			wlt = IndexWriterConfig.getDefaultWriteLockTimeout();
		writeLockTimeout = new AtomicLong(wlt);
		Node indicesNode = DomUtils.getFirstNode(node, "indices");
		if (indicesNode != null) {
			indexSet = new ConcurrentSkipListSet<String>();
			List<Node> indexNodes = DomUtils.getNodes(indicesNode, "index");
			if (indexNodes != null)
				for (Node indexNode : indexNodes)
					addIndex(indexNode.getTextContent());
		} else
			indexSet = null;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("index", "searchCache",
				Integer.toString(searchCache.get()), "filterCache",
				Integer.toString(filterCache.get()), "fieldCache",
				Integer.toString(fieldCache.get()), "termVectorCache",
				Integer.toString(termVectorCache.get()), "remoteURI",
				remoteURI != null ? remoteURI.toString() : null, "keyField",
				keyField, "keyMd5RegExp", keyMd5RegExp, "similarityClass",
				similarityClass, "maxNumSegments",
				Integer.toString(maxNumSegments.get()), "writeLockTimeout",
				Long.toString(writeLockTimeout.get()));
		if (indexSet != null) {
			xmlWriter.startElement("indices");
			for (String index : indexSet) {
				xmlWriter.startElement("index");
				xmlWriter.textNode(index);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}

	/**
	 * Add a new index in the index set
	 * 
	 * @param indexName
	 */
	public void addIndex(String indexName) {
		if (indexName == null || indexName.length() == 0)
			return;
		indexSet.add(indexName);
	}

	/**
	 * Remove an index from the index set
	 * 
	 * @param indexName
	 */
	public void removeIndex(String indexName) {
		if (indexName == null || indexName.length() == 0)
			return;
		indexSet.remove(indexName);
	}

	public boolean isMulti() {
		return indexSet != null;
	}

	/**
	 * @return the index list
	 */
	public List<String> getIndexList() {
		if (indexSet == null)
			return null;
		List<String> list = new ArrayList<String>(indexSet.size());
		for (String indexName : indexSet)
			list.add(indexName);
		return list;
	}

	public boolean isIndexMulti(String index) {
		return indexSet != null && indexSet.contains(index);
	}

	/**
	 * @return the searchCache
	 */
	public int getSearchCache() {
		return searchCache.get();
	}

	/**
	 * @param searchCache
	 *            the searchCache to set
	 */
	public void setSearchCache(int searchCache) {
		this.searchCache.set(searchCache);
	}

	/**
	 * @return the filterCache
	 */
	public int getFilterCache() {
		return filterCache.get();
	}

	/**
	 * @param filterCache
	 *            the filterCache to set
	 */
	public void setFilterCache(int filterCache) {
		this.filterCache.set(filterCache);
	}

	/**
	 * @return the documentCache
	 */
	public int getFieldCache() {
		return fieldCache.get();
	}

	/**
	 * @param documentCache
	 *            the documentCache to set
	 */
	public void setFieldCache(int fieldCache) {
		this.fieldCache.set(fieldCache);
	}

	/**
	 * @return the termVectorCache
	 */
	public int getTermVectorCache() {
		return termVectorCache.get();
	}

	/**
	 * @param termVectorCache
	 *            the termVectorCache to set
	 */
	public void setTermVectorCache(int termVectorCache) {
		this.termVectorCache.set(termVectorCache);
	}

	/**
	 * @return the remoteURI
	 */
	public URI getRemoteURI() {
		return remoteURI;
	}

	/**
	 * @param remoteURI
	 *            the remoteURI to set
	 */
	public void setRemoteURI(URI remoteURI) {
		this.remoteURI = remoteURI;
	}

	/**
	 * @return the keyField
	 */
	public String getKeyField() {
		return keyField;
	}

	/**
	 * @param keyField
	 *            the keyField to set
	 */
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}

	/**
	 * @return the keyMd5RegExp
	 */
	public String getKeyMd5RegExp() {
		return keyMd5RegExp;
	}

	/**
	 * @param keyMd5RegExp
	 *            the keyMd5RegExp to set
	 */
	public void setKeyMd5RegExp(String keyMd5RegExp) {
		this.keyMd5RegExp = keyMd5RegExp;
	}

	/**
	 * @param similarityClass
	 *            the similarityClass to set
	 */
	public void setSimilarityClass(String similarityClass) {
		this.similarityClass = similarityClass;
	}

	/**
	 * @return the similarityClass
	 */
	public String getSimilarityClass() {
		return similarityClass;
	}

	/**
	 * @return the maxNumSegments
	 */
	public int getMaxNumSegments() {
		return maxNumSegments.get();
	}

	/**
	 * @param maxNumSegments
	 *            the maxNumSegments to set
	 */
	public void setMaxNumSegments(int maxNumSegments) {
		this.maxNumSegments.set(maxNumSegments);
	}

	public Similarity getNewSimilarityInstance() throws SearchLibException {
		if (similarityClass == null)
			return null;
		try {
			return (Similarity) Class.forName(similarityClass).newInstance();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	public IndexAbstract getNewIndex(File configDir,
			boolean createIndexIfNotExists) throws IOException,
			URISyntaxException, SearchLibException, JSONException {
		return new IndexSingle(configDir, this, createIndexIfNotExists);
	}

	public long getWriteLockTimeout() {
		return writeLockTimeout.get();
	}

	/**
	 * @param writeLockTimeout
	 *            the writeLockTimeout to set
	 */
	public void setWriteLockTimeout(long writeLockTimeout) {
		this.writeLockTimeout.set(writeLockTimeout);
	}

}
