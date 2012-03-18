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

package com.jaeksoft.searchlib.crawler.web.database;

import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class UrlItemFieldEnum extends ExtensibleEnum<ItemField> {

	final public ItemField url = new ItemField(this, "url");

	final public ItemField parentUrl = new ItemField(this, "parentUrl");

	final public ItemField origin = new ItemField(this, "origin");

	final public ItemField lang = new ItemField(this, "lang");

	final public ItemField langMethod = new ItemField(this, "langMethod");

	final public ItemField contentDispositionFilename = new ItemField(this,
			"contentDispositionFilename");

	final public ItemField contentBaseType = new ItemField(this,
			"contentBaseType");

	final public ItemField contentTypeCharset = new ItemField(this,
			"contentTypeCharset");

	final public ItemField contentEncoding = new ItemField(this,
			"contentEncoding");

	final public ItemField contentLength = new ItemField(this, "contentLength");

	final public ItemField outlink = new ItemField(this, "outlink");

	final public ItemField inlink = new ItemField(this, "inlink");

	final public ItemField host = new ItemField(this, "host");

	final public ItemField subhost = new ItemField(this, "subhost");

	final public ItemField when = new ItemField(this, "when");

	final public ItemField responseCode = new ItemField(this, "responseCode");

	final public ItemField robotsTxtStatus = new ItemField(this,
			"robotsTxtStatus");

	final public ItemField parserStatus = new ItemField(this, "parserStatus");

	final public ItemField fetchStatus = new ItemField(this, "fetchStatus");

	final public ItemField indexStatus = new ItemField(this, "indexStatus");

	final public ItemField md5size = new ItemField(this, "md5size");

	final public ItemField lastModifiedDate = new ItemField(this,
			"lastModifiedDate");

	final public ItemField reponseCode = new ItemField(this, "responseCode");

}
