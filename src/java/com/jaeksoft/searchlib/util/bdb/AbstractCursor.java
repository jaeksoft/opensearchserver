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

import com.jaeksoft.searchlib.util.PartialList;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;

public abstract class AbstractCursor {

	protected boolean abort;
	protected BdbEntry entry;
	protected PartialList<?> list;

	protected AbstractCursor(BdbList<?> bdbList) {
		abort = false;
		this.list = bdbList.list;
		this.entry = bdbList;
	}

	public void abort() {
		abort = true;
	}

	public abstract void countLeft(LockMode lockMode) throws DatabaseException;

	public abstract void forward(long offset, LockMode lockMode)
			throws DatabaseException;

	public abstract void getRows(long rows, LockMode lockMode)
			throws DatabaseException;

	public void getLimit(long start, long rows, LockMode lockMode)
			throws DatabaseException {

		forward(start, lockMode);

		getRows(rows, lockMode);

		countLeft(lockMode);
	}

}
