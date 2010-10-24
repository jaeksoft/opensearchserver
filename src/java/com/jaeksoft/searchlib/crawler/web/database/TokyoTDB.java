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

package com.jaeksoft.searchlib.crawler.web.database;

import tokyocabinet.BDB;
import tokyocabinet.TDB;

import com.jaeksoft.searchlib.SearchLibException;

public class TokyoTDB extends TokyoADB {

	public TDB db;

	protected TokyoTDB() {
		db = new TDB();
	}

	protected void openRead() throws SearchLibException {
		if (!db.open(getAbsolutePath(), BDB.OREADER))
			throwError("Database open error " + getAbsolutePath());
		isOpen = true;
	}

	protected void openWrite() throws SearchLibException {
		if (!db.open(getAbsolutePath(), BDB.OWRITER | BDB.OCREAT))
			throwError("Database open error " + getAbsolutePath());
		isOpen = true;
	}

	protected void close() throws SearchLibException {
		if (!isOpen)
			return;
		if (!db.close())
			throwError("Database close error " + getAbsolutePath());
		isOpen = false;
	}

	@Override
	protected void throwError(String prefix) throws SearchLibException {
		throw new SearchLibException(prefix + " " + db.errmsg());
	}
}
