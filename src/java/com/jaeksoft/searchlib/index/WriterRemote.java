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

import org.apache.commons.httpclient.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

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

	public void optimize(String indexName) throws HttpException, IOException,
			URISyntaxException {
		ActionServlet.optimize(uri, getName());
	}

	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (!acceptDocument(document))
			return false;
		return IndexServlet.update(uri, getName(), document);
	}

	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		return IndexServlet.update(uri, getName(), documents);
	}

	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		return DeleteServlet.delete(uri, getName(), uniqueField);
	}

	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws IOException, URISyntaxException {
		return DeleteServlet.delete(uri, getName(), uniqueFields);
	}

}
