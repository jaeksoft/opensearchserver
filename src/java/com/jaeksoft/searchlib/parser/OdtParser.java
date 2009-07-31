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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class OdtParser extends OOParser {
	StringBuffer textBuffer;

	public OdtParser() {
		super();
	}

	@Override
	protected void parseContent(LimitInputStream inputStream) {

		// Parse meta
		try {
			super.parseContent(inputStream);

			DocumentBuilder builder = null;
			Document inputDocument = null;
			try {
				builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				inputDocument = builder.parse(inputStream);
			} catch (IOException e) {
				System.err.println("Unable to read input file.");
				System.err.println(e.getMessage());
			} catch (Exception e) {
				System.err.println("Unable to parse input file.");
				System.err.println(e.getMessage());
			}

			if (inputDocument != null) {
				Node childNode = inputDocument.getFirstChild();

				while (childNode != null) {
					inputDocument.removeChild(childNode);
					childNode = inputDocument.getFirstChild();
					System.out.println(childNode.getNodeValue());
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

}
