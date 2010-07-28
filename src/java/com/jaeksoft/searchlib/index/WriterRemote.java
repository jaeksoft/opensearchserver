/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.web.ActionServlet;
import com.jaeksoft.searchlib.web.DeleteServlet;
import com.jaeksoft.searchlib.web.IndexServlet;

public class WriterRemote extends WriterAbstract {

	private URI uri;

	protected WriterRemote(IndexConfig indexConfig) {
		super(indexConfig);
		this.uri = indexConfig.getRemoteUri();
	}

	public void xmlInfo(PrintWriter writer) {
	}

	@Override
	public void optimize() throws SearchLibException {
		try {
			ActionServlet.optimize(uri, null);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		try {
			if (!acceptDocument(document))
				return false;
			return IndexServlet.update(uri, null, document);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		try {
			return IndexServlet.update(uri, null, documents);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		return DeleteServlet.delete(uri, null, uniqueField);
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		try {
			return DeleteServlet.delete(uri, null, uniqueFields);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

}
