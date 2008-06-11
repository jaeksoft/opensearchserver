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

package com.jaeksoft.searchlib.crawler.urldb;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.urldb.UrlManager.Field;
import com.jaeksoft.searchlib.util.PartialList;

public class UrlList extends PartialList<UrlItem> {

	private Config config;
	private String like;
	private String host;
	private UrlStatus status;
	private Date startDate;
	private Date endDate;
	private Field orderBy;
	private boolean asc;

	public UrlList(Config config, int rows, String like, String host,
			UrlStatus status, Date startDate, Date endDate, Field orderBy,
			boolean asc) {
		super(rows);
		this.config = config;
		this.like = like;
		this.host = host;
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
		this.orderBy = orderBy;
		this.asc = asc;
		update(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<UrlItem> getResultList(Query query) throws Exception {
		return (List<UrlItem>) query.getResultList(UrlItem.class);
	}

	@Override
	protected Query getQuery(Transaction transaction) throws SQLException {
		return config.getUrlManager().getUrl(transaction, like, host, status,
				startDate, endDate, orderBy, asc);
	}

	@Override
	protected Transaction getDatabaseTransaction() throws SQLException {
		return config.getDatabaseTransaction();
	}

}
