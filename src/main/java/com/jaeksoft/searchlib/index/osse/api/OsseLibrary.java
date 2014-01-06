/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.index.osse.memory.OsseFastStringArray;
import com.jaeksoft.searchlib.index.osse.memory.OssePointerArray;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public class OsseLibrary {

	final public static int OSSCLIB_FIELD_UI32FIELDTYPE_STRING = 1;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET = 0x00000002;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION = 0x00000004;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1 = 0x00000040;

	final public static int OSSCLIB_QCURSOR_UI32BOP_OR = 0x00000000;
	final public static int OSSCLIB_QCURSOR_UI32BOP_AND = 0x00000001;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_OR = 0x00000002;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_AND = 0x00000003;

	public static native WString OSSCLib_GetVersionInfoText();

	public static native Pointer OSSCLib_ExtErrInfo_Create();

	public static native int OSSCLib_ExtErrInfo_GetErrorCode(Pointer hExtErrInfo);

	public static native WString OSSCLib_ExtErrInfo_GetText(Pointer lpErr);

	public static native void OSSCLib_ExtErrInfo_Delete(Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsIndex_Create(
			WString lpwszIndexDirectoryName, WString lpwszFileName_MsRoot,
			Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsIndex_Open(
			WString lpwszIndexDirectoryName, WString lpwszFileName_MsRoot,
			Pointer hExtErrInfo);

	public static native boolean OSSCLib_MsIndex_Close(Pointer hMsIndex,
			Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsTransact_Begin(Pointer hMsIndex,
			WString lpwszNewSegmentDirectoryName, int ui32MaxNumberOfNewDocs,
			Pointer hExtErrInfo);

	public static native int OSSCLib_MsTransact_Document_GetNewDocId(
			Pointer hMsTransact, Pointer hExtErrInfo);

	public static native int OSSCLib_MsTransact_Document_AddStringTerms(
			Pointer hMsTransactField, int ui32DocId,
			OsseFastStringArray lplpsu8zTermArray, int ui32NumberOfTerms,
			Pointer hExtErrInfo);

	public static native boolean OSSCLib_MsTransact_RollBack(
			Pointer hMsTransact, Pointer hExtErrInfo);

	public static native boolean OSSCLib_MsTransact_Commit(Pointer hMsTransact,
			int ui32IndexSignature, LongByReference lpui64NewDocIdBase,
			IntByReference lpbSomeDocIdsChanged, Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsTransact_CreateFieldW(
			Pointer hMsTransact, WString lpwszFieldName, int ui32FieldType,
			int ui32FieldFlags, Pointer lpFieldParams,
			boolean bFailIfAlreadyExists, IntByReference lpui32MsFieldId,
			Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsTransact_FindFieldW(
			Pointer hMsTransact, WString lpwszFieldName, Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsTransact_GetExistingField(
			Pointer hMsTransact, int ui32MsFieldId, Pointer hExtErrInfo);

	public static native int OSSCLib_MsTransact_DeleteFields(
			Pointer hMsTransact, OssePointerArray lphMsTransactFieldArray,
			int ui32NumberOfFields, Pointer hExtErrInfo);

	public static native int OSSCLib_MsIndex_GetListOfFields(Pointer hMsIndex,
			int[] lpui32FieldIdArray, int ui32FieldIdArraySize,
			boolean bSortArrayByFieldId, Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsIndex_GetFieldNameAndProperties(
			Pointer hMsIndex, int ui32MsFieldId,
			IntByReference lpui32FieldType, IntByReference lpui32FieldFlags,
			Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsQCursor_Create(Pointer hMsIndex,
			int ui32MsFieldId, OsseFastStringArray lplpsu8zTerm,
			int ui32NumberOfTerms, int ui32Bop, Pointer hExtErrInfo);

	public static native void OSSCLib_MsQCursor_Delete(Pointer hMsQCursor);

	public static native long OSSCLib_MsQCursor_GetNumberOfDocs(
			Pointer hMsQCursor, IntByReference lpbSuccess, Pointer hExtErrInfo);

	public static native int OSSCLib_MsQCursor_GetDocIds(
			Pointer hMsQCursor, // Cursor handle
			long[] lpui64DocId, long ui64DocPosition,
			boolean bPosMeasuredFromEnd, long ui32NumberOfDocsToRetrieve,
			IntByReference lpbSuccess, Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsQCursor_CreateCombinedCursor(
			Pointer hMsIndex, // Index handle
			OssePointerArray lphMsQCursor, int ui32NumberOfCursors,
			int ui32Bop, Pointer hExtErrInfo);

	public static native Pointer OSSCLib_MsDocTCursor_Create(Pointer hMsIndex,
			Pointer hExtErrInfo);

	public static native void OSSCLib_MsDocTCursor_Delete(Pointer hMsDocTCursor);

	public static native int OSSCLib_MsDocTCursor_FindFirstTerm(
			Pointer hMsDocTCursor, int ui32MsFieldId, long ui64DocId,
			Pointer lplpsu8zTerm, IntByReference lpbError, Pointer hExtErrInfo);

	public static native int OSSCLib_MsDocTCursor_FindNextTerm(
			Pointer hMsDocTCursor, Pointer lplpsu8zTerm,
			IntByReference lpbError, Pointer hExtErrInfo);

	public static native String OSSCLib_MsDocTCursor_GetCurrentTerm(
			Pointer hMsDocTCursor, Pointer hExtErrInfo);

	static {
		Native.register("OpenSearchServer_CLib");
	}

}