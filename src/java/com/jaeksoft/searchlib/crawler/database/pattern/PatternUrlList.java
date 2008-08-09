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

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.util.PartialList;

public class PatternUrlList extends PartialList<PatternUrlItem> {

	private PatternUrlManager patternUrlManager;
	private String like;
	private boolean asc;

	public PatternUrlList(PatternUrlManager patternUrlManager, int windowRows,
			String like, boolean asc) {
		super(windowRows);
		this.patternUrlManager = patternUrlManager;
		this.like = like;
		this.asc = asc;
		update(0);
	}

	@Override
	protected void update(int start) {
		try {
			patternUrlManager.getPatterns(like, asc, start, windowRows, this);
		} catch (CrawlDatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
