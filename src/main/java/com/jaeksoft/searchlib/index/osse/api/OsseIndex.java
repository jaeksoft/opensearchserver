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
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class OsseIndex {

	final public static OsseJNILibrary LIB;

	static {
		System.out.println(System.getProperty("java.library.path"));
		LIB = new OsseJNILibrary();
		System.out.println(LIB.OSSCLib_GetVersionInfoText());
	}

	private long indexPtr;

	public OsseIndex(File indexDirectory, OsseErrorHandler err, boolean bCreate)
			throws SearchLibException {
		String path = indexDirectory.getPath();
		if (bCreate) {
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsIndex_Create", path);
			indexPtr = LIB.OSSCLib_MsIndex_Create(path, null, err.getPointer());
			et.end("returns " + indexPtr);
		} else {
			ExecutionToken et = FunctionTimer.newExecutionToken(
					"OSSCLib_MsIndex_Open", indexDirectory.getPath());
			indexPtr = LIB.OSSCLib_MsIndex_Open(path, null, err.getPointer());
			et.end("returns " + indexPtr);
		}
		if (indexPtr == 0)
			throw new SearchLibException(err.getError());
	}

	public FieldInfo getFieldNameAndProperties(OsseErrorHandler error,
			int ui32MsFieldId) throws SearchLibException {
		IntByReference fieldType = new IntByReference();
		IntByReference fieldFlags = new IntByReference();
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_GetFieldNameAndProperties",
				Integer.toString(ui32MsFieldId));
		String hFieldName = LIB
				.OSSCLib_MsIndex_GetFieldNameAndProperties(indexPtr,
						ui32MsFieldId,
						Memory.nativeValue(fieldType.getPointer()),
						Memory.nativeValue(fieldFlags.getPointer()),
						error.getPointer());
		et.end();
		if (StringUtils.isEmpty(hFieldName))
			error.throwError();
		return new FieldInfo(hFieldName, ui32MsFieldId, fieldType.getValue(),
				fieldType.getValue());
	}

	public Map<String, FieldInfo> getListOfFields(OsseErrorHandler error)
			throws SearchLibException {
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_GetListOfFields", Integer.toString(0));
		int nField = LIB.OSSCLib_MsIndex_GetListOfFields(indexPtr, null, 0,
				false, error.getPointer());
		et.end("returns ", Integer.toString(nField));
		error.checkNoError();
		if (nField == 0)
			return null;
		Map<String, FieldInfo> fieldMap = new TreeMap<String, FieldInfo>();
		int[] hFieldArray = new int[nField];
		et = FunctionTimer.newExecutionToken("OSSCLib_MsIndex_GetListOfFields",
				Integer.toString(nField));
		LIB.OSSCLib_MsIndex_GetListOfFields(indexPtr, hFieldArray, nField,
				false, error.getPointer());
		et.end();
		error.checkNoError();
		for (int fieldId : hFieldArray) {
			FieldInfo info = getFieldNameAndProperties(error, fieldId);
			fieldMap.put(info.name, info);
		}
		return fieldMap;
	}

	public void close(OsseErrorHandler err) {
		if (indexPtr == 0)
			return;
		ExecutionToken et = FunctionTimer.newExecutionToken(
				"OSSCLib_MsIndex_Close", " ", Integer.toString(0));
		if (!LIB.OSSCLib_MsIndex_Close(indexPtr, err.getPointer()))
			Logging.warn(err.getError());
		et.end();
	}

	@Override
	public String toString() {
		return Long.toString(indexPtr);
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

	public long getPointer() {
		return indexPtr;
	}
}
