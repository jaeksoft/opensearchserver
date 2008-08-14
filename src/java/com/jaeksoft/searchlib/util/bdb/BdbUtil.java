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

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.jaeksoft.searchlib.util.PartialList;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;

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

	public void getStartsWith(Cursor cursor, String pattern, int start,
			int rows, PartialList<T> list) throws DatabaseException,
			UnsupportedEncodingException {

		DatabaseEntry key = new DatabaseEntry();
		setKey(pattern, key);

		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);

		if (cursor.getSearchKeyRange(key, data, null) != OperationStatus.SUCCESS)
			return;

		while (start-- > 0) {
			if (!startsWith(key, pattern))
				return;
			list.size++;
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return;
		}

		data = new DatabaseEntry();
		while (rows-- > 0) {
			if (!startsWith(key, pattern))
				return;
			list.size++;
			list.add(entryToObject(data));
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return;
		}

		data.setPartial(0, 0, true);
		while (startsWith(key, pattern)) {
			list.size++;
			if (cursor.getNext(key, data, null) != OperationStatus.SUCCESS)
				return;
		}
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

	private BdbList<T> getNewBdbList(PartialList<T> partialList) {
		partialList.reset();
		return new BdbList<T>(this, partialList);
	}

	public JointCursor getCursor(JoinCursor joinCursor,
			PartialList<T> partialList) {
		return new JointCursor(getNewBdbList(partialList), joinCursor);
	}

	public UniqueCursor getCursor(Cursor cursor, PartialList<T> partialList) {
		return new UniqueCursor(getNewBdbList(partialList), cursor);
	}

	public NonUniqueCursor getCursor(SecondaryCursor cursor,
			PartialList<T> partialList) {
		return new NonUniqueCursor(getNewBdbList(partialList), cursor);
	}

}
