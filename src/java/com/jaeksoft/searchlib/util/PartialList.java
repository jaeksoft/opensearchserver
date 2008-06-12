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

package com.jaeksoft.searchlib.util;

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;

public abstract class PartialList<T> extends AbstractList<T> {

	final private static Logger logger = Logger.getLogger(PartialList.class);

	protected int size;
	protected List<T> partialList;
	protected int currentStart;
	protected int rows;

	public PartialList(int rows) {
		this.rows = rows;
		this.currentStart = 0;
		this.partialList = null;
		this.size = 0;
	}

	@Override
	public T get(int index) {
		if (index < currentStart || index >= currentStart + rows)
			update(index);
		return partialList.get(index - currentStart);
	}

	protected abstract Query getQuery(Transaction transaction)
			throws SQLException;

	protected abstract Transaction getDatabaseTransaction() throws SQLException;

	protected abstract List<T> getResultList(Query query) throws Exception;

	protected void update(int start) {
		synchronized (this) {
			if (partialList != null)
				if (currentStart == start && currentStart != -1)
					return;
			Transaction transaction = null;
			try {
				transaction = getDatabaseTransaction();
				Query query = getQuery(transaction);
				currentStart = start;
				query.setFirstResult(currentStart);
				query.setMaxResults(rows);
				partialList = getResultList(query);
				size = query.getResultCount();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				partialList = null;
				size = 0;
			} catch (Exception e) {
				e.printStackTrace();
				partialList = null;
				size = 0;
			} finally {
				if (transaction != null)
					transaction.close();
			}
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		return "start: " + currentStart + " size: " + size;
	}

}
