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

public interface WriterInterface {

	public void deleteDocuments(String indexName, Schema schema,
			String uniqueField, boolean bForceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException;

	public void deleteDocuments(Schema schema, String uniqueField,
			boolean bForceLocal) throws CorruptIndexException,
			LockObtainFailedException, IOException;

	public void updateDocument(Schema schema, IndexDocument document,
			boolean bForceLocal) throws NoSuchAlgorithmException, IOException;

	public void updateDocument(String indexName, Schema schema,
			IndexDocument document, boolean bForceLocal)
			throws NoSuchAlgorithmException, IOException;

	public void updateDocuments(Schema schema,
			List<? extends IndexDocument> documents, boolean bForceLocal)
			throws NoSuchAlgorithmException, IOException;

	public void updateDocuments(String indexName, Schema schema,
			List<? extends IndexDocument> documents, boolean bForceLocal)
			throws NoSuchAlgorithmException, IOException;

	public void optimize(String indexName, boolean bForceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException;

}
