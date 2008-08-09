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

package com.jaeksoft.searchlib.crawler.database.url;

import java.util.Date;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.url.UrlManager.Field;
import com.jaeksoft.searchlib.util.PartialList;

public class UrlList extends PartialList<UrlItem> {

	private UrlManager urlManager;
	private String like;
	private String host;
	private FetchStatus fetchStatus;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;
	private Date startDate;
	private Date endDate;
	private Field orderBy;

	public UrlList(UrlManager urlManager, int windowRows, String like,
			String host, FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate, Field orderBy) {
		super(windowRows);
		this.urlManager = urlManager;
		this.like = like;
		this.host = host;
		this.fetchStatus = fetchStatus;
		this.parserStatus = parserStatus;
		this.indexStatus = indexStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.orderBy = orderBy;
		update(0);
	}

	@Override
	protected void update(int start) {
		try {
			urlManager.getUrls(like, host, fetchStatus, parserStatus,
					indexStatus, startDate, endDate, orderBy, start,
					windowRows, this);
		} catch (CrawlDatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
