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

package com.jaeksoft.searchlib.crawler.database.pattern;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.url.InjectUrlItem;

public abstract class PatternUrlManager {

	protected Hashtable<String, ArrayList<PatternUrlItem>> patternUrlMap = null;

	public abstract void addList(List<PatternUrlItem> patternList)
			throws CrawlDatabaseException;

	public abstract void delPattern(PatternUrlItem item)
			throws CrawlDatabaseException;

	protected abstract void updateCache() throws CrawlDatabaseException;

	public PatternUrlItem findPatternUrl(URL url) {
		ArrayList<PatternUrlItem> patternList = null;
		synchronized (this) {
			patternList = patternUrlMap.get(url.getHost());
		}
		if (patternList == null)
			return null;
		synchronized (patternList) {
			String sUrl = url.toExternalForm();
			for (PatternUrlItem patternItem : patternList)
				if (patternItem.match(sUrl))
					return patternItem;
			return null;
		}
	}

	public void injectUrl(List<InjectUrlItem> urlListItems)
			throws CrawlDatabaseException {
		Iterator<InjectUrlItem> it = urlListItems.iterator();
		List<PatternUrlItem> patternList = new ArrayList<PatternUrlItem>();
		while (it.hasNext()) {
			InjectUrlItem item = it.next();
			if (findPatternUrl(item.getURL()) != null)
				continue;
			patternList.add(new PatternUrlItem(item.getURL()));
		}
		addList(patternList);
	}

	public abstract List<PatternUrlItem> getPatterns(String like, boolean asc,
			int start, int rows, PatternUrlList urlList)
			throws CrawlDatabaseException;
}
