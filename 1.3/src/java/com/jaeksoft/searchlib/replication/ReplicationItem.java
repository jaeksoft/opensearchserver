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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexMode;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.PushServlet;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ReplicationItem implements Comparable<ReplicationItem> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name = null;

	private URL instanceUrl = null;

	private String cachedUrl = null;

	private String login = null;

	private String indexName = null;

	private String apiKey = null;

	private ReplicationThread lastReplicationThread;

	private ReplicationMaster replicationMaster;

	private ReplicationType replicationType;

	private IndexMode readWriteMode;

	public final static String[] NOT_PUSHED_PATH = { "replication.xml",
			"replication_old.xml", "jobs.xml", "jobs_old.xml", "report",
			"statstore" };

	public final static String[] NOT_PUSHED_PATH_NODB = { "web_crawler_url",
			"file_crawler_url" };

	public ReplicationItem(ReplicationMaster replicationMaster, String name) {
		this.replicationMaster = replicationMaster;
		replicationType = ReplicationType.MAIN_INDEX;
		lastReplicationThread = null;
		readWriteMode = IndexMode.READ_WRITE;
	}

	public ReplicationItem(ReplicationMaster replicationMaster) {
		this(replicationMaster, null);
	}

	public ReplicationItem() {
		this(null, null);
	}

	public ReplicationItem(ReplicationItem item) {
		this.copy(item);
	}

	public ReplicationItem(ReplicationMaster replicationMaster,
			XPathParser xpp, Node node) throws MalformedURLException {
		this.name = null;
		this.replicationMaster = replicationMaster;
		String url = XPathParser.getAttributeString(node, "instanceUrl");
		if (url != null && url.length() > 0)
			setInstanceUrl(new URL(url));
		setIndexName(XPathParser.getAttributeString(node, "indexName"));
		setLogin(XPathParser.getAttributeString(node, "login"));
		String encodedApiKey = XPathParser.getAttributeString(node, "apiKey");
		if (encodedApiKey != null && encodedApiKey.length() > 0)
			setApiKey(StringUtils.base64decode(encodedApiKey));
		setReplicationType(ReplicationType.find(XPathParser.getAttributeString(
				node, "replicationType")));
		readWriteMode = IndexMode.find(XPathParser.getAttributeString(node,
				"readWriteMode"));
		updateName();
	}

	private void updateName() throws MalformedURLException {
		String u = getInstanceUrl().toExternalForm();
		if (!u.endsWith("/"))
			u += '/';
		u += getIndexName();
		name = u;
	}

	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			String encodedApiKey = (apiKey != null && apiKey.length() > 0) ? new String(
					StringUtils.base64encode(apiKey)) : "";
			xmlWriter.startElement("replicationItem", "instanceUrl",
					instanceUrl.toExternalForm(), "indexName", indexName,
					"login", login, "apiKey", encodedApiKey, "replicationType",
					replicationType.name(), "readWriteMode",
					readWriteMode.name());
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
			this.name = item.name;
			this.indexName = item.indexName;
			this.instanceUrl = item.instanceUrl;
			this.login = item.login;
			this.apiKey = item.apiKey;
			this.lastReplicationThread = item.lastReplicationThread;
			this.replicationMaster = item.replicationMaster;
			this.replicationType = item.replicationType;
			this.cachedUrl = null;
			this.readWriteMode = item.readWriteMode;
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

	public File getDirectory(Config config) throws SearchLibException {
		rwl.r.lock();
		try {
			switch (replicationType) {
			case MAIN_INDEX:
			case BACKUP_INDEX:
				return config.getDirectory();
			case WEB_CRAWLER_URL_DATABASE:
				return config.getUrlManager().getUrlDbClient().getDirectory();
			case FILE_CRAWLER_URI_DATABASE:
				return config.getFileManager().getFileDbClient().getDirectory();
			default:
				throw new SearchLibException("Unsupported replication");
			}
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the replicationType
	 */
	public ReplicationType getReplicationType() {
		rwl.r.lock();
		try {
			return replicationType;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param replicationType
	 *            the replicationType to set
	 */
	public void setReplicationType(ReplicationType replicationType) {
		rwl.w.lock();
		try {
			this.replicationType = replicationType;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the setReadOnly
	 */
	public IndexMode getReadWriteMode() {
		return readWriteMode;
	}

	public void setReadWriteMode(IndexMode mode) {
		readWriteMode = mode;
	}

	@Override
	public int compareTo(ReplicationItem item) {
		return getName().compareTo(item.getName());
	}

}
