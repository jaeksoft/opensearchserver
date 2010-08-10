/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

public class RtfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.content,
			ParserFieldEnum.filename, ParserFieldEnum.content_type };

	public RtfParser() {
		super(fl);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		try {
			rtf.read(inputStream, doc, 0);
			addField(ParserFieldEnum.content, doc.getText(0, doc.getLength())
					.trim());
		} catch (BadLocationException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		try {
			rtf.read(reader, doc, 0);
			addField(ParserFieldEnum.content, doc.getText(0, doc.getLength())
					.trim());
		} catch (BadLocationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}

}
