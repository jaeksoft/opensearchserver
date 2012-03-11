/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class FileItemFieldEnum extends ExtensibleEnum<ItemField> {

	public final static ExtensibleEnum<ItemField> ENUM = new ExtensibleEnum<ItemField>();

	public final static ItemField repository = new ItemField(ENUM, "repository");

	public final static ItemField uri = new ItemField(ENUM, "uri");

	public final static ItemField directory = new ItemField(ENUM, "directory");

	public final static ItemField subDirectory = new ItemField(ENUM,
			"subDirectory");

	public final static ItemField lang = new ItemField(ENUM, "lang");

	public final static ItemField langMethod = new ItemField(ENUM, "langMethod");

	public final static ItemField contentLength = new ItemField(ENUM,
			"contentLength");

	public final static ItemField parserStatus = new ItemField(ENUM,
			"parserStatus");

	public final static ItemField fetchStatus = new ItemField(ENUM,
			"fetchStatus");

	public final static ItemField indexStatus = new ItemField(ENUM,
			"indexStatus");

	public final static ItemField crawlDate = new ItemField(ENUM, "crawlDate");

	public final static ItemField fileSystemDate = new ItemField(ENUM,
			"fileSystemDate");

	public final static ItemField fileName = new ItemField(ENUM, "fileName");

	public final static ItemField fileSize = new ItemField(ENUM, "fileSize");

	public final static ItemField fileExtension = new ItemField(ENUM,
			"fileExtension");

	public final static ItemField fileType = new ItemField(ENUM, "fileType");

}
