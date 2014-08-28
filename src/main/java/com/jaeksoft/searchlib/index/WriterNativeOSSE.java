/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.api.OsseJNALibrary;
import com.jaeksoft.searchlib.index.osse.api.OsseTransaction;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

public class WriterNativeOSSE extends WriterAbstract {

	private final OsseIndex index;
	private final OsseErrorHandler error;

	protected WriterNativeOSSE(OsseIndex index, IndexConfig indexConfig)
			throws SearchLibException {
		super(indexConfig);
		this.index = index;
		error = new OsseErrorHandler();
	}

	@Override
	public void optimize() {

	}

	final static int getFieldFlag(SchemaField schemaField)
			throws SearchLibException {
		int flag = 0;
		switch (schemaField.getTermVector()) {
		case YES:
			flag |= OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1;
			break;
		case POSITIONS_OFFSETS:
			flag |= OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1
					| OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_TERMFREQ
					| OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET
					| OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION;
			break;
		default:
			break;
		}
		switch (schemaField.getStored()) {
		case YES:
			flag |= OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_STORED;
			break;
		case COMPRESS:
			flag |= OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_STORED_COMPRESSED;
			break;
		default:
			break;
		}
		return flag;
	}

	public Map<String, FieldInfo> checkSchemaFieldList(
			SchemaFieldList schemaFieldList) throws SearchLibException {
		Map<String, FieldInfo> fieldMap = index.getListOfFields(error);
		Map<String, FieldInfo> fieldsToDelete = new TreeMap<String, FieldInfo>();
		if (fieldMap != null)
			for (FieldInfo fieldInfo : fieldMap.values())
				if (schemaFieldList.get(fieldInfo.name) == null)
					fieldsToDelete.put(fieldInfo.name, fieldInfo);
		List<SchemaField> fieldsToCreate = new ArrayList<SchemaField>(0);
		for (SchemaField schemaField : schemaFieldList) {
			if (fieldMap != null) {
				FieldInfo fieldInfo = fieldMap.get(schemaField.getName());
				if (fieldInfo != null) {
					if (fieldInfo.flags == getFieldFlag(schemaField))
						continue;
					fieldsToDelete.put(fieldInfo.name, fieldInfo);
				}
			}
			fieldsToCreate.add(schemaField);
		}
		if (fieldsToDelete.size() == 0 && fieldsToCreate.size() == 0)
			return fieldMap;
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index, null, null, 0);
			for (FieldInfo fieldToDelete : fieldsToDelete.values())
				transaction.deleteField(fieldToDelete);
			for (SchemaField schemaField : fieldsToCreate)
				transaction.createField(schemaField.getName(),
						getFieldFlag(schemaField));
			transaction.commit();
			return index.getListOfFields(error);
		} finally {
			IOUtils.close(transaction);
		}
	}

	final private void updateDoc(final OsseTransaction transaction,
			final Map<String, FieldInfo> fieldMap, final Schema schema,
			final IndexDocument document) throws SearchLibException,
			IOException {
		int documentId = transaction.newDocumentId();
		LanguageEnum lang = document.getLang();
		for (FieldContent fieldContent : document) {
			SchemaField schemaField = schema.getFieldList().get(
					fieldContent.getField());
			if (schemaField == null)
				throw new SearchLibException("Unknown field: "
						+ fieldContent.getField());
			Analyzer analyzer = schema.getAnalyzer(schemaField, lang);
			CompiledAnalyzer compiledAnalyzer = analyzer != null ? analyzer
					.getIndexAnalyzer() : null;
			FieldInfo fieldInfo = fieldMap.get(schemaField.getName());
			if (fieldInfo == null)
				continue;
			for (FieldValueItem valueItem : fieldContent.getValues()) {
				String value = valueItem.getValue();
				if (compiledAnalyzer != null)
					transaction.updateTerms(documentId, fieldInfo,
							compiledAnalyzer, value);
				else
					transaction.updateTerm(documentId, fieldInfo, value);
			}
		}
	}

	@Override
	final public boolean updateDocument(final Schema schema,
			final IndexDocument document) throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			Map<String, FieldInfo> fieldMap = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index, null, fieldMap.values(), 1);
			updateDoc(transaction, fieldMap, schema, document);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(transaction);
		}
		return true;
	}

	@Deprecated
	final public boolean updateDocument_(final Schema schema,
			final IndexDocument document) throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			Map<String, FieldInfo> fieldMap = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index, null, fieldMap.values(), 1);
			transaction.updateDocument_(schema, document);
			transaction.commit();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(transaction);
		}
		return true;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			if (CollectionUtils.isEmpty(documents))
				return 0;
			Map<String, FieldInfo> fieldMap = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index, null, fieldMap.values(),
					documents.size());
			int i = 0;
			for (IndexDocument document : documents) {
				updateDoc(transaction, fieldMap, schema, document);
				i++;
			}
			transaction.commit();
			return i;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(transaction);
		}
	}

	@Override
	public long deleteDocuments(AbstractRequest request)
			throws SearchLibException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteAll() throws SearchLibException {
		index.deleteAll(error);
	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		index.close(error);
		IOUtils.close(error);
	}

	@Override
	public int updateIndexDocuments(Schema schema,
			Collection<IndexDocumentResult> documents)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

}
