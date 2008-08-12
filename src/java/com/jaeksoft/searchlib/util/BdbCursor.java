package com.jaeksoft.searchlib.util;

import org.apache.log4j.Logger;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BdbCursor {

	final private static Logger logger = Logger.getLogger(BdbCursor.class);

	public static long countLeft(Cursor cursor, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("CountLeft Cursor");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);
		long count = 0;
		while (cursor.getNextNoDup(key, data, lockMode) == OperationStatus.SUCCESS)
			count += cursor.count();

		if (timer != null)
			logger.info(timer + " (" + count + ")");

		return count;
	}

	public static long countLeft(JoinCursor cursor, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("CountLeft JoinCursor");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		long count = 0;
		while (cursor.getNext(key, lockMode) == OperationStatus.SUCCESS)
			count++;

		if (timer != null)
			logger.info(timer + " (" + count + ")");

		return count;
	}

	public static long forward(Cursor cursor, long offset, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("Forward Cursor (" + offset + ")");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);
		long forward = 0;
		while (offset-- > 0) {
			if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
				if (cursor.getNextDup(key, data, lockMode) != OperationStatus.SUCCESS)
					return forward;
			forward++;
		}

		if (timer != null)
			logger.info(timer + " (" + forward + ")");

		return forward;
	}

	public static long forward(JoinCursor cursor, long offset, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("Forward JoinCursor (" + offset + ")");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		long forward = 0;
		while (offset-- > 0) {
			if (cursor.getNext(key, lockMode) != OperationStatus.SUCCESS)
				return forward;
			forward++;
		}

		if (timer != null)
			logger.info(timer + " (" + forward + ")");

		return forward;
	}

	public interface BdbEntry {
		void entry(DatabaseEntry data);
	}

	public static long getRows(Cursor cursor, long rows, LockMode lockMode,
			BdbEntry entry) throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("GetRows Cursor (" + rows + ")");

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		int size = 0;
		while (rows-- > 0) {
			if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
				if (cursor.getNextDup(key, data, lockMode) != OperationStatus.SUCCESS)
					return size;
			entry.entry(data);
			size++;
		}

		if (timer != null)
			logger.info(timer + " (" + size + ")");

		return size;
	}

	public static long getRows(JoinCursor cursor, long rows, LockMode lockMode,
			BdbEntry entry) throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("GetRows JoinCursor (" + rows + ")");

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		int size = 0;
		while (rows-- > 0) {
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
