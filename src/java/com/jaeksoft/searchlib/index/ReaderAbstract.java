/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.store.LockObtainFailedException;

public abstract class ReaderAbstract extends NameFilter implements
		ReaderInterface {

	protected ReaderAbstract(String indexName) {
		super(indexName);
	}

	public boolean deleteDocument(String indexName, int docId)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		if (!acceptNameOrEmpty(indexName))
			return false;
		return deleteDocument(docId);
	}

	public int deleteDocuments(String indexName, Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		if (!acceptNameOrEmpty(indexName))
			return 0;
		return deleteDocuments(docIds);
	}

}
