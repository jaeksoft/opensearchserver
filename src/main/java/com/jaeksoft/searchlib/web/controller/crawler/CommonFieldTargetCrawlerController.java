/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.web.controller.AlertController;

@AfterCompose(superclass = true)
public abstract class CommonFieldTargetCrawlerController<C extends FieldMapCrawlItem<?, T, M>, T extends CrawlThreadAbstract<T, M>, M extends CrawlMasterAbstract<M, T>>
		extends CrawlerController {

	private transient GenericLink<SourceField, CommonFieldTarget> selectedField;

	private transient String sourceFieldName;

	private transient CommonFieldTarget currentFieldTarget;

	private transient List<String> indexFieldList;

	private transient C currentCrawl;

	private transient C selectedCrawl;

	private class DeleteAlert extends AlertController {

		private C deleteCrawl;

		protected DeleteAlert(C deleteCrawl) throws InterruptedException {
			super("Please, confirm that you want to delete the crawl process: "
					+ deleteCrawl, Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.deleteCrawl = deleteCrawl;
		}

		@Override
		protected void onYes() throws SearchLibException, IOException {
			doDelete(deleteCrawl);
			onCancel();
		}
	}

	public CommonFieldTargetCrawlerController() throws SearchLibException,
			NamingException {
		super();
	}

	protected abstract void doDelete(C crawlItem) throws SearchLibException,
			IOException;

	@Override
	protected void reset() throws SearchLibException {
		selectedField = null;
		selectedCrawl = null;
		sourceFieldName = null;
		currentFieldTarget = newCommonFieldTarget();
		indexFieldList = null;
	}

	private CommonFieldTarget newCommonFieldTarget() throws SearchLibException {
		String fieldName = null;
		List<String> list = getIndexFieldList();
		if (list != null && list.size() > 0)
			fieldName = list.get(0);
		return new CommonFieldTarget(fieldName, false, false, false, null,
				false, null, null);
	}

	public C getCurrentCrawl() {
		return currentCrawl;
	}

	protected void setCurrentCrawl(C crawl) {
		this.currentCrawl = crawl;
	}

	public boolean isSelected() {
		return selectedCrawl != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public boolean isEditing() {
		return currentCrawl != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public C getSelectedCrawl() {
		return selectedCrawl;
	}

	protected abstract C newCrawlItem(C crawl);

	public void setSelectedCrawl(C crawl) throws SearchLibException {
		selectedCrawl = crawl;
		if (crawl != null)
			currentCrawl = newCrawlItem(selectedCrawl);
		reload();
	}

	@Command
	public void onCancelField() throws SearchLibException {
		selectedField = null;
		sourceFieldName = null;
		currentFieldTarget = newCommonFieldTarget();
		reload();
	}

	public abstract boolean isCrawlerEditRights() throws SearchLibException;

	@Command
	public abstract void onSave() throws InterruptedException,
			SearchLibException, IOException;

	@Command
	public abstract void onNew() throws SearchLibException;

	@Command
	public void onSaveField() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (!isCrawlerEditRights())
			throw new SearchLibException("Not allowed");
		if (currentFieldTarget == null || currentFieldTarget.getName() == null
				|| currentFieldTarget.getName().length() == 0)
			throw new SearchLibException("Error");
		if (selectedField != null)
			currentCrawl.getFieldMap().remove(selectedField);
		currentCrawl.getFieldMap().add(new SourceField(sourceFieldName),
				currentFieldTarget);
		onCancelField();
	}

	@Command
	public void removeLink(
			@BindingParam("fieldlink") GenericLink<SourceField, CommonFieldTarget> fieldLink)
			throws SearchLibException, InterruptedException {
		currentCrawl.getFieldMap().remove(fieldLink);
		reload();
	}

	@Command
	public void onCancel() throws SearchLibException {
		currentCrawl = null;
		selectedCrawl = null;
		reload();
	}

	@Command
	public void delete(@BindingParam("crawlitem") C item)
			throws SearchLibException, InterruptedException {
		new DeleteAlert(item);
	}

	@Command
	public void edit(@BindingParam("crawlitem") C item)
			throws SearchLibException, InterruptedException {
		setSelectedCrawl(item);
	}

	@Command
	public void execute(@BindingParam("crawlitem") C item)
			throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		getCrawlMaster().execute(client, item, false, null, null);
		reload();
	}

	@Command
	public void abort(@BindingParam("crawlitem") C item)
			throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		T thread = item.getLastThread();
		if (thread.isAborted())
			return;
		if (thread.isAborting())
			return;
		if (!thread.isRunning())
			return;
		thread.abort();
		reload();
	}

	@Override
	public abstract M getCrawlMaster() throws SearchLibException;

	public String getCurrentEditMode() throws SearchLibException {
		return selectedCrawl == null ? "Create a new crawl process"
				: "Edit the crawl process : " + selectedCrawl.toString();
	}

	public List<String> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			if (indexFieldList != null)
				return indexFieldList;
			Client client = getClient();
			if (client == null)
				return null;
			indexFieldList = new ArrayList<String>();
			client.getSchema().getFieldList().toNameList(indexFieldList);
			return indexFieldList;
		}
	}

	public void setSelectedIndexField(String field) {
		synchronized (this) {
			currentFieldTarget.setName(field);
		}
	}

	public String getSelectedIndexField() {
		synchronized (this) {
			return currentFieldTarget.getName();
		}
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		M crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getThreadsCount() > 0;
	}

	/**
	 * @return the currentFieldTarget
	 */
	public CommonFieldTarget getCurrentFieldTarget() {
		return currentFieldTarget;
	}

	/**
	 * @return the selectedField
	 */
	public GenericLink<SourceField, CommonFieldTarget> getSelectedField() {
		return selectedField;
	}

	/**
	 * @param selectedField
	 *            the selectedField to set
	 * @throws SearchLibException
	 */
	public void setSelectedField(
			GenericLink<SourceField, CommonFieldTarget> selectedField)
			throws SearchLibException {
		this.selectedField = selectedField;
		this.sourceFieldName = selectedField.getSource().getUniqueName();
		currentFieldTarget = new CommonFieldTarget(selectedField.getTarget());
		reload();
	}

	public boolean isFieldSelected() {
		return selectedField != null;
	}

	public boolean isNoFieldSelected() {
		return !isFieldSelected();
	}

	/**
	 * @return the sourceFieldName
	 */
	public String getSourceFieldName() {
		return sourceFieldName;
	}

	/**
	 * @param sourceFieldName
	 *            the sourceFieldName to set
	 */
	public void setSourceFieldName(String sourceFieldName) {
		this.sourceFieldName = sourceFieldName;
	}

}
