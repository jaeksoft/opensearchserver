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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.UniqueNameItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ReplicationItem extends UniqueNameItem<ReplicationItem> {

	private ReadWriteLock rwl = new ReadWriteLock();

	private URL instanceUrl = null;

	private String cachedUrl = null;

	private String login = null;

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
		rwl.r.lock();
		try {
			String encodedApiKey = (apiKey != null && apiKey.length() > 0) ? new String(
					Base64.encodeBase64(apiKey.getBytes())) : "";
			xmlWriter.startElement("replicationItem", "instanceUrl",
					instanceUrl.toExternalForm(), "indexName", indexName,
					"apiKey", encodedApiKey);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param instanceUrl
	 *            the instanceUrl to set
	 */
	public void setInstanceUrl(URL instanceUrl) {
		rwl.w.lock();
		try {
			this.instanceUrl = instanceUrl;
			updateName();
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the instanceUrl
	 */
	public URL getInstanceUrl() {
		rwl.r.lock();
		try {
			return instanceUrl;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param indexName
	 *            the indexName to set
	 */
	public void setIndexName(String indexName) {
		rwl.w.lock();
		try {
			this.indexName = indexName;
			updateName();
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		rwl.r.lock();
		try {
			return indexName;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		rwl.w.lock();
		try {
			this.apiKey = apiKey;
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		rwl.r.lock();
		try {
			return apiKey;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(String login) {
		rwl.w.lock();
		try {
			this.login = login;
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		rwl.r.lock();
		try {
			return login;
		} finally {
			rwl.r.unlock();
		}
	}

	public void copy(ReplicationItem item) {
		rwl.w.lock();
		try {
			this.indexName = item.indexName;
			this.instanceUrl = item.instanceUrl;
			this.apiKey = item.apiKey;
			this.lastReplicationThread = item.lastReplicationThread;
			this.replicationMaster = item.replicationMaster;
			setName(item.getName());
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	protected void setReplicationThread(ReplicationThread replicationThread) {
		rwl.w.lock();
		try {
			this.lastReplicationThread = replicationThread;
		} finally {
			rwl.w.unlock();
		}
	}

	public ReplicationThread getLastReplicationThread() {
		rwl.r.lock();
		try {
			return lastReplicationThread;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isReplicationThread() {
		rwl.r.lock();
		try {
			return replicationMaster.isReplicationThread(this);
		} finally {
			rwl.r.unlock();
		}
	}

	private String getPushTargetUrl() throws UnsupportedEncodingException {
		rwl.r.lock();
		try {
			if (cachedUrl != null)
				return cachedUrl;
			String url = getInstanceUrl().toExternalForm();
			cachedUrl = url + (url.endsWith("/") ? "" : '/') + "push?use="
					+ URLEncoder.encode(getIndexName(), "UTF-8");

			String login = getLogin();
			String apiKey = getApiKey();
			if (login != null && login.length() > 0 && apiKey != null
					&& apiKey.length() > 0)
				cachedUrl += "&login=" + URLEncoder.encode(login, "UTF-8")
						+ "&key=" + apiKey;
			return cachedUrl;
		} finally {
			rwl.r.unlock();
		}
	}

	protected String getPushTargetUrl(Client client, File sourceFile)
			throws UnsupportedEncodingException, SearchLibException {
		String dataPath = client.getIndexDirectory().getAbsolutePath();
		String filePath = sourceFile.getAbsolutePath();
		if (!filePath.startsWith(dataPath))
			throw new SearchLibException("Bad file path " + filePath);
		filePath = filePath.substring(dataPath.length());
		return getPushTargetUrl() + "&filePath="
				+ URLEncoder.encode(filePath, "UTF-8")
				+ (sourceFile.isDirectory() ? "&type=dir" : "");
	}

	protected String getPushTargetUrl(Client client, String cmd)
			throws UnsupportedEncodingException {
		return getPushTargetUrl() + "&cmd=" + URLEncoder.encode(cmd, "UTF-8");
	}

}
