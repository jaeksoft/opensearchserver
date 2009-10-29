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

package com.jaeksoft.searchlib.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class StopFilter extends FilterFactory {

	private CharArraySet words;

	private String filePath;

	@Override
	public void setParams(XPathParser xpp, Node node) throws IOException {
		words = new CharArraySet(0, true);
		filePath = XPathParser.getAttributeString(node, "file");
		File file = new File(xpp.getCurrentFile().getParentFile(), filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) {
				words.add(line);
			}
		}
		br.close();
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new org.apache.lucene.analysis.StopFilter(false, tokenStream,
				words);
	}

	@Override
	public String getDescription() {
		return "Removes stop words. Stop file: " + filePath + ". "
				+ words.size() + " words.";
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer
				.startElement("filter", "class", getClassName(), "file",
						filePath);
		writer.endElement();
	}

}
