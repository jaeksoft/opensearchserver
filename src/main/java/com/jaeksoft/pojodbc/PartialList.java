/**   
 * License Agreement for OpenSearchServer Pojodbc
 *
 * Copyright 2008-2013 Emmanuel Keller / Jaeksoft
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.pojodbc;

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.List;

public abstract class PartialList<T> extends AbstractList<T> {

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

	protected abstract Query getQuery(Transaction transaction) throws SQLException;

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
			} catch (Exception e) {
				partialList = null;
				size = 0;
				throw new RuntimeException(e);
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
