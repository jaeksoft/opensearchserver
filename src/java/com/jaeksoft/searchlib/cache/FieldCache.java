/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cache;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContentCacheKey;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class FieldCache extends
		LRUCache<FieldContentCacheKey, FieldValueItem[]> {

	public FieldCache(int maxSize) {
		super(maxSize);
	}

	public FieldList<FieldValue> get(ReaderLocal reader, int docId,
			FieldList<Field> fieldList) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		FieldList<FieldValue> documentFields = new FieldList<FieldValue>();
		FieldList<Field> missingField = new FieldList<Field>();

		// Getting available fields
		for (Field field : fieldList) {
			FieldContentCacheKey key = new FieldContentCacheKey(
					field.getName(), docId);
			FieldValueItem[] values = getAndPromote(key);
			if (values != null) {
				documentFields.add(new FieldValue(field, values));
			} else
				missingField.add(field);
		}

		// Check missing fields
		if (missingField.size() > 0) {
			Document document = reader.getDocFields(docId, missingField);
			for (Field field : missingField) {
				FieldContentCacheKey key = new FieldContentCacheKey(
						field.getName(), docId);
				Fieldable[] fieldables = document
						.getFieldables(field.getName());
				if (fieldables != null) {
					FieldValueItem[] valueItems = FieldValueItem
							.buildArray(fieldables);
					documentFields.add(new FieldValue(field, valueItems));
					put(key, valueItems);
				}
			}
		}
		return documentFields;

	}
}
