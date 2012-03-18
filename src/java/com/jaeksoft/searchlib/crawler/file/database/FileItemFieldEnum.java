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

	public final ItemField repository = new ItemField(this, "repository");

	public final ItemField uri = new ItemField(this, "uri");

	public final ItemField directory = new ItemField(this, "directory");

	public final ItemField subDirectory = new ItemField(this, "subDirectory");

	public final ItemField lang = new ItemField(this, "lang");

	public final ItemField langMethod = new ItemField(this, "langMethod");

	public final ItemField parserStatus = new ItemField(this, "parserStatus");

	public final ItemField fetchStatus = new ItemField(this, "fetchStatus");

	public final ItemField indexStatus = new ItemField(this, "indexStatus");

	public final ItemField crawlDate = new ItemField(this, "crawlDate");

	public final ItemField fileSystemDate = new ItemField(this,
			"fileSystemDate");

	public final ItemField fileName = new ItemField(this, "fileName");

	public final ItemField fileSize = new ItemField(this, "fileSize");

	public final ItemField fileExtension = new ItemField(this, "fileExtension");

	public final ItemField fileType = new ItemField(this, "fileType");

}
