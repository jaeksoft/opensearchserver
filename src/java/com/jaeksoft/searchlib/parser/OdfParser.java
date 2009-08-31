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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.office.OdfOfficeSpreadsheet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Emmanuel Gosse (philCube)
 * 
 */
public class OdfParser extends OOParser {
	StringBuffer textBuffer;

	public OdfParser() {
		super();
	}

	@Override
	protected void parseContent(LimitInputStream inputStream) {
		try {
			super.parseContent(inputStream);

			// Load file
			OdfSpreadsheetDocument odf = (OdfSpreadsheetDocument) OdfSpreadsheetDocument
					.loadDocument(inputStream);

			// get root of all content of a text document
			OdfOfficeSpreadsheet officeText = odf.getContentRoot();
			scanNodes(officeText.getChildNodes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void scanNodes(NodeList nodeList) {
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node lode = nodeList.item(i);

				if (lode.getNodeType() == Node.TEXT_NODE)
					addField(ParserFieldEnum.content, lode.getNodeValue());

				if (lode.getNodeType() == Node.CDATA_SECTION_NODE)
					addField(ParserFieldEnum.content, lode.getNodeValue());

				if (lode.getNodeType() == Node.NOTATION_NODE)
					addField(ParserFieldEnum.content, lode.getNodeValue());

				scanNodes(lode.getChildNodes());
			}
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

}
