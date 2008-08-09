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

import java.util.HashSet;
import java.util.Iterator;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.util.PartialList;

public class PatternUrlList extends PartialList<PatternUrlItem> {

	private PatternUrlManager patternUrlManager;
	private String like;
	private HashSet<String> selection;

	public PatternUrlList(PatternUrlManager patternUrlManager, int windowRows,
			String like) {
		super(windowRows);
		this.patternUrlManager = patternUrlManager;
		this.like = like;
		selection = new HashSet<String>();
		update(0);
	}

	@Override
	protected void update(int start) {
		try {
			patternUrlManager.getPatterns(like, start, windowRows, this);
			Iterator<PatternUrlItem> it = iterator();
			while (it.hasNext())
				it.next().setPatternUrlList(this);
		} catch (CrawlDatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	protected void addSelection(PatternUrlItem item) {
		synchronized (selection) {
			selection.add(item.getPattern());
		}
	}

	protected void removeSelection(PatternUrlItem item) {
		synchronized (selection) {
			selection.remove(item.getPattern());
		}
	}

	public int getSelectionCount() {
		synchronized (selection) {
			return selection.size();
		}
	}

	protected boolean isSelected(PatternUrlItem item) {
		synchronized (selection) {
			return selection.contains(item.getPattern());
		}
	}

	public void deleteSelection(PatternUrlManager patternManager)
			throws CrawlDatabaseException {
		synchronized (selection) {
			Iterator<String> it = selection.iterator();
			while (it.hasNext())
				patternManager.delPattern(new PatternUrlItem(it.next()));
			selection.clear();
		}
	}
}
