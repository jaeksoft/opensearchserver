package com.jaeksoft.searchlib.util.bdb;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.jaeksoft.searchlib.util.bdb.AbstractCursor.BdbEntry;
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
		setKey(pattern, key);

		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);

		if (cursor.getSearchKeyRange(key, data, null) != OperationStatus.SUCCESS)
			return 0;

		int size = 0;

		while (start-- > 0) {
			if (!startsWith(key, pattern))
				return size;
			size++;
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return size;
		}

		data = new DatabaseEntry();
		while (rows-- > 0) {
			if (!startsWith(key, pattern))
				return size;
			size++;
			list.add(entryToObject(data));
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return size;
		}

		data.setPartial(0, 0, true);
		while (startsWith(key, pattern)) {
			size++;
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return size;
		}

		return size;
	}

	private class ListAdd implements BdbEntry {

		public List<T> list;

		public ListAdd(List<T> list) {
			this.list = list;
		}

		public void entry(DatabaseEntry data) {
			list.add(entryToObject(data));
		}
	}

	public long getLimit(AbstractCursor cursor, int start, int rows,
			List<T> list, LockMode lockMode) throws DatabaseException {

		long size = cursor.forward(start, lockMode);

		size += cursor.getRows(rows, lockMode, new ListAdd(list));

		return size + cursor.countLeft(lockMode);
	}

	public interface BdbFilter<T> {
		public boolean accept(T object);
	}

	public void getFilter(JoinCursor cursor, BdbFilter<T> filter,
			LockMode lockMode) throws DatabaseException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		while (cursor.getNext(key, data, lockMode) == OperationStatus.SUCCESS) {
			T object = entryToObject(data);
			if (!filter.accept(object))
				break;
		}
	}

	public void getFilter(JoinCursor cursor, List<T> list, long limit,
			BdbFilter<T> filter, LockMode lockMode) throws DatabaseException {

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		while (limit > 0) {
			if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
				return;
			T object = entryToObject(data);
			if (!filter.accept(object))
				continue;
			list.add(object);
			limit--;
		}
	}

}
