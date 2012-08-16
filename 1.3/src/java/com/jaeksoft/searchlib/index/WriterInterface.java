/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Schema;

public interface WriterInterface {

	public void deleteAll() throws SearchLibException;

	public boolean deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException;

	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException;

	public int deleteDocuments(SearchRequest query) throws SearchLibException;

	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate)
			throws SearchLibException;

	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException;

	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException;

	public void optimize() throws SearchLibException;

	public boolean isOptimizing();

}
