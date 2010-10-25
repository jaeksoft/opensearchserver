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

import tokyocabinet.BDB;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.database.tokyo.TokyoADB;

public class TokyoBDB extends TokyoADB {

	public BDB db;

	protected TokyoBDB() {
		db = new BDB();
	}

	@Override
	protected void dbOpenRead() throws SearchLibException {
		if (!db.open(getAbsolutePath(), BDB.OREADER))
			throwError("Database open error " + getAbsolutePath());
	}

	@Override
	protected void dbOpenWrite() throws SearchLibException {
		if (!db.open(getAbsolutePath(), BDB.OWRITER | BDB.OCREAT))
			throwError("Database open error " + getAbsolutePath());
	}

	@Override
	protected void dbClose() throws SearchLibException {
		if (!db.close())
			throwError("Database close error " + getAbsolutePath());
	}

	@Override
	protected void throwError(String prefix) throws SearchLibException {
		throw new SearchLibException(prefix + " " + db.errmsg());
	}
}
