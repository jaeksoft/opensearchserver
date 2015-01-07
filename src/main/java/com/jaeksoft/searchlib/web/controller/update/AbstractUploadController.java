/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import org.xml.sax.SAXException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

@AfterCompose(superclass = true)
public abstract class AbstractUploadController extends CommonController {

	public abstract class AbstractUpdateThread extends
			ThreadAbstract<AbstractUpdateThread> {

		private final String mediaName;

		protected final Client client;

		protected File tempResult;

		protected final StreamSource streamSource;

		protected AbstractUpdateThread(Client client,
				StreamSource streamSource, String mediaName) {
			super(client, null, null, null);
			this.client = client;
			this.mediaName = mediaName;
			tempResult = null;
			this.streamSource = streamSource;
			setInfo("Starting...");
		}

		public String getMediaName() {
			return mediaName;
		}

		protected abstract int doUpdate() throws SearchLibException,
				IOException;

		@Override
		public final void runner() throws Exception {
			setInfo("Running...");
			int updatedCount = doUpdate();
			setInfo("Done: " + updatedCount + " document(s)");
		}

		@Override
		public void release() {
			if (tempResult != null)
				tempResult.delete();
		}

	}

	private final ScopeAttribute updateScopeAttribute;

	public AbstractUploadController(ScopeAttribute updateScopeAttribute)
			throws SearchLibException {
		super();
		this.updateScopeAttribute = updateScopeAttribute;
	}

	/**
	 * Return the map of current threads. The map is stored in users session
	 * 
	 * @return
	 */
	private Map<Client, List<AbstractUpdateThread>> getUpdateMap() {
		synchronized (this) {
			synchronized (updateScopeAttribute) {
				@SuppressWarnings("unchecked")
				Map<Client, List<AbstractUpdateThread>> map = (Map<Client, List<AbstractUpdateThread>>) getAttribute(updateScopeAttribute);
				if (map == null) {
					map = new HashMap<Client, List<AbstractUpdateThread>>();
					setAttribute(updateScopeAttribute, map);
				}
				return map;
			}
		}
	}

	private List<AbstractUpdateThread> getUpdateList(Client client) {
		Map<Client, List<AbstractUpdateThread>> map = getUpdateMap();
		if (map == null)
			return null;
		synchronized (map) {
			List<AbstractUpdateThread> list = map.get(client);
			if (list == null) {
				list = new ArrayList<AbstractUpdateThread>(0);
				map.put(client, list);
			}
			return list;
		}
	}

	public List<AbstractUpdateThread> getUpdateList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return getUpdateList(client);
		}
	}

	public boolean isUpdateListNotEmpty() throws SearchLibException {
		synchronized (this) {
			List<AbstractUpdateThread> list = getUpdateList();
			if (list == null)
				return false;
			return getUpdateList().size() > 0;
		}
	}

	public boolean isRefresh() throws SearchLibException {
		synchronized (this) {
			List<AbstractUpdateThread> list = getUpdateList();
			if (list == null)
				return false;
			for (AbstractUpdateThread thread : list)
				if (thread.isRunning())
					return true;
		}
		return false;
	}

	@Command
	public void onTimerRefresh() throws SearchLibException {
		reload();
	}

	@Command
	public void onPurge() throws SearchLibException {
		synchronized (this) {
			List<AbstractUpdateThread> list = getUpdateList();
			synchronized (list) {
				Iterator<AbstractUpdateThread> it = list.iterator();
				while (it.hasNext()) {
					AbstractUpdateThread thread = it.next();
					if (!thread.isRunning())
						it.remove();
				}
			}
			reload();
		}
	}

	protected abstract AbstractUpdateThread newUpdateThread(Client client,
			StreamSource streamSource, String mediaName);

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
			AbstractUpdateThread thread = newUpdateThread(client, streamSource,
					media.getName());
			List<AbstractUpdateThread> list = getUpdateList(client);
			synchronized (list) {
				list.add(thread);
			}
			thread.execute(180);
		}
	}

	@Command
	public void onUpload(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx)
			throws InterruptedException, XPathExpressionException,
			NoSuchAlgorithmException, ParserConfigurationException,
			SAXException, IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		UploadEvent uploadEvent = (UploadEvent) ctx.getTriggerEvent();
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
		reload();
	}

}
