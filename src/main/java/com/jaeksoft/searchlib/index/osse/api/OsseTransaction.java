/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.osse.OsseTermBuffer;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.memory.DocumentRecord;
import com.jaeksoft.searchlib.index.osse.memory.MemoryBuffer;
import com.jaeksoft.searchlib.index.osse.memory.OsseFastStringArray;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.jaeksoft.searchlib.util.IOUtils;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseTransaction implements Closeable {

	private long transactPtr;

	private OsseErrorHandler err;

	public final MemoryBuffer memoryBuffer;

	private final Map<String, Long> fieldPointerMap;

	public OsseTransaction(OsseIndex index, Collection<FieldInfo> fieldInfos,
			int maxBufferSize) throws SearchLibException {
		memoryBuffer = new MemoryBuffer();
		err = new OsseErrorHandler();
		final ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsTransact_Begin ", Long.toString(index.getPointer()));
		transactPtr = OsseIndex.LIB.OSSCLib_MsTransact_Begin(
				index.getPointer(), null, maxBufferSize, err.getPointer());
		et.end();
		if (transactPtr == 0)
			err.throwError();
		if (fieldInfos != null) {
			fieldPointerMap = new TreeMap<String, Long>();
			for (FieldInfo fieldInfo : fieldInfos)
				fieldPointerMap
						.put(fieldInfo.name, getExistingField(fieldInfo));
		} else
			fieldPointerMap = null;
	}

	final public void commit() throws SearchLibException {
		final ExecutionToken et = FunctionTimer
				.newExecutionToken("OSSCLib_MsTransact_Commit");
		if (!OsseIndex.LIB.OSSCLib_MsTransact_Commit(transactPtr, 0, 0, 0,
				err.getPointer()))
			err.throwError();
		et.end();
		transactPtr = 0;
		if (FunctionTimer.MODE != FunctionTimer.Mode.OFF)
			FunctionTimer.dumpExecutionInfo(true);
	}

	@Deprecated
	final public int newDocumentId() throws SearchLibException {
		final ExecutionToken et = FunctionTimer
				.newExecutionToken("OSSCLib_MsTransact_Document_GetNewDocId");
		int documentId = OsseIndex.LIB.OSSCLib_MsTransact_Document_GetNewDocId(
				transactPtr, err.getPointer());
		et.end();
		err.checkNoError();
		if (documentId < 0)
			err.throwError();
		return documentId;
	}

	final public int createField(String fieldName, int flag)
			throws SearchLibException {
		final ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsTransact_CreateFieldW ", fieldName, " ",
				Integer.toString(flag));
		IntByReference fieldId = new IntByReference();
		long fieldPtr = OsseIndex.LIB.OSSCLib_MsTransact_CreateFieldW(
				transactPtr, fieldName,
				OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING, flag,
				(long) 0, true, Memory.nativeValue(fieldId.getPointer()),
				err.getPointer());
		et.end();
		err.checkNoError();
		if (fieldPtr == 0)
			err.throwError();
		return fieldId.getValue();
	}

	public void createField(SchemaField schemaField) throws SearchLibException {
		int flag = 0;
		switch (schemaField.getTermVector()) {
		case YES:
			flag += OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1;
			break;
		case POSITIONS_OFFSETS:
			flag = OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1
					| OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET
					| OsseJNALibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION;
			break;
		default:
			break;
		}
		createField(schemaField.getName(), flag);
	}

	final public void deleteField(FieldInfo field) throws SearchLibException {
		OssePointerArray ossePointerArray = null;
		try {
			ossePointerArray = new OssePointerArray(memoryBuffer, new Pointer(
					getExistingField(field)));
			final ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_DeleteFields ", field.name, "(",
					Integer.toString(field.id), ")");
			int i = OsseIndex.LIB.OSSCLib_MsTransact_DeleteFields(transactPtr,
					Memory.nativeValue(ossePointerArray), 1, err.getPointer());
			et.end();
			if (i != 1)
				err.throwError();
		} finally {
			IOUtils.close(ossePointerArray);
		}
	}

	final private long getExistingField(FieldInfo field)
			throws SearchLibException {
		final ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsTransact_GetExistingField ",
				Long.toString(transactPtr), " ", field.name, "(",
				Integer.toString(field.id), ")");
		long transactFieldPtr = OsseIndex.LIB
				.OSSCLib_MsTransact_GetExistingField(transactPtr, field.id,
						err.getPointer());
		et.end();
		if (transactFieldPtr == 0)
			err.throwError();
		return transactFieldPtr;
	}

	// final public void updateTerms(final int documentId,
	// final FieldInfo fieldInfo, final OsseTermBuffer buffer)
	// throws SearchLibException {
	// final ExecutionToken et = FunctionTimer
	// .newExecutionToken("OSSCLib_MsTransact_Document_AddStringTerms");
	// final int termCount = buffer.getTermCount();
	// final int res = OsseIndex.LIB
	// .OSSCLib_MsTransact_Document_AddStringTermsJ(
	// Memory.nativeValue(fieldPointerMap.get(fieldInfo.name)),
	// documentId, buffer.getTerms(), termCount,
	// err.getPointer());
	// et.end();
	// if (res != termCount)
	// err.throwError();
	// }

	@Deprecated
	final public void updateTerms_(final int documentId,
			final FieldInfo fieldInfo, final OsseTermBuffer buffer)
			throws SearchLibException {
		OsseFastStringArray ofsa = null;
		try {
			final int termCount = buffer.getTermCount();
			ofsa = new OsseFastStringArray(memoryBuffer, buffer);
			final ExecutionToken et = FunctionTimer
					.newExecutionToken("OSSCLib_MsTransact_Document_AddStringTerms");
			final int res = OsseIndex.LIB
					.OSSCLib_MsTransact_Document_AddStringTerms(
							fieldPointerMap.get(fieldInfo.name), documentId,
							Memory.nativeValue(ofsa), termCount,
							err.getPointer());
			et.end();
			if (res != termCount)
				err.throwError();
		} finally {
			if (ofsa != null)
				ofsa.close();
		}
	}

	final public void updateDocument(final MemoryBuffer memoryBuffer,
			final OsseTermBuffer termBuffer, final Schema schema,
			final IndexDocument indexDocument) throws SearchLibException,
			IOException {
		DocumentRecord documentRecord = null;
		try {
			termBuffer.reset();
			documentRecord = new DocumentRecord(memoryBuffer, termBuffer,
					schema, fieldPointerMap, indexDocument);
			final ExecutionToken et = FunctionTimer
					.newExecutionToken("OSSCLib_MsTransact_AddEntireNewDocument");
			final int res = OsseIndex.LIB
					.OSSCLib_MsTransact_AddEntireNewDocument(transactPtr,
							documentRecord.getNumberOfField(),
							documentRecord.getFieldPtrArrayPointer(),
							documentRecord.getNumberOfTermsArrayPointer(),
							documentRecord.getTermArrayPointer(),
							err.getPointer());
			et.end();
			if (res == 0)
				err.throwError();
		} finally {
			IOUtils.close(documentRecord);
		}
	}

	final public void rollback() throws SearchLibException {
		final ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsTransact_RollBack ", Long.toString(transactPtr));
		if (!OsseIndex.LIB.OSSCLib_MsTransact_RollBack(transactPtr,
				err.getPointer()))
			throw new SearchLibException(err.getError());
		et.end();
		transactPtr = 0;
	}

	@Override
	final public void close() {
		try {
			try {
				if (transactPtr != 0)
					rollback();
			} catch (SearchLibException e) {
				Logging.warn(e);
			}
			if (err != null) {
				IOUtils.close(err);
				err = null;
			}
		} finally {
			memoryBuffer.close();
		}
	}

}
