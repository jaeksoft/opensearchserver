/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.io.IOUtils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class TextParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.content,
			ParserFieldEnum.filename, ParserFieldEnum.content_type };

	public TextParser() {
		super(fl);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {
		byte[] bytes = IOUtils.toByteArray(inputStream);
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		String content = null;
		if (match != null)
			content = match.getString();
		else
			content = new String(bytes);
		addField(ParserFieldEnum.content, content);
		langDetection(10000, ParserFieldEnum.content);
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
