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
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.OsseTermBuffer;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.memory.MemoryBuffer;
import com.jaeksoft.searchlib.index.osse.memory.OsseFastStringArray;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.jaeksoft.searchlib.util.IOUtils;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class OsseTransaction implements Closeable {

	private final static ReentrantLock l = new ReentrantLock();

	private Pointer transactPtr;

	private OsseErrorHandler err;

	final private Map<String, Pointer> transactFieldPtrMap;

	public final static boolean FAKE_MODE = false;

	public final MemoryBuffer memoryBuffer;

	public OsseTransaction(OsseIndex index, int maxBufferSize)
			throws SearchLibException {
		l.lock();
		try {
			memoryBuffer = new MemoryBuffer();
			err = new OsseErrorHandler();
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_Begin ", index.getPointer().toString());
			transactPtr = OsseLibrary.OSSCLib_MsTransact_Begin(
					index.getPointer(), null, maxBufferSize, err.getPointer());
			et.end();
			if (transactPtr == null)
				err.throwError();
			transactFieldPtrMap = new TreeMap<String, Pointer>();
		} finally {
			l.unlock();
		}
	}

	final public void commit() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer
					.newExecutionToken("OSSCLib_MsTransact_Commit");
			if (!OsseLibrary.OSSCLib_MsTransact_Commit(transactPtr, 0, null,
					null, err.getPointer()))
				err.throwError();
			et.end();
			transactPtr = null;
		} finally {
			l.unlock();
		}
		if (FunctionTimer.ACTIVE)
			FunctionTimer.dumpExecutionInfo(true);
	}

	final public int newDocumentId() throws SearchLibException {
		l.lock();
		try {
			if (FAKE_MODE)
				return 0;
			ExecutionToken et = FunctionTimer
					.newExecutionToken("OSSCLib_MsTransact_Document_GetNewDocId");
			int documentId = OsseLibrary
					.OSSCLib_MsTransact_Document_GetNewDocId(transactPtr,
							err.getPointer());
			et.end();
			err.checkNoError();
			if (documentId < 0)
				err.throwError();
			return documentId;
		} finally {
			l.unlock();
		}
	}

	final public int createField(String fieldName, int flag)
			throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_CreateFieldW ", fieldName, " ",
					Integer.toString(flag));
			IntByReference fieldId = new IntByReference();
			Pointer fieldPtr = OsseLibrary.OSSCLib_MsTransact_CreateFieldW(
					transactPtr, new WString(fieldName),
					OsseLibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING, flag, null,
					true, fieldId, err.getPointer());
			et.end();
			err.checkNoError();
			if (fieldPtr == null)
				err.throwError();
			return fieldId.getValue();
		} finally {
			l.unlock();
		}
	}

	public void createField(SchemaField schemaField) throws SearchLibException {
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
		createField(schemaField.getName(), flag);
	}

	final public void deleteField(FieldInfo field) throws SearchLibException {
		OssePointerArray ossePointerArray = null;
		l.lock();
		try {
			ossePointerArray = new OssePointerArray(memoryBuffer,
					getExistingField(field));
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_DeleteFields ", field.name, "(",
					Integer.toString(field.id), ")");
			int i = OsseLibrary.OSSCLib_MsTransact_DeleteFields(transactPtr,
					ossePointerArray, 1, err.getPointer());
			et.end();
			if (i != 1)
				err.throwError();
		} finally {
			l.unlock();
			IOUtils.close(ossePointerArray);
		}
	}

	final private Pointer getExistingField(FieldInfo field)
			throws SearchLibException {
		Pointer transactFieldPtr = transactFieldPtrMap.get(field.name);
		if (transactFieldPtr != null)
			return transactFieldPtr;
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsTransact_GetExistingField ", transactPtr.toString(),
				" ", field.name, "(", Integer.toString(field.id), ")");
		transactFieldPtr = OsseLibrary.OSSCLib_MsTransact_GetExistingField(
				transactPtr, field.id, err.getPointer());
		et.end();
		if (transactFieldPtr == null)
			err.throwError();
		transactFieldPtrMap.put(field.name, transactFieldPtr);
		return transactFieldPtr;
	}

	final public void updateTerms(final int documentId, final FieldInfo field,
			final OsseTermBuffer buffer) throws SearchLibException {
		OsseFastStringArray ofsa = null;
		l.lock();
		try {
			ofsa = new OsseFastStringArray(memoryBuffer, buffer);
			Pointer transactFieldPtr = getExistingField(field);
			if (FAKE_MODE)
				return;
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_Document_AddStringTerms",
					transactFieldPtr.toString(), " ",
					Integer.toString(documentId),
					Integer.toString(buffer.getTermCount()));
			int res = OsseLibrary.OSSCLib_MsTransact_Document_AddStringTerms(
					transactFieldPtr, documentId, ofsa, buffer.getTermCount(),
					err.getPointer());
			et.end();
			if (res != buffer.getTermCount())
				err.throwError();
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (CharacterCodingException e) {
			throw new SearchLibException(e);
		} finally {
			l.unlock();
			IOUtils.close(ofsa);
		}
	}

	final public void rollback() throws SearchLibException {
		l.lock();
		try {
			if (FAKE_MODE)
				return;
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsTransact_RollBack ", transactPtr.toString());
			if (!OsseLibrary.OSSCLib_MsTransact_RollBack(transactPtr,
					err.getPointer()))
				throw new SearchLibException(err.getError());
			et.end();
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	@Override
	final public void close() {
		l.lock();
		try {
			try {
				if (transactPtr != null)
					rollback();
			} catch (SearchLibException e) {
				Logging.warn(e);
			}
			if (err != null) {
				IOUtils.close(err);
				err = null;
			}
		} finally {
			l.unlock();
			memoryBuffer.close();
		}
	}

}
