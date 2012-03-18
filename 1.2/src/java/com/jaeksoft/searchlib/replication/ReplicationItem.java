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

package com.jaeksoft.searchlib.replication;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.UniqueNameItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.PushServlet;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ReplicationItem extends UniqueNameItem<ReplicationItem> {

	private ReadWriteLock rwl = new ReadWriteLock();

	private URL instanceUrl = null;

	private String cachedUrl = null;

	private String login = null;

	private String indexName = null;

	private String apiKey = null;

	private ReplicationThread lastReplicationThread;

	private ReplicationMaster replicationMaster;

	public ReplicationItem(ReplicationMaster replicationMaster, String name) {
		super(name);
		this.replicationMaster = replicationMaster;
		lastReplicationThread = null;
	}

	public ReplicationItem(ReplicationMaster replicationMaster) {
		this(replicationMaster, null);
	}

	public ReplicationItem() {
		this(null, null);
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
		setLogin(XPathParser.getAttributeString(node, "login"));
		String encodedApiKey = XPathParser.getAttributeString(node, "apiKey");
		if (encodedApiKey != null && encodedApiKey.length() > 0)
			setApiKey(StringUtils.base64decode(encodedApiKey));
		updateName();
	}

	private void updateName() throws MalformedURLException {
		String u = getInstanceUrl().toExternalForm();
		if (!u.endsWith("/"))
			u += '/';
		u += getIndexName();
		setName(u);
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			String encodedApiKey = (apiKey != null && apiKey.length() > 0) ? new String(
					StringUtils.base64encode(apiKey)) : "";
			xmlWriter.startElement("replicationItem", "instanceUrl",
					instanceUrl.toExternalForm(), "indexName", indexName,
					"login", login, "apiKey", encodedApiKey);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param instanceUrl
	 *            the instanceUrl to set
	 * @throws MalformedURLException
	 */
	public void setInstanceUrl(URL instanceUrl) throws MalformedURLException {
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
	 * @throws MalformedURLException
	 */
	public URL getInstanceUrl() throws MalformedURLException {
		rwl.r.lock();
		try {
			if (instanceUrl != null)
				return instanceUrl;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (instanceUrl != null)
				return instanceUrl;
			instanceUrl = new URL(CommonController.getBaseUrl().toString());
			return instanceUrl;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param indexName
	 *            the indexName to set
	 * @throws MalformedURLException
	 */
	public void setIndexName(String indexName) throws MalformedURLException {
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
			this.login = item.login;
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

	public String getCachedUrl() throws UnsupportedEncodingException,
			MalformedURLException {
		rwl.r.lock();
		try {
			if (cachedUrl != null)
				return cachedUrl;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (cachedUrl != null)
				return cachedUrl;
			cachedUrl = PushServlet.getCachedUrl(this);
			return cachedUrl;
		} finally {
			rwl.w.unlock();
		}
	}

}
