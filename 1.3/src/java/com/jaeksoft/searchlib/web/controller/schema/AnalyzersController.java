/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
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
import org.zkoss.zul.Messagebox;
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
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.web.SchemaServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AnalyzersController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -556387199220890770L;

	private transient String selectedName;

	private transient Analyzer selectedAnalyzer;

	private transient Analyzer editAnalyzer;

	private transient Analyzer currentAnalyzer;

	private transient FilterFactory currentFilter;

	private transient FilterEnum selectedFilter;

	private transient String testText;

	private transient String testType;

	private transient List<DebugTokenFilter> testList;

	private class DeleteAlert extends AlertController {

		private transient Analyzer deleteAnalyzer;

		protected DeleteAlert(Analyzer deleteAnalyzer)
				throws InterruptedException {
			super("Please, confirm that you want to delete the analyzer: "
					+ deleteAnalyzer.getName() + " "
					+ deleteAnalyzer.getLang().getName(), Messagebox.YES
					| Messagebox.NO, Messagebox.QUESTION);
			this.deleteAnalyzer = deleteAnalyzer;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			Schema schema = client.getSchema();
			schema.getAnalyzerList().remove(deleteAnalyzer);
			client.saveConfig();
			schema.recompileAnalyzers();
			onCancel();
		}
	}

	public AnalyzersController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() throws SearchLibException {
		editAnalyzer = null;
		selectedName = null;
		selectedAnalyzer = null;
		selectedFilter = FilterEnum.StandardFilter;
		currentAnalyzer = null;
		currentFilter = null;
		Client client = getClient();
		if (client != null) {
			currentFilter = FilterFactory.getDefaultFilter(client);
			currentAnalyzer = new Analyzer(client);
		}
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
		if (selectedName != null)
			return selectedName;
		AnalyzerList alist = getList();
		if (alist == null)
			return null;
		Set<String> set = alist.getNameSet();
		if (set != null && set.size() > 0)
			selectedName = set.iterator().next();
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
		if (selectedAnalyzer != null)
			return selectedAnalyzer;
		List<Analyzer> li = getLangList();
		if (li != null && li.size() > 0)
			setSelectedAnalyzer(li.get(0));
		return selectedAnalyzer;
	}

	public boolean isSelectedAnalyzer() throws SearchLibException {
		return getSelectedAnalyzer() != null;
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
		Analyzer analyzer = getCurrentAnalyzer();
		if (analyzer == null)
			return null;
		TokenizerFactory tokenizer = analyzer.getTokenizer();
		if (tokenizer == null)
			return null;
		return tokenizer.getClassName();
	}

	public void setCurrentTokenizer(String className) throws SearchLibException {
		getCurrentAnalyzer().setTokenizer(
				TokenizerFactory.create(getClient(), className));
	}

	public void onEdit() throws SearchLibException {
		editAnalyzer = getSelectedAnalyzer();
		if (editAnalyzer != null)
			currentAnalyzer.copyFrom(editAnalyzer);
		reloadPage();
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		if (getSelectedAnalyzer() == null)
			return;
		new DeleteAlert(getSelectedAnalyzer());
		reloadPage();
	}

	public void onSave() throws InterruptedException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		if (editAnalyzer != null)
			selectedAnalyzer.copyFrom(currentAnalyzer);
		else
			client.getSchema().getAnalyzerList().add(currentAnalyzer);
		SchemaServlet.saveSchema(client, client.getSchema());
		onCancel();
	}

	public void onCancel() throws SearchLibException {
		editAnalyzer = null;
		currentAnalyzer = new Analyzer(getClient());
		reloadPage();
	}

	public String[] getTokenizerList() {
		return TokenizerEnum.getStringArray();
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

	public void onFilterUp(Component component) throws SearchLibException {
		FilterFactory filter = getFilter(component);
		currentAnalyzer.filterUp(filter);
		reloadPage();
	}

	public void onFilterDown(Component component) throws SearchLibException {
		FilterFactory filter = getFilter(component);
		currentAnalyzer.filterDown(filter);
		reloadPage();
	}

	public void onFilterRemove(Component component) throws SearchLibException {
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

	public void onTest() throws IOException, SearchLibException {
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
