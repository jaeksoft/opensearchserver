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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.DebugTokenFilter;
import com.jaeksoft.searchlib.analysis.FilterEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.FilterScope;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerEnum;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerFactory;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AnalyzersController extends CommonController implements
		ListitemRenderer {

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

	private String testText;

	private String testType;

	private List<DebugTokenFilter> testList;

	public AnalyzersController() throws SearchLibException {
		super();
		editAnalyzer = null;
		selectedName = null;
		selectedAnalyzer = null;
		selectedFilter = FilterEnum.StandardFilter;
		currentFilter = FilterFactory.getDefaultFilter(getClient());
		currentAnalyzer = new Analyzer(getClient());
		testType = "query";
		testText = null;
		testList = null;
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

	public String getCurrentTokenizer() {
		return getCurrentAnalyzer().getTokenizer().getClassName();
	}

	public void setCurrentTokenizer(String className) throws SearchLibException {
		getCurrentAnalyzer().setTokenizer(
				TokenizerFactory.create(getClient(), className));
		reloadPage();
	}

	public void onEdit() throws SearchLibException {
		editAnalyzer = getSelectedAnalyzer();
		if (editAnalyzer != null)
			currentAnalyzer.copyFrom(editAnalyzer);
		reloadPage();
	}

	public void onSave() throws InterruptedException, SearchLibException {
		if (editAnalyzer != null)
			selectedAnalyzer.copyFrom(currentAnalyzer);
		else
			getClient().getSchema().getAnalyzerList().add(currentAnalyzer);
		getClient().saveConfig();
		onCancel();
	}

	public void onCancel() throws SearchLibException {
		editAnalyzer = null;
		currentAnalyzer = new Analyzer(getClient());
		reloadPage();
	}

	public TokenizerEnum[] getTokenizerList() {
		return TokenizerEnum.values();
	}

	public FilterEnum[] getFilterEnum() {
		return FilterEnum.values();
	}

	public FilterScope[] getFilterScopeEnum() {
		return FilterScope.values();
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
		reloadPage();
	}

	/**
	 * @return the selectedFilter
	 */
	public FilterEnum getSelectedFilter() {
		return selectedFilter;
	}

	private FilterFactory getFilter(Component component) {
		return (FilterFactory) component.getParent().getAttribute("filteritem");
	}

	public void onFilterAdd() throws SearchLibException {
		currentAnalyzer.add(FilterFactory.create(currentFilter));
		reloadPage();
	}

	public void onFilterUp(Component component) {
		FilterFactory filter = getFilter(component);
		currentAnalyzer.filterUp(filter);
		reloadPage();
	}

	public void onFilterDown(Component component) {
		FilterFactory filter = getFilter(component);
		currentAnalyzer.filterDown(filter);
		reloadPage();
	}

	public void onFilterRemove(Component component) {
		FilterFactory filter = getFilter(component);
		currentAnalyzer.filterRemove(filter);
		reloadPage();
	}

	/**
	 * @param testType
	 *            the testType to set
	 */
	public void setTestType(String testType) {
		this.testType = testType;
	}

	/**
	 * @return the testType
	 */
	public String getTestType() {
		return testType;
	}

	/**
	 * @param testText
	 *            the testText to set
	 */
	public void setTestText(String testText) {
		this.testText = testText;
	}

	/**
	 * @return the testText
	 */
	public String getTestText() {
		return testText;
	}

	public void onTest() throws IOException {
		CompiledAnalyzer compiledAnalyzer = ("query".equals(testType)) ? currentAnalyzer
				.getQueryAnalyzer() : currentAnalyzer.getIndexAnalyzer();
		testList = compiledAnalyzer.test(testText);
		reloadPage();
	}

	public List<DebugTokenFilter> getTestList() {
		return testList;
	}

	@Override
	public void render(Listitem item, Object data) throws Exception {
		DebugTokenFilter debugFilter = (DebugTokenFilter) data;
		Listcell listcell = new Listcell(debugFilter.getClassFactory()
				.getClassName());
		listcell.setParent(item);
		listcell = new Listcell();
		Hbox hbox = new Hbox();
		for (String term : debugFilter.getTokenList()) {
			Window window = new Window();
			window.setBorder("normal");
			new Label(term).setParent(window);
			window.setParent(hbox);
		}
		hbox.setParent(listcell);
		listcell.setParent(item);
	}
}
