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
