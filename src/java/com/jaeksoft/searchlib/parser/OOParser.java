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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.jopendocument.dom.ODMeta;
import org.jopendocument.dom.ODPackage;

public abstract class OOParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.author, ParserFieldEnum.subject,
			ParserFieldEnum.content, ParserFieldEnum.producer,
			ParserFieldEnum.keywords, ParserFieldEnum.creation_date,
			ParserFieldEnum.modification_date, ParserFieldEnum.language,
			ParserFieldEnum.number_of_pages };

	public OOParser() {
		super(fl);
	}

	/**
	 * 
	 * Parser Meta informations of all OpenOffice documents
	 * 
	 */
	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		ODPackage pkg = new ODPackage(inputStream);
		ODMeta meta = pkg.getMeta();
		if (meta != null) {
			addField(ParserFieldEnum.title, meta.getTitle());
			addField(ParserFieldEnum.author, meta.getInitialCreator());
			addField(ParserFieldEnum.subject, meta.getSubject());
			addField(ParserFieldEnum.creation_date, meta.getCreationDate());
			addField(ParserFieldEnum.modification_date, meta.getModifDate());
			addField(ParserFieldEnum.language, meta.getLanguage());

			for (Iterator<String> ite = meta.getKeywords().iterator(); ite
					.hasNext();) {
				addField(ParserFieldEnum.keywords, ite.next());
			}
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

	protected static File getFile(InputStream stream) throws IOException {
		File f = new File("currentOO.oo");
		OutputStream out = new FileOutputStream(f);
		byte buf[] = new byte[1024];
		int len;
		while ((len = stream.read(buf)) > 0)
			out.write(buf, 0, len);
		out.close();
		stream.close();
		return f;
	}
}
