/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.cache;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContentCacheKey;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.Timer;

public class FieldCache extends
		LRUCache<FieldContentCacheKey, FieldValueItem[]> {

	private IndexConfig indexConfig;

	public FieldCache(IndexConfig indexConfig) {
		super("Field cache", indexConfig.getFieldCache());
		this.indexConfig = indexConfig;
	}

	public Map<String, FieldValue> get(ReaderLocal reader, int docId,
			Set<String> fieldNameSet, Timer timer) throws IOException,
			ParseException, SyntaxError {
		Map<String, FieldValue> documentFields = new TreeMap<String, FieldValue>();
		Set<String> storeField = new TreeSet<String>();
		Set<String> indexedField = new TreeSet<String>();
		Set<String> vectorField = new TreeSet<String>();
		Set<String> missingField = new TreeSet<String>();

		// Getting available fields in the cache
		for (String fieldName : fieldNameSet) {
			FieldContentCacheKey key = new FieldContentCacheKey(fieldName,
					docId);
			FieldValueItem[] values = getAndPromote(key);
			if (values != null)
				documentFields
						.put(fieldName, new FieldValue(fieldName, values));
			else
				storeField.add(fieldName);
		}

		// Check missing fields from store
		if (storeField.size() > 0) {
			Document document = reader.getDocFields(docId, storeField);
			for (String fieldName : storeField) {
				Fieldable[] fieldables = document.getFieldables(fieldName);
				if (fieldables != null && fieldables.length > 0) {
					FieldValueItem[] valueItems = FieldValueItem
							.buildArray(fieldables);
					put(documentFields, fieldName, docId, valueItems);
				} else
					indexedField.add(fieldName);
			}
		}

		// Check missing fields from StringIndex
		if (indexedField.size() > 0) {
			for (String fieldName : indexedField) {
				StringIndex stringIndex = reader.getStringIndex(fieldName);
				if (stringIndex != null) {
					String term = stringIndex.lookup[stringIndex.order[docId]];
					if (term != null) {
						FieldValueItem[] valueItems = FieldValueItem
								.buildArray(FieldValueOriginEnum.STRING_INDEX,
										term);
						put(documentFields, fieldName, docId, valueItems);
						continue;
					}
				}
				vectorField.add(fieldName);
			}
		}

		// Check missing fields from vector
		if (vectorField.size() > 0) {
			for (String fieldName : vectorField) {
				TermFreqVector tfv = reader.getTermFreqVector(docId, fieldName);
				if (tfv != null) {
					FieldValueItem[] valueItems = FieldValueItem.buildArray(
							FieldValueOriginEnum.TERM_VECTOR, tfv.getTerms());
					put(documentFields, fieldName, docId, valueItems);
				} else
					missingField.add(fieldName);
			}
		}

		if (missingField.size() > 0)
			for (String fieldName : missingField)
				documentFields.put(fieldName, new FieldValue(fieldName));

		return documentFields;
	}

	private void put(Map<String, FieldValue> documentFields, String fieldName,
			int docId, FieldValueItem[] valueItems) {
		documentFields.put(fieldName, new FieldValue(fieldName, valueItems));
		FieldContentCacheKey key = new FieldContentCacheKey(fieldName, docId);
		put(key, valueItems);
	}

	@Override
	public void setMaxSize(int newMaxSize) {
		super.setMaxSize(newMaxSize);
		indexConfig.setFieldCache(newMaxSize);
	}
}
