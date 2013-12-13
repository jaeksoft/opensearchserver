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

package com.jaeksoft.searchlib.index.osse;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public interface OsseLibrary extends Library {

	final public static int OSSCLIB_FIELD_UI32FIELDTYPE_STRING = 1;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET = 0x00000002;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION = 0x00000004;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1 = 0x00000040;

	final public static int OSSCLIB_QCURSOR_UI32BOP_OR = 0x00000000;
	final public static int OSSCLIB_QCURSOR_UI32BOP_AND = 0x00000001;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_OR = 0x00000002;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_AND = 0x00000003;

	public OsseLibrary INSTANCE = (OsseLibrary) Native.loadLibrary(
			"OpenSearchServer_CLib", OsseLibrary.class);

	WString OSSCLib_GetVersionInfoText();

	Pointer OSSCLib_ExtErrInfo_Create();

	int OSSCLib_ExtErrInfo_GetErrorCode(Pointer hExtErrInfo);

	WString OSSCLib_ExtErrInfo_GetText(Pointer lpErr);

	void OSSCLib_ExtErrInfo_Delete(Pointer hExtErrInfo);

	Pointer OSSCLib_MsIndex_Create(WString lpwszIndexDirectoryName,
			WString lpwszFileName_MsRoot, Pointer hExtErrInfo);

	Pointer OSSCLib_MsIndex_Open(WString lpwszIndexDirectoryName,
			WString lpwszFileName_MsRoot, Pointer hExtErrInfo);

	boolean OSSCLib_MsIndex_Close(Pointer hMsIndex, Pointer hExtErrInfo);

	Pointer OSSCLib_MsTransact_Begin(Pointer hMsIndex,
			WString lpwszNewSegmentDirectoryName, Pointer hExtErrInfo);

	int OSSCLib_MsTransact_Document_GetNewDocId(Pointer hMsTransact,
			Pointer hExtErrInfo);

	int OSSCLib_MsTransact_Document_AddStringTermsW(Pointer hMsTransactField,
			int ui32DocId, WString[] lplpwszTermArray, int ui32NumberOfTerms,
			Pointer hExtErrInfo);

	boolean OSSCLib_MsTransact_RollBack(Pointer hMsTransact, Pointer hExtErrInfo);

	boolean OSSCLib_MsTransact_Commit(Pointer hMsTransact,
			int ui32IndexSignature, LongByReference lpui64NewDocIdBase,
			IntByReference lpbSomeDocIdsChanged, Pointer hExtErrInfo);

	Pointer OSSCLib_MsTransact_CreateFieldW(Pointer hMsTransact,
			WString lpwszFieldName, int ui32FieldType, int ui32FieldFlags,
			Pointer lpFieldParams, boolean bFailIfAlreadyExists,
			IntByReference lpui32MsFieldId, Pointer hExtErrInfo);

	Pointer OSSCLib_MsTransact_FindFieldW(Pointer hMsTransact,
			WString lpwszFieldName, Pointer hExtErrInfo);

	Pointer OSSCLib_MsTransact_GetExistingField(Pointer hMsTransact,
			int ui32MsFieldId, Pointer hExtErrInfo);

	int OSSCLib_MsTransact_DeleteFields(Pointer hMsTransact,
			Pointer[] lphMsTransactFieldArray, int ui32NumberOfFields,
			Pointer hExtErrInfo);

	int OSSCLib_MsIndex_GetListOfFields(Pointer hMsIndex,
			int[] lpui32FieldIdArray, int ui32FieldIdArraySize,
			boolean bSortArrayByFieldId, Pointer hExtErrInfo);

	Pointer OSSCLib_MsIndex_GetFieldNameAndProperties(Pointer hMsIndex,
			int ui32MsFieldId, IntByReference lpui32FieldType,
			IntByReference lpui32FieldFlags, Pointer hExtErrInfo);

	Pointer OSSCLib_QCursor_Create(Pointer hIndex, WString lpwszFieldName,
			WString[] lplpwszTerm, int ui32NumberOfTerms, int ui32Bop,
			Pointer hExtErrInfo);

	void OSSCLib_QCursor_Delete(Pointer hCursor);

	Pointer OSSCLib_QCursor_CreateCombinedCursor(Pointer[] lphCursor,
			int ui32NumberOfCursors, int ui32Bop, Pointer hExtErrInfo);

	long OSSCLib_QCursor_GetDocumentIds(
			Pointer hCursor, // Cursor handle
			long[] lpui64DocId, long ui64NumberOfDocsToRetrieve,
			long ui64DocPosition, boolean bPosMeasuredFromEnd,
			IntByReference lpbSuccess, Pointer hExtErrInfo);

	long OSSCLib_QCursor_GetNumberOfDocuments(Pointer hCursor,
			Pointer lpbSuccess, Pointer hExtErrInfo);

	Pointer OSSCLib_DocTCursor_Create(Pointer hIndex, Pointer hExtErrInfo);

	void OSSCLib_DocTCursor_Delete(Pointer hDocTCursor);

	WString OSSCLib_DocTCursor_FindFirstTerm(Pointer hDocTCursor,
			Pointer hIndexField, long ui64DocId, Pointer hExtErrInfo);

	WString OSSCLib_DocTCursor_FindNextTerm(Pointer hDocTCursor,
			IntByReference lpbError, Pointer hExtErrInfo);
}