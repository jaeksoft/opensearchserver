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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jopendocument.dom.ODXMLDocument;
import org.jopendocument.dom.OOUtils;

public class OOParser extends Parser {

	StringBuffer textBuffer;

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.author, ParserFieldEnum.subject,
			ParserFieldEnum.content };

	public OOParser() {
		super(fl);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		

		/*if (doc != null) {
			addField(ParserFieldEnum.title, doc.getTitle());
			addField(ParserFieldEnum.author, doc.getAuthor());
			addField(ParserFieldEnum.subject, doc.getSubject());
		}

		List<String> paragraphes = doc.getContent();
		for (String paragraph : paragraphes) {
			String[] frags = paragraph.split("\\n");
			for (String frag : frags)
				addField(ParserFieldEnum.content, frag.replaceAll("\\s+", " "));
		}*/
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}

	private void processElement(Object o) {

		if (o instanceof Element) {
			Element e = (Element) o;
			String elementName = e.getQualifiedName();

			if (elementName.startsWith("text")) {

				if (elementName.equals("text:tab")) // add tab for text:tab
					textBuffer.append("\t");
				else if (elementName.equals("text:s")) // add space for text:s
					textBuffer.append(" ");
				else {
					List children = e.getContent();
					Iterator iterator = children.iterator();

					while (iterator.hasNext()) {

						Object child = iterator.next();
						// If Child is a Text Node, then append the text
						if (child instanceof Text) {
							Text t = (Text) child;
							textBuffer.append(t.getValue());
						} else
							processElement(child); // Recursively process the
													// child element
					}
				}
				if (elementName.equals("text:p"))
					textBuffer.append("\n");
			} else {
				List non_text_list = e.getContent();
				Iterator it = non_text_list.iterator();
				while (it.hasNext()) {
					Object non_text_child = it.next();
					processElement(non_text_child);
				}
			}
		}
	}

	private String getText(String fileName) throws Exception {
		textBuffer = new StringBuffer();

		// Unzip the openOffice Document
		ZipFile zipFile = new ZipFile(fileName);
		Enumeration entries = zipFile.entries();
		ZipEntry entry;

		while (entries.hasMoreElements()) {
			entry = (ZipEntry) entries.nextElement();

			if (entry.getName().equals("content.xml")) {

				textBuffer = new StringBuffer();
				SAXBuilder sax = new SAXBuilder();
				Document doc = sax.build(zipFile.getInputStream(entry));
				Element rootElement = doc.getRootElement();
				processElement(rootElement);
				break;
			}
		}
		return textBuffer.toString();
	}

}
