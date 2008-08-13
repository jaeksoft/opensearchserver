package com.jaeksoft.searchlib.util.bdb;

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.util.Timer;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class JointCursor extends AbstractCursor {

	final private static Logger logger = Logger.getLogger(JointCursor.class);

	private JoinCursor cursor;

	public JointCursor(JoinCursor cursor) {
		super();
		this.cursor = cursor;
	}

	public long countLeft(LockMode lockMode) throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("CountLeft JoinCursor");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		long count = 0;
		while (!abort
				&& cursor.getNext(key, lockMode) == OperationStatus.SUCCESS)
			count++;

		if (timer != null)
			logger.info(timer + " (" + count + ")");

		return count;
	}

	public long forward(long offset, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("Forward JoinCursor (" + offset + ")");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		long forward = 0;
		while (!abort && offset-- > 0) {
			if (cursor.getNext(key, lockMode) != OperationStatus.SUCCESS)
				return forward;
			forward++;
		}

		if (timer != null)
			logger.info(timer + " (" + forward + ")");

		return forward;
	}

	public long getRows(long rows, LockMode lockMode, BdbEntry entry)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("GetRows JoinCursor (" + rows + ")");

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		int size = 0;
		while (!abort && rows-- > 0) {
			if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
				return size;
			entry.entry(data);
			size++;
		}

		if (timer != null)
			logger.info(timer + " (" + size + ")");

		return size;
	}

}
