/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util.bdb;

import java.util.ArrayList;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

public class BdbJoin {

	private ArrayList<SecondaryCursor> cursorList = null;
	private JoinCursor joinCursor = null;

	public BdbJoin() {
		cursorList = new ArrayList<SecondaryCursor>();
	}

	public void close() throws DatabaseException {
		for (SecondaryCursor cursor : cursorList)
			if (cursor != null)
				cursor.close();
		cursorList.clear();
		if (joinCursor != null) {
			joinCursor.close();
			joinCursor = null;
		}
	}

	public boolean add(Transaction txn, CursorConfig config, DatabaseEntry key,
			SecondaryDatabase secDb) throws DatabaseException {
		DatabaseEntry data = new DatabaseEntry();
		SecondaryCursor cursor = secDb.openSecondaryCursor(txn, config);
		cursorList.add(cursor);
		if (cursor.getSearchKey(key, data, null) != OperationStatus.SUCCESS)
			return false;
		return true;
	}

	public JoinCursor getJoinCursor(Database db) throws DatabaseException {
		if (joinCursor != null)
			return joinCursor;
		joinCursor = db.join(cursorList.toArray(new SecondaryCursor[cursorList
				.size()]), null);
		return joinCursor;
	}

}
