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

	Pointer OSSCLib_Index_Create(WString wszIndexDirectoryName,
			WString wszRootFileName, Pointer hExtErrInfo);

	Pointer OSSCLib_Index_Open(WString wszIndexDirectoryName,
			WString wszRootFileName, Pointer hExtErrInfo);

	boolean OSSCLib_Index_Close(Pointer hIndex, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_Begin(Pointer hIndex, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_Document_New(Pointer hTransact, Pointer hExtErrInfo);

	int OSSCLib_Transact_Document_AddStringTerms(Pointer hTransact,
			Pointer hDoc, Pointer hField, WString[] termArray,
			OsseTermOffset[] termOffsetArray, int[] termPosIncrArray,
			boolean[] successArray, int numberOfTerms, Pointer hExtErrInfo);

	boolean OSSCLib_Transact_RollBack(Pointer hTransact, Pointer hExtErrInfo);

	boolean OSSCLib_Transact_Commit(Pointer hTransact, Pointer lphDoc,
			long ui64NumberOfDocs, Pointer lpui64DocId, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_CreateField(Pointer hTransact,
			WString wszFieldName, int ui32FieldType, int ui32FieldFlags,
			Pointer lpFieldParams, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_GetField(Pointer hTransact, WString wszFieldName,
			Pointer hExtErrInfo);

	int OSSCLib_Transact_DeleteFields(Pointer hTransact,
			WString[] lplpwszFieldName, int ui32NumberOfFields,
			Pointer hExtErrInfo);

	int OSSCLib_Transact_ReserveExtraSpaceForDocHandles(Pointer hTransact,
			int ui32NumberOfNewDocs, int ui32NumberOfExistingDocs,
			Pointer hExtErrInfo);

	int OSSCLib_Index_GetListOfFields(Pointer hIndex, Pointer[] hFieldArray,
			int fieldArraySize, Pointer hExtErrInfo);

	Pointer OSSCLib_Index_GetFieldNameAndProperties(Pointer hIndex,
			Pointer hIndexField, IntByReference fieldId,
			IntByReference fieldType, IntByReference fieldFlags,
			Pointer hExtErrInfo);

	void OSSCLib_Index_GetFieldNameAndProperties_Free(Pointer hFieldName);

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