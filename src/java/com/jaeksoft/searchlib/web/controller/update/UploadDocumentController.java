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

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;
import org.zkoss.util.media.Media;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Panelchildren;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.model.FieldContentModel;

public class UploadDocumentController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5038289778698152000L;

	private Panelchildren basketComponent;

	public UploadDocumentController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		basketComponent = null;
	}

	public boolean isCurrentDocumentValid() {
		synchronized (this) {
			checkBasketComponent();
			return getCurrentDocument() != null;
		}
	}

	private void removeBasketComponent() {
		synchronized (this) {
			if (basketComponent == null)
				return;
			getFellow("basketDocumentPanel").removeChild(basketComponent);
			basketComponent = null;
		}
	}

	private void setBasketComponent() {
		synchronized (this) {
			removeBasketComponent();
			IndexDocument basketDocument = getCurrentDocument();
			if (basketDocument == null)
				return;
			basketComponent = FieldContentModel
					.createIndexDocumentComponent(basketDocument
							.getFieldContentArray());
			basketComponent.setParent(getFellow("basketDocumentPanel"));
		}
	}

	private void checkBasketComponent() {
		synchronized (this) {
			if (basketComponent != null)
				return;
			setBasketComponent();
		}
	}

	public IndexDocument getCurrentDocument() {
		synchronized (this) {
			return (IndexDocument) getAttribute(ScopeAttribute.BASKET_CURRENT_DOCUMENT);
		}
	}

	public void setCurrentDocument(IndexDocument basketDocument) {
		synchronized (this) {
			setAttribute(ScopeAttribute.BASKET_CURRENT_DOCUMENT, basketDocument);
			setBasketComponent();
		}
	}

	public void onUpload() throws InterruptedException,
			XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		Media media = Fileupload.get();
		if (media == null)
			return;
		synchronized (this) {
			setCurrentDocument(null);
			ParserSelector parserSelector = getClient().getParserSelector();
			Parser parser = null;
			String contentType = media.getContentType();
			if (contentType != null)
				parser = parserSelector.getParserFromMimeType(contentType);
			if (parser == null) {
				String extension = FilenameUtils.getExtension(media.getName());
				parser = parserSelector.getParserFromExtension(extension);
			}
			if (parser == null) {
				new AlertController("No parser found for that document type ("
						+ contentType + " - " + media.getName() + ')');
				return;
			}

			parser.addField(ParserFieldEnum.filename, media.getName());
			parser.addField(ParserFieldEnum.content_type, contentType);

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
			setCurrentDocument(parser.getParserDocument());
			reloadPage();
		}
	}

	// TODO index document
	public void onSave() throws SearchLibException, InterruptedException,
			NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		synchronized (this) {
			IndexDocument basketDocument = getCurrentDocument();
			if (basketDocument == null)
				return;
			getClient().updateDocument(basketDocument);
			setCurrentDocument(null);
			new AlertController("Document updated");
			reloadDesktop();
		}
	}
}
