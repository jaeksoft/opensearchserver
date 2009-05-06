/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

public class ParserFactory {

	private String className;
	private long sizeLimit;

	public ParserFactory(String className, long sizeLimit) {
		this.className = className;
		this.sizeLimit = sizeLimit;
	}

	public Parser getNewParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Parser parser = (Parser) Class.forName(className).newInstance();
		parser.setSizeLimit(sizeLimit);
		return parser;
	}

	public String getClassName() {
		return className;
	}

	public long getSizeLimit() {
		return sizeLimit;
	}
}
