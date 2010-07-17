/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.schema;

import java.util.List;
import java.util.Set;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.SimpleListModel;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.FilterEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerEnum;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AnalyzersController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -556387199220890770L;

	private String selectedName;

	private Analyzer selectedAnalyzer;

	private Analyzer editAnalyzer;

	private Analyzer currentAnalyzer;

	private FilterFactory currentFilter;

	private FilterEnum selectedFilter;

	public AnalyzersController() throws SearchLibException {
		super();
		editAnalyzer = null;
		selectedName = null;
		selectedAnalyzer = null;
		selectedFilter = FilterEnum.StandardFilter;
		currentFilter = FilterFactory.getDefaultFilter(getClient());
		currentAnalyzer = new Analyzer(getClient());
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		Combobox cb = (Combobox) getFellow("combotokenizer");
		cb.setModel(new SimpleListModel(getTokenizerList()));
	}

	public AnalyzerList getList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSchema().getAnalyzerList();
	}

	public List<Analyzer> getLangList() throws SearchLibException {
		if (getSelectedName() == null)
			return null;
		AnalyzerList analyzerList = getList();
		if (analyzerList == null)
			return null;
		return analyzerList.get(getSelectedName());
	}

	@Override
	public void reset() {
	}

	/**
	 * @param selectedName
	 *            the selectedName to set
	 */
	public void setSelectedName(String selectedName) {
		this.selectedName = selectedName;
		this.selectedAnalyzer = null;
	}

	/**
	 * @return the selectedName
	 * @throws SearchLibException
	 */
	public String getSelectedName() throws SearchLibException {
		if (selectedName == null) {
			Set<String> set = getList().getNameSet();
			if (set != null && set.size() > 0)
				selectedName = set.iterator().next();
		}
		return selectedName;
	}

	/**
	 * @param selectedAnalyzer
	 *            the selectedAnalyzer to set
	 */
	public void setSelectedAnalyzer(Analyzer analyzer) {
		this.selectedAnalyzer = analyzer;
	}

	/**
	 * @return the selectedAnalyzer
	 * @throws SearchLibException
	 */
	public Analyzer getSelectedAnalyzer() throws SearchLibException {
		if (selectedAnalyzer == null) {
			List<Analyzer> li = getLangList();
			if (li != null && li.size() > 0)
				setSelectedAnalyzer(li.get(0));
		}
		return selectedAnalyzer;
	}

	public boolean isEdit() throws SearchLibException {
		return editAnalyzer != null;
	}

	public boolean isNotEdit() throws SearchLibException {
		return !isEdit();
	}

	/**
	 * @return the editMode
	 */
	public String getEditMode() throws SearchLibException {
		if (editAnalyzer == null)
			return "Create a new analyzer";
		return "Editing analyzer: " + editAnalyzer.getName() + " - "
				+ editAnalyzer.getLang();
	}

	/**
	 * @return the currentAnalyzer
	 */
	public Analyzer getCurrentAnalyzer() {
		return currentAnalyzer;
	}

	public void onEdit() throws SearchLibException {
		editAnalyzer = getSelectedAnalyzer();
		if (editAnalyzer != null)
			currentAnalyzer.copyFrom(editAnalyzer);
		reloadPage();
	}

	public TokenizerEnum[] getTokenizerList() {
		return TokenizerEnum.values();
	}

	public FilterEnum[] getFilterEnum() {
		return FilterEnum.values();
	}

	/**
	 * @return the currentFilter
	 */
	public FilterFactory getCurrentFilter() {
		return currentFilter;
	}

	/**
	 * @param selectedFilter
	 *            the selectedFilter to set
	 * @throws SearchLibException
	 */
	public void setSelectedFilter(FilterEnum selectedFilter)
			throws SearchLibException {
		this.selectedFilter = selectedFilter;
		this.currentFilter = FilterFactory.create(getClient(),
				selectedFilter.name());
	}

	/**
	 * @return the selectedFilter
	 */
	public FilterEnum getSelectedFilter() {
		return selectedFilter;
	}

}
