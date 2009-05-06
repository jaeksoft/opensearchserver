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
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.XmlInfo;

public interface WriterInterface extends XmlInfo {

	public boolean deleteDocument(String indexName, Schema schema,
			String uniqueField) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException;

	public int deleteDocuments(String indexName, Schema schema,
			Collection<String> uniqueFields) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException;

	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException;

	public boolean updateDocument(String indexName, Schema schema,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException;

	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException;

	public int updateDocuments(String indexName, Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException;

	public void optimize(String indexName) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

}
