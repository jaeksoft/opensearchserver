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

public class OsseJNILibrary {

	static {
		System.loadLibrary("OpenSearchServer_CLib");
	}

	public native String OSSCLib_GetVersionInfoText();

	public native long OSSCLib_ExtErrInfo_Create();

	public native int OSSCLib_ExtErrInfo_GetErrorCode(long hExtErrInfo);

	public native String OSSCLib_ExtErrInfo_GetText(long lpErr);

	public native void OSSCLib_ExtErrInfo_Delete(long hExtErrInfo);

	public native long OSSCLib_MsIndex_Create(String lpwszIndexDirectoryName,
			String lpwszFileName_MsRoot, long hExtErrInfo);

	public native long OSSCLib_MsIndex_Open(String lpwszIndexDirectoryName,
			String lpwszFileName_MsRoot, long hExtErrInfo);

	public native boolean OSSCLib_MsIndex_Close(long hMsIndex, long hExtErrInfo);

	public native long OSSCLib_MsTransact_Begin(long hMsIndex,
			String lpwszNewSegmentDirectoryName, int ui32MaxNumberOfNewDocs,
			long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_GetNewDocId(long hMsTransact,
			long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_AddStringTerms(
			long hMsTransactField, int ui32DocId, long lplpsu8zTermArray,
			int ui32NumberOfTerms, long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_AddStringTermsW(
			long hMsTransactField, int ui32DocId, long lplpwszTermArray,
			int ui32NumberOfTerms, long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_RollBack(long hMsTransact,
			long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_Commit(long hMsTransact,
			int ui32IndexSignature, long lpui64NewDocIdBase,
			long lpbSomeDocIdsChanged, long hExtErrInfo);

	public native long OSSCLib_MsTransact_CreateFieldW(long hMsTransact,
			String lpwszFieldName, int ui32FieldType, int ui32FieldFlags,
			long lpFieldParams, boolean bFailIfAlreadyExists,
			long lpui32MsFieldId, long hExtErrInfo);

	public native long OSSCLib_MsTransact_FindFieldW(long hMsTransact,
			String lpwszFieldName, long hExtErrInfo);

	public native long OSSCLib_MsTransact_GetExistingField(long hMsTransact,
			int ui32MsFieldId, long hExtErrInfo);

	public native int OSSCLib_MsTransact_DeleteFields(long hMsTransact,
			long lphMsTransactFieldArray, int ui32NumberOfFields,
			long hExtErrInfo);

	public native int OSSCLib_MsIndex_GetListOfFields(long hMsIndex,
			int[] lpui32FieldIdArray, int ui32FieldIdArraySize,
			boolean bSortArrayByFieldId, long hExtErrInfo);

	public native long OSSCLib_MsIndex_GetFieldNameAndProperties(long hMsIndex,
			int ui32MsFieldId, long lpui32FieldType, long lpui32FieldFlags,
			long hExtErrInfo);

	public native long OSSCLib_MsQCursor_Create(long hMsIndex,
			int ui32MsFieldId, long lplpsu8zTerm, int ui32NumberOfTerms,
			int ui32Bop, long hExtErrInfo);

	public native void OSSCLib_MsQCursor_Delete(long hMsQCursor);

	public native long OSSCLib_MsQCursor_GetNumberOfDocs(long hMsQCursor,
			long lpbSuccess, long hExtErrInfo);

	public native int OSSCLib_MsQCursor_GetDocIds(
			long hMsQCursor, // Cursor handle
			long[] lpui64DocId, long ui64DocPosition,
			boolean bPosMeasuredFromEnd, long ui32NumberOfDocsToRetrieve,
			long lpbSuccess, long hExtErrInfo);

	public native long OSSCLib_MsQCursor_CreateCombinedCursor(
			long hMsIndex, // Index handle
			long lphMsQCursor, int ui32NumberOfCursors, int ui32Bop,
			long hExtErrInfo);

	public native long OSSCLib_MsDocTCursor_Create(long hMsIndex,
			long hExtErrInfo);

	public native void OSSCLib_MsDocTCursor_Delete(long hMsDocTCursor);

	public native int OSSCLib_MsDocTCursor_FindFirstTerm(long hMsDocTCursor,
			int ui32MsFieldId, long ui64DocId, long lplpsu8zTerm,
			long lpbError, long hExtErrInfo);

	public native int OSSCLib_MsDocTCursor_FindNextTerm(long hMsDocTCursor,
			long lplpsu8zTerm, long lpbError, long hExtErrInfo);

	public native String OSSCLib_MsDocTCursor_GetCurrentTerm(
			long hMsDocTCursor, long hExtErrInfo);

}