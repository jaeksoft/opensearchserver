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

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;

import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;

public class DocumentCacheItem {

	private FieldList<FieldValue> documentFields;
	private String key;
	private transient int docId;

	@SuppressWarnings("unchecked")
	public DocumentCacheItem(String key, int docId, Request request,
			Document document) throws IOException {
		documentFields = (FieldList<FieldValue>) request.getDocumentFieldList()
				.clone();
		for (FieldValue field : documentFields)
			field.addValues(document.getValues(field.getName()));
		this.key = key;
		this.docId = docId;
	}

	public String getKey() {
		return key;
	}

	public ArrayList<String> getValues(Field field) {
		return documentFields.get(field.getName()).getValues();
	}

	public int getDocId() {
		return docId;
	}

}
