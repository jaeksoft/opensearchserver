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

import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class OsseErrorHandler {

	private Pointer errPtr;

	private OsseErrorHandler errRef;

	public OsseErrorHandler(OsseErrorHandler err) {
		if (err == null)
			errPtr = OsseLibrary.INSTANCE.OSSCLib_ExtErrInfo_Create();
		else {
			errRef = err;
			errPtr = err.errPtr;
		}
	}

	public OsseErrorHandler() {
		this(null);
	}

	final public String getError() {
		WString error = OsseLibrary.INSTANCE.OSSCLib_ExtErrInfo_GetText(errPtr);
		return error != null ? error.toString() : null;
	}

	final public Pointer getPointer() {
		return errPtr;
	}

	final public void release() {
		if (errRef != null)
			if (errPtr != null)
				OsseLibrary.INSTANCE.OSSCLib_ExtErrInfo_Delete(errPtr);
		errPtr = null;
	}

}
