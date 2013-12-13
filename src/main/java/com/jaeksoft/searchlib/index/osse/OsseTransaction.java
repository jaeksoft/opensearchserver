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

package com.jaeksoft.searchlib.index.osse;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.OsseFieldList.FieldInfo;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate.TermBuffer;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class OsseTransaction {

	private final static ReentrantLock l = new ReentrantLock();

	private Pointer transactPtr;

	private OsseErrorHandler err;

	final private Map<String, Pointer> transactFieldPtrMap;

	public OsseTransaction(OsseIndex index) throws SearchLibException {
		l.lock();
		try {
			err = new OsseErrorHandler();
			ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
					"OSSCLib_MsTransact_Begin ", index.getPointer().toString());
			transactPtr = OsseLibrary.INSTANCE.OSSCLib_MsTransact_Begin(
					index.getPointer(), null, err.getPointer());
			et.end();
			if (transactPtr == null)
				throwError();
			transactFieldPtrMap = new TreeMap<String, Pointer>();
		} finally {
			l.unlock();
		}
	}

	final public void commit() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_MsTransact_Commit");
			if (!OsseLibrary.INSTANCE.OSSCLib_MsTransact_Commit(transactPtr, 0,
					null, null, err.getPointer()))
				throwError();
			et.end();
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	final public int newDocumentId() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_MsTransact_Document_GetNewDocId");
			int documentId = OsseLibrary.INSTANCE
					.OSSCLib_MsTransact_Document_GetNewDocId(transactPtr,
							err.getPointer());
			et.end();
			err.checkNoError();
			if (documentId < 0)
				throwError();
			return documentId;
		} finally {
			l.unlock();
		}
	}

	final public int createField(String fieldName, int flag)
			throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
					"OSSCLib_MsTransact_CreateFieldW ", fieldName, " ",
					Integer.toString(flag));
			IntByReference fieldId = new IntByReference();
			Pointer fieldPtr = OsseLibrary.INSTANCE
					.OSSCLib_MsTransact_CreateFieldW(transactPtr, new WString(
							fieldName),
							OsseLibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING,
							flag, null, true, fieldId, err.getPointer());
			et.end();
			err.checkNoError();
			if (fieldPtr == null)
				throwError();
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
		l.lock();
		try {
			Pointer[] fieldsPtr = { getExistingField(field) };
			ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
					"OSSCLib_MsTransact_DeleteFields ", field.name, "(",
					Integer.toString(field.id), ")");
			int i = OsseLibrary.INSTANCE.OSSCLib_MsTransact_DeleteFields(
					transactPtr, fieldsPtr, 1, err.getPointer());
			et.end();
			if (i != fieldsPtr.length)
				throwError();
		} finally {
			l.unlock();
		}
	}

	final private Pointer getExistingField(FieldInfo field)
			throws SearchLibException {
		Pointer transactFieldPtr = transactFieldPtrMap.get(field.name);
		if (transactFieldPtr != null)
			return transactFieldPtr;
		ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
				"OSSCLib_MsTransact_GetExistingField ", transactPtr.toString(),
				" ", field.name, "(", Integer.toString(field.id), ")");
		transactFieldPtr = OsseLibrary.INSTANCE
				.OSSCLib_MsTransact_GetExistingField(transactPtr, field.id,
						err.getPointer());
		et.end();
		if (transactFieldPtr == null)
			throwError();
		transactFieldPtrMap.put(field.name, transactFieldPtr);
		return transactFieldPtr;
	}

	final public void updateTerms(int documentId, FieldInfo field,
			TermBuffer buffer, int length) throws SearchLibException {
		l.lock();
		try {
			Pointer transactFieldPtr = getExistingField(field);
			ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
					"OSSCLib_MsTransact_Document_AddStringTermsW ",
					transactFieldPtr.toString(), " ",
					Integer.toString(documentId), " [",
					Integer.toString(buffer.terms.length), "] ",
					Integer.toString(length));
			int res = OsseLibrary.INSTANCE
					.OSSCLib_MsTransact_Document_AddStringTermsW(
							transactFieldPtr, documentId, buffer.terms, length,
							err.getPointer());
			et.end();
			if (res != length)
				throwError();
		} finally {
			l.unlock();
		}
	}

	final public void rollback() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
					"OSSCLib_MsTransact_RollBack ", transactPtr.toString());
			if (!OsseLibrary.INSTANCE.OSSCLib_MsTransact_RollBack(transactPtr,
					err.getPointer()))
				throw new SearchLibException(err.getError());
			et.end();
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	final public void release() {
		l.lock();
		try {
			try {
				if (transactPtr != null)
					rollback();
			} catch (SearchLibException e) {
				Logging.warn(e);
			}
			if (err != null) {
				err.release();
				err = null;
			}
		} finally {
			l.unlock();
		}
	}

	final public void throwError() throws SearchLibException {
		l.lock();
		try {
			throw new SearchLibException(err.getError());
		} finally {
			l.unlock();
		}
	}

	public OsseErrorHandler getError() {
		l.lock();
		try {
			return err;
		} finally {
			l.unlock();
		}
	}

}
