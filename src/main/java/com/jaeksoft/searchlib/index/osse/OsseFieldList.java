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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseFieldList {

	public class FieldInfo {

		public final String name;
		public final int id;
		public final int type;
		public final int flags;
		public final Pointer pointer;

		private FieldInfo(OsseIndex index, OsseErrorHandler error,
				Pointer fieldPtr) throws SearchLibException {
			IntByReference fieldId = new IntByReference();
			IntByReference fieldType = new IntByReference();
			IntByReference fieldFlags = new IntByReference();
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Index_GetFieldNameAndProperties");
			Pointer hFieldName = OsseLibrary.INSTANCE
					.OSSCLib_Index_GetFieldNameAndProperties(
							index.getPointer(), fieldPtr, fieldId, fieldType,
							fieldFlags, index.getPointer());
			et.end();
			if (hFieldName == null)
				throw new SearchLibException(error.getError());
			name = hFieldName.getString(0, true);
			id = fieldId.getValue();
			type = fieldType.getValue();
			flags = fieldType.getValue();
			pointer = fieldPtr;
			et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_Index_GetFieldNameAndProperties_Free");
			OsseLibrary.INSTANCE
					.OSSCLib_Index_GetFieldNameAndProperties_Free(hFieldName);
			et.end();
		}
	}

	private Map<String, FieldInfo> fieldPointerMap;

	public OsseFieldList(OsseIndex index, OsseErrorHandler error)
			throws SearchLibException {
		fieldPointerMap = new TreeMap<String, FieldInfo>();
		ExecutionToken et = FunctionTimer.INSTANCE
				.newExecutionToken("OSSCLib_Index_GetListOfFields");
		int nField = OsseLibrary.INSTANCE.OSSCLib_Index_GetListOfFields(
				index.getPointer(), null, 0, error.getPointer());
		et.end();
		if (nField == 0)
			return;
		Pointer[] hFieldArray = new Pointer[nField];
		et = FunctionTimer.INSTANCE
				.newExecutionToken("OSSCLib_Index_GetListOfFields");
		OsseLibrary.INSTANCE.OSSCLib_Index_GetListOfFields(index.getPointer(),
				hFieldArray, nField, error.getPointer());
		et.end();
		for (Pointer fieldPtr : hFieldArray) {
			FieldInfo info = new FieldInfo(index, error, fieldPtr);
			fieldPointerMap.put(info.name, info);
		}
	}

	public FieldInfo getFieldInfo(String fieldName) {
		return fieldPointerMap.get(fieldName);
	}

	public Collection<FieldInfo> collection() {
		return fieldPointerMap.values();
	}

	public int getSize() {
		return fieldPointerMap.size();
	}

}
