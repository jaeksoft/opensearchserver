/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.replication;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.UniqueNameItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ReplicationItem extends UniqueNameItem<ReplicationItem> {

	private URL instanceUrl = null;

	private String indexName = null;

	private String apiKey = null;

	private ReplicationThread lastReplicationThread;

	private ReplicationMaster replicationMaster;

	public ReplicationItem(ReplicationMaster replicationMaster) {
		super(null);
		this.replicationMaster = replicationMaster;
		lastReplicationThread = null;
	}

	public ReplicationItem(ReplicationItem item) {
		super(item.getName());
		this.copy(item);
	}

	public ReplicationItem(ReplicationMaster replicationMaster,
			XPathParser xpp, Node node) throws MalformedURLException {
		super(null);
		this.replicationMaster = replicationMaster;
		String url = XPathParser.getAttributeString(node, "instanceUrl");
		if (url != null && url.length() > 0)
			setInstanceUrl(new URL(url));
		setIndexName(XPathParser.getAttributeString(node, "indexName"));
		String encodedApiKey = XPathParser.getAttributeString(node, "apiKey");
		if (encodedApiKey != null && encodedApiKey.length() > 0)
			setApiKey(new String(Base64.decodeBase64(encodedApiKey.getBytes())));
		updateName();
	}

	private void updateName() {
		setName(getInstanceUrl().toExternalForm() + "/" + getIndexName());
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		synchronized (this) {
			String encodedApiKey = (apiKey != null && apiKey.length() > 0) ? new String(
					Base64.encodeBase64(apiKey.getBytes())) : "";
			xmlWriter.startElement("replicationItem", "instanceUrl",
					instanceUrl.toExternalForm(), "indexName", indexName,
					"apiKey", encodedApiKey);
			xmlWriter.endElement();
		}
	}

	/**
	 * @param instanceUrl
	 *            the instanceUrl to set
	 */
	public void setInstanceUrl(URL instanceUrl) {
		this.instanceUrl = instanceUrl;
		updateName();
	}

	/**
	 * @return the instanceUrl
	 */
	public URL getInstanceUrl() {
		return instanceUrl;
	}

	/**
	 * @param indexName
	 *            the indexName to set
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
		updateName();
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	public void copy(ReplicationItem item) {
		this.indexName = item.indexName;
		this.instanceUrl = item.instanceUrl;
		this.apiKey = item.apiKey;
		this.lastReplicationThread = item.lastReplicationThread;
		this.replicationMaster = item.replicationMaster;
		setName(item.getName());
	}

	protected void setReplicationThread(ReplicationThread replicationThread) {
		this.lastReplicationThread = replicationThread;
	}

	public ReplicationThread getLastReplicationThread() {
		return lastReplicationThread;
	}

	public boolean isReplicationThread() {
		return replicationMaster.isReplicationThread(this);
	}

}
