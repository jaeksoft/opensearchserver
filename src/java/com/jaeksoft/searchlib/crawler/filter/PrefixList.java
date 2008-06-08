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

package com.jaeksoft.searchlib.crawler.filter;

import java.sql.SQLException;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.PartialList;

public class PrefixList extends PartialList<PrefixItem> {

	private Config config;
	private String like;
	private boolean asc;

	public PrefixList(Config config, int rows, String like, boolean asc) {
		super(rows);
		this.config = config;
		this.like = like;
		this.asc = asc;
		update(0);
	}

	@Override
	protected Transaction getDatabaseTransaction() throws SQLException {
		return config.getDatabaseTransaction();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<PrefixItem> getResultList(Query query) throws Exception {
		return (List<PrefixItem>) query.getResultList(PrefixItem.class);
	}

	@Override
	protected Query getQuery(Transaction transaction) throws SQLException {
		return config.getPrefixUrlFilter().getPrefix(transaction, like, asc);
	}

}
