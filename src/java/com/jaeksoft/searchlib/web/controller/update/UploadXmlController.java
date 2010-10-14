/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class UploadXmlController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1806972305859799181L;

	private int updatedCount;

	public UploadXmlController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		updatedCount = 0;
	}

	public int getUpdatedCount() {
		synchronized (this) {
			return updatedCount;
		}
	}

	private void doMedia(Media media) throws XPathExpressionException,
			NoSuchAlgorithmException, SAXException, IOException,
			ParserConfigurationException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		synchronized (this) {
			if (media.inMemory()) {
				if (media.isBinary()) {
					byte[] bytes = media.getByteData();
					updatedCount += getClient().updateXmlDocuments(
							new InputSource(new ByteInputStream(bytes,
									bytes.length)));
				} else {
					byte[] bytes = media.getStringData().getBytes();
					updatedCount += getClient().updateXmlDocuments(
							new InputSource(new ByteInputStream(bytes,
									bytes.length)));
				}
			} else {
				if (media.isBinary())
					updatedCount += getClient().updateXmlDocuments(
							new InputSource(media.getStreamData()));
				else
					updatedCount += getClient().updateXmlDocuments(
							new InputSource(media.getReaderData()));
			}
		}

	}

	public void onUpload(Event event) throws InterruptedException,
			XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		updatedCount = 0;
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
}
