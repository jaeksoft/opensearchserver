/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public abstract class AbstractPatternController extends CrawlerController {

	private transient List<PatternItem> patternList;

	private transient String like;

	private transient String pattern;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	private transient Set<PatternItem> selectionSet;

	public AbstractPatternController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		patternList = null;
		pattern = null;
		like = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selectionSet = new TreeSet<PatternItem>();
	}

	public void setPageSize(int v) {
		pageSize = v;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setLike(String v) {
		if (v == like)
			return;
		like = v;
	}

	public String getLike() {
		return like;
	}

	protected abstract PatternManager getPatternManager()
			throws SearchLibException;

	protected abstract boolean isInclusion();

	public abstract PropertyItem<Boolean> getEnabled()
			throws SearchLibException;

	public List<PatternItem> getPatternList() {
		synchronized (this) {
			if (patternList != null)
				return patternList;
			try {
				Client client = getClient();
				if (client == null)
					return null;
				PatternManager patternManager = getPatternManager();
				patternList = new ArrayList<PatternItem>();
				totalSize = patternManager.getPatterns(like, getActivePage()
						* getPageSize(), getPageSize(), patternList);
				selectionSet.clear();
				return patternList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Set<PatternItem> getSelectedItems() {
		return selectionSet;
	}

	@NotifyChange("*")
	public void setSelectedItems(Set<PatternItem> selectionSet)
			throws SearchLibException {
		this.selectionSet = selectionSet;
	}

	public void setActivePage(int page) throws SearchLibException {
		patternList = null;
		this.activePage = page;
		reload();
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			patternList = null;
			activePage = 0;
			totalSize = 0;
			reload();
		}
	}

	public boolean isSelectionRemovable() throws SearchLibException {
		synchronized (this) {
			if (patternList == null)
				return false;
			if (!isWebCrawlerEditPatternsRights())
				return false;
			return (selectionSet.size() > 0);
		}
	}

	@Command
	public void onDelete() throws SearchLibException {
		synchronized (this) {
			if (!isWebCrawlerEditPatternsRights())
				throw new SearchLibException("Not allowed");
			PatternManager patternManager = getPatternManager();
			try {
				deleteSelection(patternManager);
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
			onSearch();
		}
	}

	@Command
	public void onDeleteAll() throws SearchLibException {
		synchronized (this) {
			if (!isWebCrawlerEditPatternsRights())
				throw new SearchLibException("Not allowed");
			PatternManager patternManager = getPatternManager();
			try {
				patternManager.addList(null, true);
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
			onSearch();
		}

	}

	public void deleteSelection(PatternManager patternManager)
			throws SearchLibException {
		synchronized (selectionSet) {
			if (!isWebCrawlerEditPatternsRights())
				throw new SearchLibException("Not allowed");
			patternManager.delPatternItem(selectionSet);
			selectionSet.clear();
		}
	}

	public String getPattern() {
		synchronized (this) {
			return pattern;
		}
	}

	public void setPattern(String v) {
		synchronized (this) {
			pattern = v;
		}
	}

	@Command
	public void onAdd() throws SearchLibException {
		synchronized (this) {
			if (!isWebCrawlerEditPatternsRights())
				throw new SearchLibException("Not allowed");
			List<PatternItem> list = PatternManager.getPatternList(pattern);
			if (list.size() > 0) {
				getPatternManager().addList(list, false);
				if (isInclusion())
					getClient().getUrlManager().injectPrefix(list);
			}
			setPattern(PatternManager.getStringPatternList(list));
			patternList = null;
			reload();
		}
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			patternList = null;
			super.reload();
		}
	}

}