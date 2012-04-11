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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.w3c.dom.Document;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

/**
 * 
 * @author Emmanuel Gosse (philCube)
 * 
 */
public class OdtParser extends OOParser {
	StringBuffer textBuffer;

	public OdtParser() {
		super();
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) {
		try {

			// Load file
			OdfTextDocument odt = (OdfTextDocument) OdfDocument
					.loadDocument(streamLimiter.getNewInputStream());

			// get root of all content of a text document
			OfficeTextElement officeText = odt.getContentRoot();
			scanNodes(officeText.getChildNodes(), ParserFieldEnum.content);

			Document docOwner = officeText.getOwnerDocument();
			if (docOwner != null)
				scanNodes(docOwner.getChildNodes(), ParserFieldEnum.author);

			langDetection(10000, ParserFieldEnum.content);

		} catch (IOException e) {
			Logging.error(e.getMessage(), e);
		} catch (Exception e) {
			Logging.error(e.getMessage(), e);
		}
	}

}
