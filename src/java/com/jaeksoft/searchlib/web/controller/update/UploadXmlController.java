/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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
import org.zkoss.zul.Fileupload;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class UploadXmlController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1806972305859799181L;

	private Integer updatedCount;

	public UploadXmlController() throws SearchLibException {
		super();
		updatedCount = null;
	}

	public Integer getUpdatedCount() {
		synchronized (this) {
			return updatedCount;
		}
	}

	public void onUpload() throws InterruptedException,
			XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Media media = Fileupload.get();
		if (media == null)
			return;
		synchronized (this) {
			if (media.inMemory()) {
				if (media.isBinary())
					updatedCount = getClient().updateXmlDocuments(null,
							new String(media.getByteData()));
				else
					updatedCount = getClient().updateXmlDocuments(null,
							media.getStringData());
			} else {
				if (media.isBinary())
					updatedCount = getClient().updateXmlDocuments(null,
							new InputSource(media.getStreamData()));
				else
					updatedCount = getClient().updateXmlDocuments(null,
							new InputSource(media.getReaderData()));
			}
			reloadPage();
		}
	}

}
