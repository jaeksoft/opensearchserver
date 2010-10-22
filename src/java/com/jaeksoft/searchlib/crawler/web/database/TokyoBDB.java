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

import com.jaeksoft.searchlib.SearchLibException;

public class TokyoBDB {

	private BDB bdb;
	private boolean isOpen;
	private String filePath;

	public TokyoBDB(String filePath) {
		bdb = new BDB();
		isOpen = false;
		this.filePath = filePath;
	}

	public void openRead() {

	}

	public void openWrite() {

	}

	public void close() throws SearchLibException {
		if (!isOpen)
			return;
		if (bdb.close())
			return;
		throw new SearchLibException(getLastError("Close error"));
	}

	protected String getLastError(String prefix) {
		return prefix + bdb.errmsg();
	}
}
