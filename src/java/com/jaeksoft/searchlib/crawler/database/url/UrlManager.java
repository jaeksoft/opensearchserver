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

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlItem;
import com.jaeksoft.searchlib.crawler.spider.Crawl;

public abstract class UrlManager {

	public enum Field {

		URL("url"), WHEN("when"), RETRY("retry"), FETCHSTATUS("fetchStatus"), PARSERSTATUS(
				"parserStatus"), INDEXSTATUS("indexStatus"), HOST("host");

		private String name;

		private Field(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public abstract void delete(String sUrl) throws SQLException;

	public abstract void update(Crawl crawl) throws SQLException,
			MalformedURLException;

	public abstract void inject(List<InjectUrlItem> list);

	public abstract void injectPrefix(List<PatternUrlItem> patternList);

	public abstract List<HostCountItem> getHostToFetch(int fetchInterval,
			int limit) throws SQLException;

	public abstract List<UrlItem> getUrlToFetch(HostCountItem host,
			int fetchInterval, int limit) throws SQLException;
}