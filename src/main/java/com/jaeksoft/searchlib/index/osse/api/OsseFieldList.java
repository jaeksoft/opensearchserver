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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseLibrary;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseFieldList {

	public class FieldInfo {

		public final String name;
		public final int id;
		public final int type;
		public final int flags;

		private FieldInfo(OsseIndex index, OsseErrorHandler error,
				int ui32MsFieldId) throws SearchLibException {
			IntByReference fieldType = new IntByReference();
			IntByReference fieldFlags = new IntByReference();
			Pointer hFieldName = OsseLibrary
					.OSSCLib_MsIndex_GetFieldNameAndProperties(
							index.getPointer(), ui32MsFieldId, fieldType,
							fieldFlags, index.getPointer());
			if (hFieldName == null)
				throw new SearchLibException(error.getError());
			name = hFieldName.getString(0);
			id = ui32MsFieldId;
			type = fieldType.getValue();
			flags = fieldType.getValue();
		}

		@Override
		public String toString() {
			return StringUtils.fastConcat("name: ", name, " - id: ",
					Integer.toString(id), " - type: ", Integer.toString(type),
					" - flags: ", Integer.toString(flags));
		}
	}

	private Map<String, FieldInfo> fieldPointerMap;

	public OsseFieldList(OsseIndex index, OsseErrorHandler error)
			throws SearchLibException {
		fieldPointerMap = new TreeMap<String, FieldInfo>();
		int nField = OsseLibrary.OSSCLib_MsIndex_GetListOfFields(
				index.getPointer(), null, 0, false, error.getPointer());
		if (nField == 0)
			return;
		int[] hFieldArray = new int[nField];
		OsseLibrary.OSSCLib_MsIndex_GetListOfFields(index.getPointer(),
				hFieldArray, nField, false, error.getPointer());
		error.checkNoError();
		for (int fieldId : hFieldArray) {
			FieldInfo info = new FieldInfo(index, error, fieldId);
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
