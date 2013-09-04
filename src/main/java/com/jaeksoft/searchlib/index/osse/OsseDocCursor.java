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

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class OsseDocCursor {

	private Pointer docCursorPtr;
	private OsseErrorHandler error;

	public OsseDocCursor(OsseIndex index, OsseErrorHandler error)
			throws SearchLibException {
		this.error = error;
		ExecutionToken et = FunctionTimer.INSTANCE
				.newExecutionToken("OSSCLib_DocTCursor_Create");
		docCursorPtr = OsseLibrary.INSTANCE.OSSCLib_DocTCursor_Create(
				index.getPointer(), error.getPointer());
		et.end();
		if (docCursorPtr == null)
			throw new SearchLibException(error.getError());
	}

	private final void addTerm(List<FieldValueItem> list, WString term) {
		list.add(new FieldValueItem(FieldValueOriginEnum.TERM_ENUM, term
				.toString()));
	}

	public List<FieldValueItem> getTerms(Pointer indexFieldPtr, long docId) {
		ExecutionToken et = FunctionTimer.INSTANCE
				.newExecutionToken("OSSCLib_DocTCursor_FindFirstTerm");
		WString term = OsseLibrary.INSTANCE.OSSCLib_DocTCursor_FindFirstTerm(
				docCursorPtr, indexFieldPtr, docId, error.getPointer());
		et.end();
		if (term == null)
			return null;
		List<FieldValueItem> list = new ArrayList<FieldValueItem>(0);
		addTerm(list, term);
		IntByReference bError = new IntByReference();
		for (;;) {
			et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_DocTCursor_FindNextTerm");
			term = OsseLibrary.INSTANCE.OSSCLib_DocTCursor_FindNextTerm(
					docCursorPtr, bError, error.getPointer());
			et.end();
			if (term == null)
				break;
			if (bError.getValue() != 0)
				break;
			addTerm(list, term);
		}
		return list;
	}

	final public Pointer getPointer() {
		return docCursorPtr;
	}

	final public void release() {
		if (docCursorPtr == null)
			return;
		ExecutionToken et = FunctionTimer.INSTANCE
				.newExecutionToken("OSSCLib_DocTCursor_Delete");
		OsseLibrary.INSTANCE.OSSCLib_DocTCursor_Delete(docCursorPtr);
		et.end();
		docCursorPtr = null;
	}

}
