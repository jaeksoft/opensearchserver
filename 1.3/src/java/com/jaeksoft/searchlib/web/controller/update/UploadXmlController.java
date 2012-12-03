/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UploadXmlController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1806972305859799181L;

	public class UpdateThread extends ThreadAbstract {

		private volatile String mediaName;

		private volatile Client client;

		private volatile String xsl;

		private volatile File xmlTempResult;

		private volatile StreamSource streamSource;

		private UpdateThread(Client client, StreamSource streamSource,
				String xsl, String mediaName) {
			super(client, null);
			this.client = client;
			this.xsl = xsl;
			this.mediaName = mediaName;
			xmlTempResult = null;
			this.streamSource = streamSource;
			setInfo("Starting...");
		}

		@Override
		public void runner() throws Exception {
			setInfo("Running...");
			ProxyHandler proxyHandler = client.getWebPropertyManager()
					.getProxyHandler();
			Node xmlDoc;
			if (xsl != null && xsl.length() > 0) {
				xmlTempResult = File.createTempFile("ossupload", ".xml");
				DomUtils.xslt(streamSource, xsl, xmlTempResult);
				xmlDoc = DomUtils.readXml(new StreamSource(xmlTempResult),
						false);
			} else {
				xmlDoc = DomUtils.readXml(streamSource, false);
			}
			int updatedCount = client.updateXmlDocuments(xmlDoc, 50, null,
					proxyHandler, this);
			setInfo("Done: " + updatedCount + " document(s)");
		}

		public String getMediaName() {
			return mediaName;
		}

		@Override
		public void release() {
			if (xmlTempResult != null)
				xmlTempResult.delete();
		}

	}

	private boolean xslEnabled;

	private String xslContent;

	public UploadXmlController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		xslEnabled = false;
		xslContent = null;
	}

	/**
	 * Return the map of current threads. The map is stored in users session
	 * 
	 * @return
	 */
	private Map<Client, List<UpdateThread>> getUpdateMap() {
		synchronized (this) {
			synchronized (ScopeAttribute.UPDATE_XML_MAP) {
				@SuppressWarnings("unchecked")
				Map<Client, List<UpdateThread>> map = (Map<Client, List<UpdateThread>>) getAttribute(ScopeAttribute.UPDATE_XML_MAP);
				if (map == null) {
					map = new HashMap<Client, List<UpdateThread>>();
					setAttribute(ScopeAttribute.UPDATE_XML_MAP, map);
				}
				return map;
			}
		}
	}

	private List<UpdateThread> getUpdateList(Client client) {
		Map<Client, List<UpdateThread>> map = getUpdateMap();
		if (map == null)
			return null;
		synchronized (map) {
			List<UpdateThread> list = map.get(client);
			if (list == null) {
				list = new ArrayList<UpdateThread>(0);
				map.put(client, list);
			}
			return list;
		}
	}

	public List<UpdateThread> getUpdateList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return getUpdateList(client);
		}
	}

	public boolean isUpdateListNotEmpty() throws SearchLibException {
		synchronized (this) {
			List<UpdateThread> list = getUpdateList();
			if (list == null)
				return false;
			return getUpdateList().size() > 0;
		}
	}

	public boolean isRefresh() throws SearchLibException {
		synchronized (this) {
			List<UpdateThread> list = getUpdateList();
			if (list == null)
				return false;
			for (UpdateThread thread : list)
				if (thread.isRunning())
					return true;
		}
		return false;
	}

	public void onTimerRefresh() {
		synchronized (this) {
			reloadComponent("threadList");
			reloadComponent("updateTimer");
		}
	}

	public void onPurge() throws SearchLibException {
		synchronized (this) {
			List<UpdateThread> list = getUpdateList();
			synchronized (list) {
				Iterator<UpdateThread> it = list.iterator();
				while (it.hasNext()) {
					UpdateThread thread = it.next();
					if (!thread.isRunning())
						it.remove();
				}
			}
			reloadPage();
		}
	}

	private void doMedia(Media media) throws XPathExpressionException,
			NoSuchAlgorithmException, SAXException, IOException,
			ParserConfigurationException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		synchronized (this) {
			Client client = getClient();
			StreamSource streamSource;
			if (media.inMemory()) {
				if (media.isBinary()) {
					// Memory + Binary
					streamSource = new StreamSource(new ByteArrayInputStream(
							media.getByteData()));
				} else {
					// Memory + Texte
					streamSource = new StreamSource(media.getReaderData());
				}
			} else {
				if (media.isBinary()) // File + Binary
					streamSource = new StreamSource(media.getStreamData());
				else
					// File + Text
					streamSource = new StreamSource(media.getReaderData());
			}
			UpdateThread thread = new UpdateThread(client, streamSource,
					xslContent, media.getName());
			List<UpdateThread> list = getUpdateList(client);
			synchronized (list) {
				list.add(thread);
			}
			thread.execute();
			thread.waitForStart(20);
		}
	}

	public void onUpload(Event event) throws InterruptedException,
			XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		UploadEvent uploadEvent = (UploadEvent) event;
		Media[] medias = uploadEvent.getMedias();
		if (medias != null) {
			for (Media media : medias)
				doMedia(media);
		} else {
			Media media = uploadEvent.getMedia();
			if (media == null)
				return;
			doMedia(media);
		}
		reloadPage();
	}

	/**
	 * @return the xslEnabled
	 */
	public boolean isXslEnabled() {
		return xslEnabled;
	}

	/**
	 * @param xslEnabled
	 *            the xslEnabled to set
	 * @throws SearchLibException
	 */
	public void setXslEnabled(boolean xslEnabled) throws SearchLibException {
		this.xslEnabled = xslEnabled;
		this.xslContent = null;
		reloadPage();
	}

	/**
	 * @return the xslContent
	 */
	public String getXslContent() {
		return xslContent;
	}

	/**
	 * @param xslContent
	 *            the xslContent to set
	 */
	public void setXslContent(String xslContent) {
		this.xslContent = xslContent;
	}
}
