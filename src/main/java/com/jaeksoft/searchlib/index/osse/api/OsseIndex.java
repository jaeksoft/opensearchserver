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

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray.PointerProvider;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class OsseIndex implements PointerProvider {

	private Pointer indexPtr;

	public OsseIndex(File indexDirectory, OsseErrorHandler err, boolean bCreate)
			throws SearchLibException {
		WString path = new WString(indexDirectory.getPath());
		if (bCreate) {
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsIndex_Create", indexDirectory.getPath());
			indexPtr = OsseLibrary.OSSCLib_MsIndex_Create(path, null,
					err.getPointer());
			et.end("returns " + indexPtr.toString());
		} else {
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsIndex_Open", indexDirectory.getPath());
			indexPtr = OsseLibrary.OSSCLib_MsIndex_Open(path, null,
					err.getPointer());
			et.end("returns " + indexPtr.toString());
		}
		if (indexPtr == null)
			throw new SearchLibException(err.getError());
	}

	public FieldInfo getFieldNameAndProperties(OsseErrorHandler error,
			int ui32MsFieldId) throws SearchLibException {
		IntByReference fieldType = new IntByReference();
		IntByReference fieldFlags = new IntByReference();
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_GetFieldNameAndProperties",
				Integer.toString(ui32MsFieldId));
		Pointer hFieldName = OsseLibrary
				.OSSCLib_MsIndex_GetFieldNameAndProperties(indexPtr,
						ui32MsFieldId, fieldType, fieldFlags,
						error.getPointer());
		et.end();
		if (hFieldName == null)
			error.throwError();
		return new FieldInfo(hFieldName.getString(0), ui32MsFieldId,
				fieldType.getValue(), fieldType.getValue());
	}

	public Map<String, FieldInfo> getListOfFields(OsseErrorHandler error)
			throws SearchLibException {
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_GetListOfFields", Integer.toString(0));
		int nField = OsseLibrary.OSSCLib_MsIndex_GetListOfFields(indexPtr,
				null, 0, false, error.getPointer());
		et.end("returns ", Integer.toString(nField));
		error.checkNoError();
		if (nField == 0)
			return null;
		Map<String, FieldInfo> fieldMap = new TreeMap<String, FieldInfo>();
		int[] hFieldArray = new int[nField];
		et = FunctionTimer.newExecutionToken("OSSCLib_MsIndex_GetListOfFields",
				Integer.toString(nField));
		OsseLibrary.OSSCLib_MsIndex_GetListOfFields(indexPtr, hFieldArray,
				nField, false, error.getPointer());
		et.end();
		error.checkNoError();
		for (int fieldId : hFieldArray) {
			FieldInfo info = getFieldNameAndProperties(error, fieldId);
			fieldMap.put(info.name, info);
		}
		return fieldMap;
	}

	@Override
	public Pointer getPointer() {
		return indexPtr;
	}

	public void close(OsseErrorHandler err) {
		if (indexPtr == null)
			return;
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_Close", " ", Integer.toString(0));
		if (!OsseLibrary.OSSCLib_MsIndex_Close(indexPtr, err.getPointer()))
			Logging.warn(err.getError());
		et.end();
	}

	@Override
	public String toString() {
		return indexPtr == null ? "null" : indexPtr.toString();
	}

	public class FieldInfo {

		public final String name;
		public final int id;
		public final int type;
		public final int flags;

		private FieldInfo(String name, int id, int type, int flags)
				throws SearchLibException {
			this.name = name;
			this.id = id;
			this.type = type;
			this.flags = flags;
		}

		@Override
		public String toString() {
			return StringUtils.fastConcat("name: ", name, " - id: ",
					Integer.toString(id), " - type: ", Integer.toString(type),
					" - flags: ", Integer.toString(flags));
		}
	}
}
