/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.Selector;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public abstract class AbstractPatternController extends CrawlerController
		implements ListitemRenderer, Selector<PatternItem>, AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8704978554029382442L;

	private transient List<PatternItem> patternList;

	private transient String like;

	private transient String pattern;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	private transient Set<String> selection;

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
		selection = new TreeSet<String>();
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) throws SearchLibException {
				onPaging((PagingEvent) event);
			}
		});
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
				for (PatternItem patternUrlItem : patternList)
					patternUrlItem.setPatternSelector(this);
				return patternList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void onPaging(PagingEvent pagingEvent) throws SearchLibException {
		synchronized (this) {
			patternList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() throws SearchLibException {
		synchronized (this) {
			patternList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	public boolean isSelectionRemovable() throws SearchLibException {
		synchronized (this) {
			if (patternList == null)
				return false;
			if (!isWebCrawlerEditPatternsRights())
				return false;
			return (getSelectionCount() > 0);
		}
	}

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

	public void onSelect(Event event) throws SearchLibException {
		PatternItem patternItem = (PatternItem) event.getData();
		patternItem.setSelected(!patternItem.isSelected());
		reloadPage();
	}

	@Override
	public void render(Listitem item, Object data) throws Exception {
		PatternItem patternItem = (PatternItem) data;
		item.setLabel(patternItem.getPattern());
		item.setSelected(patternItem.isSelected());
		item.addForward(null, this, "onSelect", patternItem);
	}

	@Override
	public void addSelection(PatternItem item) {
		synchronized (selection) {
			selection.add(item.getPattern());
		}
	}

	@Override
	public void removeSelection(PatternItem item) {
		synchronized (selection) {
			selection.remove(item.getPattern());
		}
	}

	public int getSelectionCount() {
		synchronized (selection) {
			return selection.size();
		}
	}

	@Override
	public boolean isSelected(PatternItem item) {
		synchronized (selection) {
			return selection.contains(item.getPattern());
		}
	}

	public void deleteSelection(PatternManager patternManager)
			throws SearchLibException {
		synchronized (selection) {
			if (!isWebCrawlerEditPatternsRights())
				throw new SearchLibException("Not allowed");
			patternManager.delPattern(selection);
			selection.clear();
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
			reloadPage();
		}
	}

	@Override
	public void reloadPage() throws SearchLibException {
		synchronized (this) {
			patternList = null;
			super.reloadPage();
		}
	}

}