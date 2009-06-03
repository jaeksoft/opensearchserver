/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.PatternSelector;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class PatternController extends CommonController implements
		ListitemRenderer, PatternSelector, AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6735801464584819587L;

	transient private List<PatternItem> patternList = null;

	private String like;

	private String pattern;

	private int pageSize;

	private int totalSize;

	private int activePage;

	private Set<String> selection;

	public PatternController() throws SearchLibException {
		super();
		patternList = null;
		pattern = null;
		like = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selection = new TreeSet<String>();
	}

	public void afterCompose() {
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			public void onEvent(Event event) {
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

	public List<PatternItem> getPatternList() {
		synchronized (this) {
			if (patternList != null)
				return patternList;
			try {
				PatternManager patternManager = getClient().getPatternManager();
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

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			patternList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() {
		synchronized (this) {
			patternList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	public boolean isSelection() {
		synchronized (this) {
			if (patternList == null)
				return false;
			return (getSelectionCount() > 0);
		}
	}

	public void onDelete() throws SearchLibException {
		synchronized (this) {
			PatternManager patternManager = getClient().getPatternManager();
			try {
				deleteSelection(patternManager);
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
			onSearch();
		}
	}

	public void onSelect(Event event) {
		PatternItem patternItem = (PatternItem) event.getData();
		patternItem.setSelected(!patternItem.isSelected());
		reloadPage();
	}

	public void render(Listitem item, Object data) throws Exception {
		PatternItem patternItem = (PatternItem) data;
		item.setLabel(patternItem.getPattern());
		item.setSelected(patternItem.isSelected());
		item.addForward(null, this, "onSelect", patternItem);
	}

	public void addSelection(PatternItem item) {
		synchronized (selection) {
			selection.add(item.getPattern());
		}
	}

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

	public boolean isSelected(PatternItem item) {
		synchronized (selection) {
			return selection.contains(item.getPattern());
		}
	}

	public void deleteSelection(PatternManager patternManager)
			throws SearchLibException {
		synchronized (selection) {
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
			List<PatternItem> list = PatternManager.getPatternList(pattern);
			if (list.size() > 0) {
				getClient().getPatternManager().addList(list, false);
				getClient().getUrlManager().injectPrefix(list);
			}
			setPattern(PatternManager.getStringPatternList(list));
			patternList = null;
			reloadPage();
		}
	}
}