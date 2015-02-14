/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class ParserType extends ExtensibleEnumItem<ParserType> {

	private final Class<? extends Parser> parserClass;

	public final String simpleName;

	public final String[] defaultMimeTypes;

	public final String[] defaultExtensions;

	public ParserType(ExtensibleEnum<ParserType> en, String name,
			Class<? extends Parser> parserClass, String[] defaultMimeTypes,
			String[] defaultExtensions) {
		super(en, name);
		this.parserClass = parserClass;
		String n = parserClass.getSimpleName().toLowerCase();
		simpleName = n.endsWith("parser") ? n.substring(0, n.length() - 6) : n;
		this.defaultMimeTypes = defaultMimeTypes;
		this.defaultExtensions = defaultExtensions;
	}

	/**
	 * @return the parserClass
	 */
	public Class<? extends Parser> getParserClass() {
		return parserClass;
	}

	/**
	 * @return the simpleName
	 */
	public String getSimpleName() {
		return simpleName;
	}

	public String[] getDefaultMimeTypes() {
		return defaultMimeTypes;
	}

	public String[] getDefaultExtensions() {
		return defaultExtensions;
	}
}
