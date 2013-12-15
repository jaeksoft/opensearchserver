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

import java.io.Closeable;

import com.jaeksoft.searchlib.SearchLibException;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class OsseErrorHandler implements Closeable {

	private Pointer errPtr;

	public OsseErrorHandler() throws SearchLibException {
		errPtr = OsseLibrary.OSSCLib_ExtErrInfo_Create();
		if (errPtr == null)
			throw new SearchLibException(
					"Internal error: OSSCLib_ExtErrInfo_Create");
	}

	final public String getError() {
		WString error = OsseLibrary.OSSCLib_ExtErrInfo_GetText(errPtr);
		return error != null ? error.toString() : null;
	}

	final public int getErrorCode() {
		return OsseLibrary.OSSCLib_ExtErrInfo_GetErrorCode(errPtr);
	}

	final public void checkNoError() throws SearchLibException {
		if (getErrorCode() != 0)
			throw new SearchLibException(getError());
	}

	final public void throwError() throws SearchLibException {
		throw new SearchLibException(getError());
	}

	final public Pointer getPointer() {
		return errPtr;
	}

	@Override
	final public void close() {
		if (errPtr == null)
			return;
		OsseLibrary.OSSCLib_ExtErrInfo_Delete(errPtr);
		errPtr = null;
	}

}
