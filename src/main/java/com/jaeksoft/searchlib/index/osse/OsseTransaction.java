/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class OsseTransaction {

	private final static ReentrantLock l = new ReentrantLock();

	private Pointer transactPtr;

	private OsseErrorHandler err;

	private final Map<String, Pointer> fieldPointerMap;

	public OsseTransaction(OsseIndex index) {
		l.lock();
		try {
			fieldPointerMap = new TreeMap<String, Pointer>();
			err = new OsseErrorHandler();
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_Begin");
			transactPtr = OsseLibrary.INSTANCE.OSSCLib_Transact_Begin(
					index.getPointer(), err.getPointer());
			et.end();
		} finally {
			l.unlock();
		}
	}

	final public void reserveExtraSpace(int newDocs, int existingDocs)
			throws SearchLibException {
		l.lock();
		try {
			err = new OsseErrorHandler();
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_ReserveExtraSpaceForDocHandles");
			int res = OsseLibrary.INSTANCE
					.OSSCLib_Transact_ReserveExtraSpaceForDocHandles(
							transactPtr, newDocs, existingDocs,
							err.getPointer());
			et.end();
			if (res == 0)
				throwError();
		} finally {
			l.unlock();
		}
	}

	final public Pointer getFieldPointer(String fieldName) {
		l.lock();
		try {
			Pointer fieldPtr = fieldPointerMap.get(fieldName);
			if (fieldPtr != null)
				return fieldPtr;
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_GetField");
			fieldPtr = OsseLibrary.INSTANCE.OSSCLib_Transact_GetField(
					transactPtr, new WString(fieldName), err.getPointer());
			et.end();
			fieldPointerMap.put(fieldName, fieldPtr);
			return fieldPtr;
		} finally {
			l.unlock();
		}
	}

	final public void commit() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_Commit");
			if (!OsseLibrary.INSTANCE.OSSCLib_Transact_Commit(transactPtr,
					null, 0, null, err.getPointer()))
				throw new SearchLibException(err.getError());
			et.end();
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	final public Pointer newDocumentPointer() throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_Document_New");
			Pointer documentPtr = OsseLibrary.INSTANCE
					.OSSCLib_Transact_Document_New(transactPtr,
							err.getPointer());
			et.end();
			if (documentPtr == null)
				throwError();
			return documentPtr;
		} finally {
			l.unlock();
		}
	}

	final public Pointer createField(String fieldName, int flag)
			throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_CreateField");
			Pointer fieldPtr = OsseLibrary.INSTANCE
					.OSSCLib_Transact_CreateField(transactPtr, new WString(
							fieldName),
							OsseLibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING,
							flag, null, err.getPointer());
			et.end();
			if (fieldPtr == null)
				throwError();
			return fieldPtr;
		} finally {
			l.unlock();
		}
	}

	final public void deleteField(String fieldName) throws SearchLibException {
		l.lock();
		try {
			WString[] wTerms = { new WString(fieldName) };
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_DeleteFields");
			int i = OsseLibrary.INSTANCE.OSSCLib_Transact_DeleteFields(
					transactPtr, wTerms, 1, err.getPointer());
			et.end();
			if (i != 1)
				throwError();
		} finally {
			l.unlock();
		}
	}

	final public void updateTerms(Pointer documentPtr, Pointer fieldPtr,
			WString[] terms, OsseTermOffset[] offsets,
			int[] positionIncrements, int length) throws SearchLibException {
		l.lock();
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_Document_AddStringTerms");
			int res = OsseLibrary.INSTANCE
					.OSSCLib_Transact_Document_AddStringTerms(transactPtr,
							documentPtr, fieldPtr, terms, offsets,
							positionIncrements, null, length, err.getPointer());
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
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Transact_Commit");
			if (!OsseLibrary.INSTANCE.OSSCLib_Transact_Commit(transactPtr,
					null, 0, null, err.getPointer()))
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
