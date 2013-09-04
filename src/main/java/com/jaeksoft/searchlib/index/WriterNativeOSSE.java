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
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.osse.OsseFieldList;
import com.jaeksoft.searchlib.index.osse.OsseFieldList.FieldInfo;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.index.osse.OsseTermOffset;
import com.jaeksoft.searchlib.index.osse.OsseTransaction;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class WriterNativeOSSE extends WriterAbstract {

	private OsseIndex index;

	protected WriterNativeOSSE(OsseIndex index, IndexConfig indexConfig) {
		super(indexConfig);
		this.index = index;
	}

	@Override
	public void optimize() {

	}

	private void checkFieldCreation(OsseTransaction transaction,
			SchemaField schemaField) throws SearchLibException {
		String fieldName = schemaField.getName();
		Pointer indexField = transaction.getFieldPointer(fieldName);
		if (indexField != null)
			return;
		int flag = 0;
		switch (schemaField.getTermVector()) {
		case YES:
			flag += OsseLibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1;
			break;
		case POSITIONS_OFFSETS:
			flag = OsseLibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1
					| OsseLibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET
					| OsseLibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION;
			break;
		default:
			break;
		}
		transaction.createField(fieldName, flag);
		System.out.println("FIELD CREATED: " + fieldName);
	}

	public void checkSchemaFieldList(SchemaFieldList schemaFieldList)
			throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index);
			for (SchemaField schemaField : schemaFieldList)
				checkFieldCreation(transaction, schemaField);
			OsseFieldList osseFieldList = new OsseFieldList(index,
					transaction.getError());
			for (FieldInfo fieldInfo : osseFieldList.collection())
				if (schemaFieldList.get(fieldInfo.name) == null)
					transaction.deleteField(fieldInfo.name);
			transaction.commit();
		} finally {
			if (transaction != null)
				transaction.release();
		}
	}

	/**
	 * Should be replaced by updateTerms with keywordAnalyzer (to embed offset
	 * and posincr)
	 * 
	 * @param transaction
	 * @param documentPtr
	 * @param fieldPtr
	 * @param value
	 * @throws SearchLibException
	 */
	final private void updateTerm(OsseTransaction transaction,
			Pointer documentPtr, Pointer fieldPtr, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		WString[] terms = { new WString(value) };
		transaction.updateTerms(documentPtr, fieldPtr, terms, null, null, 1);
	}

	final private static int TERM_BUFFER_SIZE = 100;

	final private void updateTerms(OsseTransaction transaction,
			Pointer documentPtr, Pointer fieldPtr,
			CompiledAnalyzer compiledAnalyzer, String value)
			throws IOException, SearchLibException {
		StringReader stringReader = null;
		try {
			stringReader = new StringReader(value);
			TokenStream tokenStream = compiledAnalyzer.tokenStream(null,
					stringReader);
			WString[] terms = new WString[TERM_BUFFER_SIZE];
			OsseTermOffset[] offsets = OsseTermOffset
					.getNewArray(TERM_BUFFER_SIZE);
			int[] positionIncrements = new int[TERM_BUFFER_SIZE];
			int length = 0;
			while (tokenStream.incrementToken()) {
				terms[length] = new WString("TODO"); // TODO new
				// WString(tokenStream.getCurrentTerm());
				/*
				 * TokenAttributes attr = tokenStream.getAttributes(); if
				 * (attr.positionIncrement != null) positionIncrements[length] =
				 * attr.positionIncrement; if (attr.offsetStart != null)
				 * offsets[length].set(attr); length++;
				 */
				if (length == TERM_BUFFER_SIZE) {
					transaction.updateTerms(documentPtr, fieldPtr, terms,
							offsets, positionIncrements, length);
					length = 0;
				}
			}
			if (length > 0)
				transaction.updateTerms(documentPtr, fieldPtr, terms, offsets,
						positionIncrements, length);
		} finally {
			if (stringReader != null)
				IOUtils.closeQuietly(stringReader);
		}
	}

	private void updateDoc(OsseTransaction transaction, Schema schema,
			IndexDocument document) throws SearchLibException, IOException {
		Pointer documentPtr = transaction.newDocumentPointer();
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
			Pointer fieldPtr = transaction.getFieldPointer(fieldContent
					.getField());
			if (fieldPtr == null)
				transaction.throwError();
			for (FieldValueItem valueItem : fieldContent.getValues()) {
				String value = valueItem.getValue();
				if (compiledAnalyzer != null)
					updateTerms(transaction, documentPtr, fieldPtr,
							compiledAnalyzer, value);
				else
					updateTerm(transaction, documentPtr, fieldPtr, value);
			}
		}

	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		OsseTransaction transaction = null;
		try {
			transaction = new OsseTransaction(index);
			updateDoc(transaction, schema, document);
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
			transaction = new OsseTransaction(index);
			transaction.reserveExtraSpace(documents.size(), 0);
			int i = 0;
			for (IndexDocument document : documents) {
				updateDoc(transaction, schema, document);
				i++;
			}
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
