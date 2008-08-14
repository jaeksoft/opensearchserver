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

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.util.Timer;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;

public class NonUniqueCursor extends AbstractCursor {

	final private static Logger logger = Logger
			.getLogger(NonUniqueCursor.class);

	private SecondaryCursor cursor;

	protected NonUniqueCursor(BdbList<?> bdbList, SecondaryCursor cursor) {
		super(bdbList);
		this.cursor = cursor;
	}

	public void countLeft(LockMode lockMode) throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("CountLeft Cursor");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);
		while (!abort
				&& cursor.getNextNoDup(key, data, lockMode) == OperationStatus.SUCCESS)
			list.size += cursor.count();

		if (timer != null)
			logger.info(timer + " (" + list.size + ")");
	}

	public void forward(long offset, LockMode lockMode)
			throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("Forward Cursor (" + offset + ")");

		DatabaseEntry key = new DatabaseEntry();
		key.setPartial(0, 0, true);
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(0, 0, true);
		while (!abort && offset-- > 0) {
			if (cursor.getNextDup(key, data, lockMode) != OperationStatus.SUCCESS)
				if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
					break;
			list.size++;
		}

		if (timer != null)
			logger.info(timer + " (" + list.size + ")");
	}

	public void getRows(long rows, LockMode lockMode) throws DatabaseException {

		Timer timer = null;
		if (logger.isInfoEnabled())
			timer = new Timer("GetRows Cursor (" + rows + ")");

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		while (!abort && rows-- > 0) {
			if (cursor.getNextDup(key, data, lockMode) != OperationStatus.SUCCESS)
				if (cursor.getNext(key, data, lockMode) != OperationStatus.SUCCESS)
					break;
			entry.entry(data);
			list.size++;
		}

		if (timer != null)
			logger.info(timer + " (" + list.size + ")");
	}

}
