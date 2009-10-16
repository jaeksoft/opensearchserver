/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.Md5Spliter;

public abstract class WriterAbstract extends NameFilter implements
		WriterInterface {

	private Md5Spliter md5spliter = null;
	private String keyField = null;

	protected WriterAbstract(IndexConfig indexConfig) {
		super(indexConfig.getName());
		this.md5spliter = null;
		this.keyField = indexConfig.getKeyField();
		if (indexConfig.getKeyMd5RegExp() != null)
			md5spliter = new Md5Spliter(indexConfig.getKeyMd5RegExp());
	}

	protected boolean acceptDocument(IndexDocument document)
			throws NoSuchAlgorithmException {
		if (keyField == null)
			return true;
		if (md5spliter == null)
			return true;
		FieldContent fieldContent = document.getField(keyField);
		if (fieldContent == null)
			return false;
		return md5spliter.acceptAnyKey(fieldContent.getValues());
	}

	public boolean updateDocument(String indexName, Schema schema,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!acceptNameOrEmpty(indexName))
			return false;
		return updateDocument(schema, document);
	}

	public int updateDocuments(String indexName, Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!acceptNameOrEmpty(indexName))
			return 0;
		return updateDocuments(schema, documents);
	}

	public boolean deleteDocument(String indexName, Schema schema,
			String uniqueField) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		if (!acceptNameOrEmpty(indexName))
			return false;
		return deleteDocument(schema, uniqueField);
	}

	public int deleteDocuments(String indexName, Schema schema,
			Collection<String> uniqueFields) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!acceptNameOrEmpty(indexName))
			return 0;
		return deleteDocuments(schema, uniqueFields);
	}

}
