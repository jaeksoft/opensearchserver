/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.schema.Schema;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class WriterNativeOSSE extends WriterAbstract {

	private ReaderNativeOSSE reader;

	protected WriterNativeOSSE(File configDir, IndexConfig indexConfig,
			ReaderNativeOSSE reader) {
		super(indexConfig);
		this.reader = reader;
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void optimize(String indexName) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {

	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		Pointer doc = OsseLibrary.INSTANCE.document_new();
		for (FieldContent fieldContent : document) {
			WString field = new WString(fieldContent.getField());
			for (String value : fieldContent.getValues()) {
				String[] terms = value.split("\\s");
				if (terms.length > 0) {
					WString[] wTerms = new WString[terms.length];
					int i = 0;
					for (String term : terms)
						wTerms[i++] = new WString(term);
					OsseLibrary.INSTANCE.document_add(doc, field, wTerms,
							wTerms.length);
				}
			}
		}
		OsseLibrary.INSTANCE.index_add(reader.getIndex(), doc);
		OsseLibrary.INSTANCE.document_delete(doc);
		return true;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		int i = 0;
		for (IndexDocument document : documents)
			if (updateDocument(schema, document))
				i++;
		return i;
	}

	@Override
	public void xmlInfo(PrintWriter writer) {
		// TODO Auto-generated method stub

	}

}
