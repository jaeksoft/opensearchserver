/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.security.NoSuchAlgorithmException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class FileSystemParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.file_name, ParserFieldEnum.file_length,
			ParserFieldEnum.md5 };

	public FileSystemParser() {
		super(fl);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		ParserResultItem resultItem = getNewParserResultItem();
		try {
			if (getFieldMap().isMapped(ParserFieldEnum.file_length))
				resultItem.addField(ParserFieldEnum.file_length,
						streamLimiter.getSize());
			if (getFieldMap().isMapped(ParserFieldEnum.md5))
				resultItem.addField(ParserFieldEnum.md5,
						streamLimiter.getMD5Hash());
			resultItem.addField(ParserFieldEnum.file_name,
					streamLimiter.getOriginalFileName());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

	}
}
