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
import java.util.Iterator;

import org.jdom.Document;
import org.jopendocument.dom.ODMeta;
import org.jopendocument.dom.ODPackage;

public class OdtParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.author, ParserFieldEnum.subject,
			ParserFieldEnum.content };

	public OdtParser() {
		super(fl);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		ODPackage pkg = new ODPackage(inputStream);
		ODMeta meta = pkg.getMeta();
		if (meta != null) {
			addField(ParserFieldEnum.title, meta.getTitle());
			addField(ParserFieldEnum.author, meta.getInitialCreator());
			addField(ParserFieldEnum.subject, meta.getSubject());
		}

		Document doc = pkg.getContent().getDocument();
		Iterator<Document> descendants = doc.getDescendants();
		while (descendants.hasNext()) {
			Document descendant = (Document) descendants.next();
			descendant.getContent();
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}
}
