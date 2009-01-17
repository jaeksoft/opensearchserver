/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.Md5Spliter;

public abstract class WriterAbstract extends NameFilter implements
		WriterInterface {

	private Md5Spliter md5spliter = null;
	private String keyField = null;

	protected WriterAbstract(String indexName, String keyField,
			String keyMd5Pattern) {
		super(indexName);
		this.md5spliter = null;
		this.keyField = keyField;
		if (keyMd5Pattern != null)
			md5spliter = new Md5Spliter(keyMd5Pattern);
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

	public void updateDocument(String indexName, Schema schema,
			IndexDocument document, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (!acceptName(indexName))
			return;
		updateDocument(schema, document, forceLocal);
	}

	public void updateDocuments(String indexName, Schema schema,
			List<? extends IndexDocument> documents, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (!acceptName(indexName))
			return;
		updateDocuments(schema, documents, forceLocal);
	}

	public void deleteDocuments(String indexName, Schema schema,
			String uniqueField, boolean bForceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		if (!acceptName(indexName))
			return;
		deleteDocuments(schema, uniqueField, bForceLocal);
	}

	public void deleteDocuments(String indexName, Schema schema,
			List<String> uniqueFields, boolean bForceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		if (!acceptName(indexName))
			return;
		deleteDocuments(schema, uniqueFields, bForceLocal);
	}

}
