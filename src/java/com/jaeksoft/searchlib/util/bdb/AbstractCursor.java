package com.jaeksoft.searchlib.util.bdb;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;

public abstract class AbstractCursor {

	protected boolean abort;

	protected AbstractCursor() {
		abort = false;
	}

	public void abort() {
		abort = true;
	}

	public abstract long countLeft(LockMode lockMode) throws DatabaseException;

	public abstract long forward(long offset, LockMode lockMode)
			throws DatabaseException;

	public interface BdbEntry {
		void entry(DatabaseEntry data);
	}

	public abstract long getRows(long rows, LockMode lockMode, BdbEntry entry)
			throws DatabaseException;

}
