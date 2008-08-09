package com.jaeksoft.searchlib.util;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public abstract class BdbUtil<T> extends TupleBinding<T> {

	public abstract DatabaseEntry getKey(T object)
			throws UnsupportedEncodingException;

	public boolean startsWith(DatabaseEntry key, String pattern)
			throws UnsupportedEncodingException {
		return getKey(key).startsWith(pattern);
	}

	public String getKey(DatabaseEntry key) throws UnsupportedEncodingException {
		return new String(key.getData(), "UTF-8");
	}

	public void setKey(String pattern, DatabaseEntry key)
			throws UnsupportedEncodingException {
		key.setData(pattern.getBytes("UTF-8"));
	}

	public DatabaseEntry getData(T object) {
		DatabaseEntry data = new DatabaseEntry();
		objectToEntry(object, data);
		return data;
	}

	public int getStartsWith(Cursor cursor, String pattern, int start,
			int rows, List<T> list) throws DatabaseException,
			UnsupportedEncodingException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		setKey(pattern, key);
		if (cursor.getSearchKeyRange(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
			return 0;

		int size = 0;

		while (start-- > 0) {
			if (!startsWith(key, pattern))
				return size;
			size++;
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
		}

		while (rows-- > 0) {
			if (!startsWith(key, pattern))
				return size;
			size++;
			list.add(entryToObject(data));
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
		}

		while (startsWith(key, pattern)) {
			size++;
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
		}

		return size;
	}

	public int getLimit(Cursor cursor, int start, int rows, List<T> list)
			throws DatabaseException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		int size = 0;

		while (start-- > 0) {
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
			size++;
		}

		while (rows-- > 0) {
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
			list.add(entryToObject(data));
			size++;
		}

		while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			size++;
		return size;

	}

	public int getLimit(JoinCursor cursor, int start, int rows, List<T> list)
			throws DatabaseException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		int size = 0;

		while (start-- > 0) {
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
			size++;
		}

		while (rows-- > 0) {
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return size;
			list.add(entryToObject(data));
			size++;
		}

		while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			size++;
		return size;
	}

	public void getFilter(JoinCursor cursor, List<T> list, int limit,
			Comparator<T> comparator) throws DatabaseException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		while (limit > 0) {
			if (cursor.getNext(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
				return;
			T object = entryToObject(data);
			if (comparator.compare(object, null) == 0)
				break;
			list.add(object);
			limit--;
		}
	}
}
