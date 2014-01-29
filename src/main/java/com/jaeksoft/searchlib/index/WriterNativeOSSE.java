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
import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.osse.OsseTermBuffer;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.api.OsseTransaction;
import com.jaeksoft.searchlib.index.osse.memory.MemoryBuffer;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.IOUtils;

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

	public Map<String, FieldInfo> checkSchemaFieldList(
			SchemaFieldList schemaFieldList) throws SearchLibException {
		Map<String, FieldInfo> fieldMap = index.getListOfFields(error);
		List<FieldInfo> fieldsToDelete = new ArrayList<FieldInfo>(0);
		if (fieldMap != null)
			for (FieldInfo fieldInfo : fieldMap.values())
				if (schemaFieldList.get(fieldInfo.name) == null)
					fieldsToDelete.add(fieldInfo);
		List<SchemaField> fieldsToCreate = new ArrayList<SchemaField>(0);
		for (SchemaField schemaField : schemaFieldList)
			if (fieldMap == null || fieldMap.get(schemaField.getName()) == null)
				fieldsToCreate.add(schemaField);
		if (fieldsToDelete.size() == 0 && fieldsToCreate.size() == 0)
			return fieldMap;
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index, null, 0);
			for (FieldInfo fieldToDelete : fieldsToDelete)
				transaction.deleteField(fieldToDelete);
			for (SchemaField schemaField : fieldsToCreate)
				transaction.createField(schemaField);
			transaction.commit();
			return index.getListOfFields(error);
		} finally {
			IOUtils.close(transaction);
		}
	}

	/**
	 * 
	 * @param transaction
	 * @param documentPtr
	 * @param fieldPtr
	 * @param value
	 * @throws SearchLibException
	 * @throws CharacterCodingException
	 */
	final private void updateTerm(final OsseTermBuffer termBuffer,
			final OsseTransaction transaction, final int documentId,
			final FieldInfo field, final String value)
			throws SearchLibException, IOException {
		if (value == null || value.length() == 0)
			return;
		termBuffer.reset();
		termBuffer.addTerm(value);
		// termBuffer.offsets[0].ui32StartOffset = 0;
		// termBuffer.offsets[0].ui32EndOffset = value.length();
		// termBuffer.positionIncrements[0] = 1;
		transaction.updateTerms(documentId, field, termBuffer);
	}

	final private void updateTerms(final OsseTermBuffer termBuffer,
			final OsseTransaction transaction, final int documentId,
			final FieldInfo field, final CompiledAnalyzer compiledAnalyzer,
			final String value) throws IOException, SearchLibException {
		StringReader stringReader = null;
		OsseTokenTermUpdate ottu = null;
		try {
			stringReader = new StringReader(value);
			TokenStream tokenStream = compiledAnalyzer.tokenStream(null,
					stringReader);
			ottu = new OsseTokenTermUpdate(transaction, documentId, field,
					termBuffer, tokenStream);
			while (ottu.incrementToken())
				;
			ottu.close();
		} finally {
			IOUtils.close(stringReader, ottu);
		}
	}

	final private void updateDoc(final OsseTermBuffer termBuffer,
			final OsseTransaction transaction,
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
					updateTerms(termBuffer, transaction, documentId, fieldInfo,
							compiledAnalyzer, value);
				else
					updateTerm(termBuffer, transaction, documentId, fieldInfo,
							value);
			}
		}

	}

	@Override
	final public boolean updateDocument(final Schema schema,
			final IndexDocument document) throws SearchLibException {
		OsseTransaction transaction = null;
		MemoryBuffer memoryBuffer = null;
		try {
			memoryBuffer = new MemoryBuffer();
			OsseTermBuffer osseTermBuffer = new OsseTermBuffer(memoryBuffer,
					1000);
			Map<String, FieldInfo> fieldMap = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index, fieldMap.values(), 1);
			updateDoc(osseTermBuffer, transaction, fieldMap, schema, document);
			transaction.commit();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(transaction);
			IOUtils.close(memoryBuffer);
		}
		return true;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		OsseTransaction transaction = null;
		MemoryBuffer memoryBuffer = null;
		try {
			if (CollectionUtils.isEmpty(documents))
				return 0;
			memoryBuffer = new MemoryBuffer();
			OsseTermBuffer osseTermBuffer = new OsseTermBuffer(memoryBuffer,
					1000);
			Map<String, FieldInfo> fieldMap = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index, fieldMap.values(),
					documents.size());
			int i = 0;
			for (IndexDocument document : documents) {
				updateDoc(osseTermBuffer, transaction, fieldMap, schema,
						document);
				i++;
			}
			transaction.commit();
			return i;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(transaction);
			IOUtils.close(memoryBuffer);
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public int deleteDocument(Schema schema, String field, String value)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteDocuments(Schema schema, String field,
			Collection<String> values) throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteDocuments(AbstractSearchRequest query)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
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

}
