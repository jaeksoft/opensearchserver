/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

import java.util.Collection;

public interface WriterInterface {

	void deleteAll() throws SearchLibException;

	int deleteDocuments(AbstractRequest request) throws SearchLibException;

	boolean updateDocument(Schema schema, IndexDocument document) throws SearchLibException;

	int updateDocuments(Schema schema, Collection<IndexDocument> documents) throws SearchLibException;

	int updateIndexDocuments(Schema schema, Collection<IndexDocumentResult> documents) throws SearchLibException;

	boolean isMerging();

	void mergeData(WriterInterface source) throws SearchLibException;
}
