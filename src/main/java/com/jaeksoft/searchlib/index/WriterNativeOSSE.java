/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.osse.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.OsseFieldList;
import com.jaeksoft.searchlib.index.osse.OsseFieldList.FieldInfo;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate.TermBuffer;
import com.jaeksoft.searchlib.index.osse.OsseTransaction;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.IOUtils;
import com.sun.jna.WString;

public class WriterNativeOSSE extends WriterAbstract {

	private OsseIndex index;
	private OsseErrorHandler error;
	private TermBuffer termBuffer = null;

	protected WriterNativeOSSE(OsseIndex index, IndexConfig indexConfig) {
		super(indexConfig);
		this.index = index;
		error = new OsseErrorHandler();
		termBuffer = new TermBuffer(100);
	}

	@Override
	public void optimize() {

	}

	public OsseFieldList checkSchemaFieldList(SchemaFieldList schemaFieldList)
			throws SearchLibException {
		OsseFieldList osseFieldList = new OsseFieldList(index, error);
		List<FieldInfo> fieldsToDelete = new ArrayList<FieldInfo>(0);
		for (FieldInfo fieldInfo : osseFieldList.collection())
			if (schemaFieldList.get(fieldInfo.name) == null)
				fieldsToDelete.add(fieldInfo);
		List<SchemaField> fieldsToCreate = new ArrayList<SchemaField>(0);
		for (SchemaField schemaField : schemaFieldList)
			if (osseFieldList.getFieldInfo(schemaField.getName()) == null)
				fieldsToCreate.add(schemaField);
		if (fieldsToDelete.size() == 0 && fieldsToCreate.size() == 0)
			return osseFieldList;
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index);
			for (FieldInfo fieldToDelete : fieldsToDelete)
				transaction.deleteField(fieldToDelete);
			for (SchemaField schemaField : fieldsToCreate)
				transaction.createField(schemaField);
			transaction.commit();
			return new OsseFieldList(index, error);
		} finally {
			if (transaction != null)
				transaction.release();
		}
	}

	/**
	 * 
	 * @param transaction
	 * @param documentPtr
	 * @param fieldPtr
	 * @param value
	 * @throws SearchLibException
	 */
	final private void updateTerm(final OsseTransaction transaction,
			final int documentId, final FieldInfo field, final String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		termBuffer.terms[0] = new WString(value);
		termBuffer.offsets[0].ui32StartOffset = 0;
		termBuffer.offsets[0].ui32EndOffset = value.length();
		termBuffer.positionIncrements[0] = 1;
		transaction.updateTerms(documentId, field, termBuffer, 1);
	}

	final private void updateTerms(final OsseTransaction transaction,
			final int documentId, final FieldInfo field,
			final CompiledAnalyzer compiledAnalyzer, final String value)
			throws IOException, SearchLibException {
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

	private void updateDoc(OsseTransaction transaction,
			OsseFieldList osseFieldList, Schema schema, IndexDocument document)
			throws SearchLibException, IOException {
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
			FieldInfo fieldInfo = osseFieldList.getFieldInfo(schemaField
					.getName());
			if (fieldInfo == null)
				continue;
			for (FieldValueItem valueItem : fieldContent.getValues()) {
				String value = valueItem.getValue();
				if (compiledAnalyzer != null)
					updateTerms(transaction, documentId, fieldInfo,
							compiledAnalyzer, value);
				else
					updateTerm(transaction, documentId, fieldInfo, value);
			}
		}

	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			OsseFieldList osseFieldList = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index);
			updateDoc(transaction, osseFieldList, schema, document);
			transaction.commit();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			if (transaction != null)
				transaction.release();
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
			OsseFieldList osseFieldList = checkSchemaFieldList(schema
					.getFieldList());
			transaction = new OsseTransaction(index);
			int i = 0;
			for (IndexDocument document : documents) {
				updateDoc(transaction, osseFieldList, schema, document);
				i++;
			}
			transaction.commit();
			return i;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			if (transaction != null)
				transaction.release();
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
		// TODO Auto-generated method stub

	}

}
