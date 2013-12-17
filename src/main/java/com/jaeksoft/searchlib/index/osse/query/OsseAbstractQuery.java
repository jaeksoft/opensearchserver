/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse.query;

import java.io.Closeable;
import java.util.Map;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.api.OsseCursor;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.util.IOUtils;

public abstract class OsseAbstractQuery implements Closeable {

	protected OsseCursor cursor = null;

	final public static OsseAbstractQuery create(Query query)
			throws SearchLibException {
		if (query instanceof TermQuery)
			return new OsseTermQuery((TermQuery) query);
		else if (query instanceof BooleanQuery)
			return new OsseBooleanQuery((BooleanQuery) query);
		throw new SearchLibException("Unsupported query type: "
				+ query.getClass().getName());
	}

	public abstract void execute(OsseIndex index,
			Map<String, FieldInfo> fieldMap, OsseErrorHandler error)
			throws SearchLibException;

	@Override
	public void close() {
		IOUtils.close(cursor);
	}

}
