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

package com.jaeksoft.searchlib.index.osse.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.memory.OsseFastStringArray;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray.PointerProvider;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitCollectorInterface;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.jaeksoft.searchlib.util.IOUtils;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseCursor implements PointerProvider, Closeable {

	private final OsseErrorHandler error;

	private Pointer cursorPtr;

	public OsseCursor(OsseIndex index, OsseErrorHandler error, int fieldId,
			OsseFastStringArray terms, int length, int booleanOperator)
			throws SearchLibException {
		this.error = error;
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsQCursor_Create", index.toString(), " ",
				Integer.toString(fieldId), " ", terms.toString(), " ",
				Integer.toString(length), " ",
				Integer.toString(booleanOperator));
		this.cursorPtr = OsseLibrary.OSSCLib_MsQCursor_Create(
				index.getPointer(), fieldId, terms, length, booleanOperator,
				error.getPointer());
		et.end();
		if (cursorPtr == null)
			error.checkNoError();
	}

	public OsseCursor(OsseIndex index, OsseErrorHandler error,
			int booleanOperator, Collection<OsseCursor> cursors)
			throws SearchLibException {
		this.error = error;
		OssePointerArray ossePointerArray = null;
		try {
			// TODO MemoryBuffer
			ossePointerArray = new OssePointerArray(null, cursors);
			ExecutionToken et = FunctionTimer
					.newExecutionToken("OSSCLib_MsQCursor_CreateCombinedCursor");
			this.cursorPtr = OsseLibrary
					.OSSCLib_MsQCursor_CreateCombinedCursor(index.getPointer(),
							ossePointerArray, cursors.size(), booleanOperator,
							error.getPointer());
			et.end();
			if (cursorPtr == null)
				error.checkNoError();
		} finally {
			IOUtils.close(ossePointerArray);
		}
	}

	@Override
	final public void close() {
		if (cursorPtr == null)
			return;
		ExecutionToken et = FunctionTimer
				.newExecutionToken("OSSCLib_MsQCursor_Delete");
		OsseLibrary.OSSCLib_MsQCursor_Delete(cursorPtr);
		et.end();
		cursorPtr = null;
	}

	// TODO optimising by replace long[] by a native malloc implementation
	final public int getDocIds(final long[] docIds, final long startPos)
			throws SearchLibException {
		IntByReference success = new IntByReference();
		ExecutionToken et = FunctionTimer
				.newExecutionToken("OSSCLib_MsQCursor_GetDocIds");
		int count = OsseLibrary.OSSCLib_MsQCursor_GetDocIds(cursorPtr, docIds,
				startPos, false, docIds.length, success, error.getPointer());
		et.end();
		if (success.getValue() == 0)
			error.checkNoError();
		return count;
	}

	final public long getNumberOfDocs() throws SearchLibException {
		IntByReference success = new IntByReference();
		ExecutionToken et = FunctionTimer
				.newExecutionToken("OSSCLib_MsQCursor_GetDocIds");
		long number = OsseLibrary.OSSCLib_MsQCursor_GetNumberOfDocs(cursorPtr,
				success, error.getPointer());
		et.end();
		if (success.getValue() == 0)
			error.checkNoError();
		return number;
	}

	final public void collect(final DocSetHitCollectorInterface collector)
			throws SearchLibException, IOException {
		long bufferSize = getNumberOfDocs();
		if (bufferSize > 10000)
			bufferSize = 10000;
		long[] docIdBuffer = new long[(int) bufferSize];
		long docPosition = 0;
		for (;;) {
			long length = getDocIds(docIdBuffer, docPosition);
			if (length == 0)
				break;
			for (int i = 0; i < length; i++)
				collector.collectDoc((int) docIdBuffer[i]);
			docPosition += length;
		}
	}

	@Override
	public Pointer getPointer() {
		return cursorPtr;
	}
}
