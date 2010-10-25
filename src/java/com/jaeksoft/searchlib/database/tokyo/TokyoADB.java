/**   

 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.database.tokyo;

import java.io.File;

import com.jaeksoft.searchlib.SearchLibException;

public abstract class TokyoADB {

	public enum Mode {
		CLOSED, READ, WRITE;
	}

	private File file;
	private Mode mode;

	protected TokyoADB() {
		mode = Mode.CLOSED;
		file = null;
	}

	public void init(File file) {
		this.file = file;
	}

	protected String getAbsolutePath() {
		return file == null ? null : file.getAbsolutePath();
	}

	final public void openForRead() throws SearchLibException {
		if (mode == Mode.CLOSED) {
			dbOpenRead();
			mode = Mode.READ;
			return;
		}
		if (mode == Mode.WRITE)
			throwError("Already open with write access");

	}

	final public void openForWrite() throws SearchLibException {
		if (mode == Mode.CLOSED) {
			dbOpenWrite();
			mode = Mode.WRITE;
			return;
		}
		if (mode == Mode.READ)
			throwError("Already open with read access");
	}

	final public void close() throws SearchLibException {
		if (mode == Mode.CLOSED)
			return;
		dbClose();
		mode = Mode.CLOSED;
	}

	protected abstract void throwError(String prefix) throws SearchLibException;

	protected abstract void dbOpenRead() throws SearchLibException;

	protected abstract void dbOpenWrite() throws SearchLibException;

	protected abstract void dbClose() throws SearchLibException;

}