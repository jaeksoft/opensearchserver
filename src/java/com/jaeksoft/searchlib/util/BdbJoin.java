package com.jaeksoft.searchlib.util;

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
