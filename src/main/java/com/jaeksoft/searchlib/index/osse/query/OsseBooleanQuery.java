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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.api.OsseCursor;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.util.IOUtils;

public class OsseBooleanQuery extends OsseAbstractQuery {

	private class OsseBooleanClause {

		private final OsseAbstractQuery query;

		private OsseBooleanClause(BooleanClause booleanClause)
				throws SearchLibException {
			query = OsseAbstractQuery.create(booleanClause.getQuery());
		}

		public OsseCursor execute(OsseIndex index,
				Map<String, FieldInfo> fieldMap, OsseErrorHandler error)
				throws SearchLibException {
			query.execute(index, fieldMap, error);
			return query.cursor;
		}

	}

	private final List<OsseBooleanClause> clauses;

	OsseBooleanQuery(BooleanQuery booleanQuery) throws SearchLibException {
		clauses = new ArrayList<OsseBooleanClause>(booleanQuery.clauses()
				.size());
		for (BooleanClause booleanClause : booleanQuery)
			clauses.add(new OsseBooleanClause(booleanClause));
	}

	@Override
	public void execute(OsseIndex index, Map<String, FieldInfo> fieldMap,
			OsseErrorHandler error) throws SearchLibException {
		List<OsseCursor> cursors = new ArrayList<OsseCursor>(clauses.size());
		for (OsseBooleanClause clause : clauses)
			cursors.add(clause.execute(index, fieldMap, error));
		cursor = null;// new OsseCursor(index, error,
		// OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_AND, cursors);
	}

	@Override
	public void close() {
		super.close();
		for (OsseBooleanClause clause : clauses)
			IOUtils.close(clause.query);
	}
}
