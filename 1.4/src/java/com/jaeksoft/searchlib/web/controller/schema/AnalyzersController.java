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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

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

public class AnalyzersController extends CommonController {

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
	@NotifyChange("*")
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
	public void setSelectedLang(Analyzer analyzer) {
		this.selectedAnalyzer = analyzer;
	}

	/**
	 * @return the selectedAnalyzer
	 * @throws SearchLibException
	 */
	public Analyzer getSelectedLang() throws SearchLibException {
		if (selectedAnalyzer != null)
			return selectedAnalyzer;
		List<Analyzer> li = getLangList();
		if (li != null && li.size() > 0)
			setSelectedLang(li.get(0));
		return selectedAnalyzer;
	}

	public boolean isSelectedAnalyzer() throws SearchLibException {
		return getSelectedLang() != null;
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

	@NotifyChange({ ".", "currentAnalyzer" })
	public void setCurrentTokenizer(String className) throws SearchLibException {
		getCurrentAnalyzer().setTokenizer(
				TokenizerFactory.create(getClient(), className));
	}

	@Command
	public void onEdit() throws SearchLibException {
		editAnalyzer = getSelectedLang();
		if (editAnalyzer != null)
			currentAnalyzer.copyFrom(editAnalyzer);
		reload();
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		if (getSelectedLang() == null)
			return;
		new DeleteAlert(getSelectedLang());
		reload();
	}

	@Command
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

	@Command
	public void onCancel() throws SearchLibException {
		editAnalyzer = null;
		currentAnalyzer = new Analyzer(getClient());
		reload();
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
	@NotifyChange({ "filterScopeEnum", "currentFilter" })
	public void setSelectedFilter(FilterEnum selectedFilter)
			throws SearchLibException {
		this.selectedFilter = selectedFilter;
		this.currentFilter = FilterFactory.create(getClient(),
				selectedFilter.name());
		reload();
	}

	/**
	 * @return the selectedFilter
	 */
	public FilterEnum getSelectedFilter() {
		return selectedFilter;
	}

	@Command
	@NotifyChange("currentAnalyzer")
	public void onFilterAdd() throws SearchLibException {
		currentAnalyzer.add(FilterFactory.create(currentFilter));
	}

	@Command
	@NotifyChange("currentAnalyzer")
	public void onFilterUp(@BindingParam("filterItem") FilterFactory filter)
			throws SearchLibException {
		currentAnalyzer.filterUp(filter);
	}

	@Command
	@NotifyChange("currentAnalyzer")
	public void onFilterDown(@BindingParam("filterItem") FilterFactory filter)
			throws SearchLibException {
		currentAnalyzer.filterDown(filter);
		reload();
	}

	@Command
	@NotifyChange("currentAnalyzer")
	public void onFilterRemove(@BindingParam("filterItem") FilterFactory filter)
			throws SearchLibException {
		currentAnalyzer.filterRemove(filter);
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

	@Command
	@NotifyChange("testList")
	public void onTest() throws IOException, SearchLibException {
		CompiledAnalyzer compiledAnalyzer = ("query".equals(testType)) ? currentAnalyzer
				.getQueryAnalyzer() : currentAnalyzer.getIndexAnalyzer();
		testList = compiledAnalyzer.test(testText);
		reload();
		compiledAnalyzer.close();
	}

	public List<DebugTokenFilter> getTestList() {
		return testList;
	}

}
