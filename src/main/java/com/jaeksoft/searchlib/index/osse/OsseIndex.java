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

package com.jaeksoft.searchlib.index.osse;

import java.io.File;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.FunctionTimer;
import com.jaeksoft.searchlib.util.FunctionTimer.ExecutionToken;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class OsseIndex {

	private Pointer indexPtr;

	public OsseIndex(File indexDirectory, OsseErrorHandler err, boolean bCreate)
			throws SearchLibException {
		err = new OsseErrorHandler(err);
		try {
			WString path = new WString(indexDirectory.getPath());
			if (bCreate) {
				ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
						"OSSCLib_MsIndex_Create ", indexDirectory.getPath());
				indexPtr = OsseLibrary.INSTANCE.OSSCLib_MsIndex_Create(path,
						null, err.getPointer());
				et.end();
			} else {
				ExecutionToken et = FunctionTimer.INSTANCE.newExecutionToken(
						"OSSCLib_MsIndex_Open ", indexDirectory.getPath());
				indexPtr = OsseLibrary.INSTANCE.OSSCLib_MsIndex_Open(path,
						null, err.getPointer());
				et.end();
			}
			if (indexPtr == null)
				throw new SearchLibException(err.getError());
		} finally {
			err.release();
		}
	}

	public Pointer getPointer() {
		return indexPtr;
	}

	public void close(OsseErrorHandler err) {
		if (indexPtr == null)
			return;
		err = new OsseErrorHandler(err);
		try {
			ExecutionToken et = FunctionTimer.INSTANCE
					.newExecutionToken("OSSCLib_MsIndex_Close");
			if (!OsseLibrary.INSTANCE.OSSCLib_MsIndex_Close(indexPtr,
					err.getPointer()))
				Logging.warn(err.getError());
			et.end();
		} finally {
			err.release();
		}
	}

}
