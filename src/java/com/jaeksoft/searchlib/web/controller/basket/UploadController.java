/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.basket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.util.media.Media;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Fileupload;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.basket.BasketDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UploadController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5038289778698152000L;

	public UploadController() throws SearchLibException {
		super();
	}

	public boolean isCurrentDocumentValid() {
		return getCurrentDocument() != null;
	}

	public BasketDocument getCurrentDocument() {
		return (BasketDocument) getAttribute(ScopeAttribute.BASKET_CURRENT_DOCUMENT);
	}

	public void setCurrentDocument(BasketDocument basketDocument) {
		setAttribute(ScopeAttribute.BASKET_CURRENT_DOCUMENT, basketDocument);
	}

	public void onUpload() throws InterruptedException,
			XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Media media = Fileupload.get();
		if (media == null)
			return;
		setCurrentDocument(null);
		ParserSelector parserSelector = getClient().getParserSelector();
		Parser parser = null;
		String contentType = media.getContentType();
		if (contentType != null)
			parser = parserSelector.getParserFromMimeType(contentType);
		if (parser == null) {
			String extension = FileUtils.getFileNameExtension(media.getName());
			parser = parserSelector.getParserFromExtension(extension);
		}
		if (parser == null) {
			Messagebox.show("No parser found for that document type ("
					+ contentType + " - " + media.getName() + ')');
			return;
		}

		BasketDocument basketDocument = parser.getBasketDocument();
		setCurrentDocument(basketDocument);
		basketDocument.addIfNoEmpty("filename", media.getName());
		basketDocument.addIfNoEmpty("content_type", contentType);

		synchronized (this) {
			if (media.inMemory()) {
				if (media.isBinary())
					parser.parseContent(media.getByteData());
				else
					parser.parseContent(media.getStringData());
			} else {
				if (media.isBinary())
					parser.parseContent(media.getStreamData());
				else
					parser.parseContent(media.getReaderData());
			}
			reloadPage();
		}
	}

	public void onPaging(Event event) {
		System.out.println(event);
	}
}
