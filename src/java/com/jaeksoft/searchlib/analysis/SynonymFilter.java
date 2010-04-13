/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SynonymFilter extends FilterFactory {

	private SynonymMap synonymMap = null;

	private String filePath = null;

	private static TreeMap<File, SynonymMap> synonymMaps = new TreeMap<File, SynonymMap>();

	@Override
	public void setParams(XPathParser xpp, Node node) throws IOException {
		filePath = XPathParser.getAttributeString(node, "file");
		File file = new File(xpp.getCurrentFile().getParentFile(), filePath);

		synchronized (synonymMaps) {
			synonymMap = synonymMaps.get(file);
			if (synonymMap == null) {
				synonymMap = new SynonymMap(file);
				synonymMaps.put(file, synonymMap);
			}
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		for (SynonymQueue queue : synonymMap.getSynonymQueues())
			tokenStream = new SynonymTokenFilter(tokenStream, queue);
		return tokenStream;
	}

	@Override
	public String getDescription() {
		return "Add synonyms support. File: " + filePath + ". "
				+ synonymMap.getSize() + " synonym(s).";
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer
				.startElement("filter", "class", getClassName(), "file",
						filePath);
		writer.endElement();
	}
}
