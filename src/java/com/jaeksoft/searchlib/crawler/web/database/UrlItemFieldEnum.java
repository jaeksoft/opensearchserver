/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class UrlItemFieldEnum extends ExtensibleEnum<UrlItemField> {

	final public UrlItemField url = new UrlItemField(this, "url");

	final public UrlItemField lang = new UrlItemField(this, "lang");

	final public UrlItemField langMethod = new UrlItemField(this, "langMethod");

	final public UrlItemField contentDispositionFilename = new UrlItemField(
			this, "contentDispositionFilename");

	final public UrlItemField contentBaseType = new UrlItemField(this,
			"contentBaseType");

	final public UrlItemField contentTypeCharset = new UrlItemField(this,
			"contentTypeCharset");

	final public UrlItemField contentEncoding = new UrlItemField(this,
			"contentEncoding");

	final public UrlItemField contentLength = new UrlItemField(this,
			"contentLength");

	final public UrlItemField outlink = new UrlItemField(this, "outlink");

	final public UrlItemField inlink = new UrlItemField(this, "inlink");

	final public UrlItemField host = new UrlItemField(this, "host");

	final public UrlItemField subhost = new UrlItemField(this, "subhost");

	final public UrlItemField when = new UrlItemField(this, "when");

	final public UrlItemField responseCode = new UrlItemField(this,
			"responseCode");

	final public UrlItemField robotsTxtStatus = new UrlItemField(this,
			"robotsTxtStatus");

	final public UrlItemField parserStatus = new UrlItemField(this,
			"parserStatus");

	final public UrlItemField fetchStatus = new UrlItemField(this,
			"fetchStatus");

	final public UrlItemField indexStatus = new UrlItemField(this,
			"indexStatus");

	final public UrlItemField md5size = new UrlItemField(this, "md5size");

	final public UrlItemField lastModifiedDate = new UrlItemField(this,
			"lastModifiedDate");

}
