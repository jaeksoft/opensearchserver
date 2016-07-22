/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;
import com.jaeksoft.searchlib.learning.LearnerResultItem;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskLearnerRun;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

@AfterCompose(superclass = true)
public class LearningController extends CommonController {

	private Learner selectedLearner;
	private Learner currentLearner;

	private transient String selectedSourceIndexField;
	private transient String selectedSourceLearnerField;
	private transient Float selectedSourceLearnerFieldBoost;
	private transient SchemaField selectedTargetIndexField;
	private transient String selectedTargetLearnerField;

	private transient int totalSize;
	private transient int activePage;

	private transient String testText;
	private transient LearnerResultItem[] learnerResultItems;

	public LearningController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentLearner = null;
		selectedLearner = null;
		selectedSourceIndexField = null;
		selectedSourceLearnerField = null;
		selectedTargetIndexField = null;
		selectedTargetLearnerField = null;
		selectedSourceLearnerFieldBoost = null;
		totalSize = 0;
		activePage = 0;
		testText = null;
		learnerResultItems = null;
	}

	public Learner[] getLearners() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getLearnerManager().getArray();
	}

	public List<SchemaField> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList().getList();
			if (list.size() > 0) {
				if (selectedSourceIndexField == null)
					selectedSourceIndexField = list.get(0).getName();
				if (selectedTargetIndexField == null)
					selectedTargetIndexField = list.get(0);
			}
			return list;
		}
	}

	public void setSelectedSourceIndexField(String fieldName) {
		synchronized (this) {
			selectedSourceIndexField = fieldName;
		}
	}

	public String getSelectedSourceIndexField() {
		synchronized (this) {
			return selectedSourceIndexField;
		}
	}

	public void setSelectedTargetIndexField(SchemaField field) {
		synchronized (this) {
			selectedTargetIndexField = field;
		}
	}

	public SchemaField getSelectedTargetIndexField() {
		synchronized (this) {
			return selectedTargetIndexField;
		}
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedLearner == null ? "Create a new learner" : "Edit the selected learner";
	}

	public boolean isEditing() {
		return currentLearner != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public boolean isSelected() {
		return selectedLearner != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public List<String> getRequestList() throws SearchLibException {
		List<String> requestList = new ArrayList<String>(0);
		Client client = getClient();
		if (client == null)
			return requestList;
		client.getRequestMap().getNameList(requestList, RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
		return requestList;
	}

	@Command
	@NotifyChange("*")
	public void onNewLearner() throws SearchLibException {
		currentLearner = new Learner(getClient());
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		currentLearner = null;
		selectedLearner = null;
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException, UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return;
		LearnerManager lm = client.getLearnerManager();
		if (selectedLearner != null)
			lm.set(currentLearner);
		else
			lm.add(currentLearner);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onDelete() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getLearnerManager().remove(selectedLearner.getName());
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onClassify() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		if (currentLearner == null)
			return;
		learnerResultItems = currentLearner.classify(client, testText, null, null);
	}

	public LearnerResultItem[] getLearnerResultItems() {
		return learnerResultItems;
	}

	public Learner getCurrentLearner() {
		return currentLearner;
	}

	public int getPageSize() {
		return 20;
	}

	public int getActivePage() {
		return activePage;
	}

	public void setActivePage(int page) throws SearchLibException {
		synchronized (this) {
			activePage = page;
			reload();
		}
	}

	public int getTotalSize() {
		return totalSize;
	}

	/**
	 * @return the selectedLearnerField
	 */
	public String getSelectedSourceLearnerField() {
		return selectedSourceLearnerField;
	}

	/**
	 * @param selectedLearnerField
	 *            the selectedLearnerField to set
	 */
	public void setSelectedSourceLearnerField(String selectedLearnerField) {
		this.selectedSourceLearnerField = selectedLearnerField;
	}

	/**
	 * @return the selectedLearnerField
	 */
	public String getSelectedTargetLearnerField() {
		return selectedTargetLearnerField;
	}

	/**
	 * @param selectedLearnerField
	 *            the selectedLearnerField to set
	 */
	public void setSelectedTargetLearnerField(String selectedLearnerField) {
		this.selectedTargetLearnerField = selectedLearnerField;
	}

	@Command
	@NotifyChange("*")
	public void onReset(@BindingParam("learner") String learnerName) throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getLearnerManager().reset(learnerName);
	}

	@Command
	@NotifyChange("*")
	public void onLearn(@BindingParam("learner") String learnerName) throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		TaskLearnerRun taskLearnerRun = new TaskLearnerRun();
		taskLearnerRun.setManualLearn(learnerName);
		TaskItem taskItem = new TaskItem(client, taskLearnerRun);
		TaskManager.executeTask(client, taskItem, null);
	}

	@Command
	@NotifyChange("*")
	public void onEdit(@BindingParam("learner") String learnerName) throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		selectedLearner = client.getLearnerManager().get(learnerName);
		if (selectedLearner == null)
			throw new SearchLibException("Learner not found: " + learnerName);
		if (selectedLearner.isRunning())
			throw new SearchLibException("Learning is running");
		currentLearner = new Learner(selectedLearner);
	}

	@Command
	@NotifyChange("*")
	public void onSourceLinkAdd() throws SearchLibException, TransformerConfigurationException, SAXException,
			IOException, XPathExpressionException, ParserConfigurationException {
		if (selectedSourceLearnerField == null || selectedSourceIndexField == null || currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getSourceFieldMap();
		fieldMap.add(new SourceField(selectedSourceIndexField),
				new TargetField(selectedSourceLearnerField, null, selectedSourceLearnerFieldBoost, null));
	}

	@Command
	@NotifyChange("*")
	public void onSourceLinkRemove(@BindingParam("link") GenericLink<SourceField, TargetField> link)
			throws SearchLibException, TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getSourceFieldMap();
		fieldMap.remove(link);
	}

	@Command
	@NotifyChange({ "learners", "running" })
	public void onRefreshList() {
	}

	/**
	 * @return the testText
	 */
	public String getTestText() {
		return testText;
	}

	/**
	 * @param testText
	 *            the testText to set
	 */
	public void setTestText(String testText) {
		this.testText = testText;
	}

	public boolean isRunning() throws SearchLibException {
		Learner[] learners = getLearners();
		if (learners == null)
			return false;
		for (Learner learner : learners)
			if (learner.isRunning())
				return true;
		return false;
	}

	/**
	 * @return the selectedSourceLearnerFieldBoost
	 */
	public Float getSelectedSourceLearnerFieldBoost() {
		return selectedSourceLearnerFieldBoost;
	}

	/**
	 * @param selectedSourceLearnerFieldBoost
	 *            the selectedSourceLearnerFieldBoost to set
	 */
	public void setSelectedSourceLearnerFieldBoost(Float selectedSourceLearnerFieldBoost) {
		this.selectedSourceLearnerFieldBoost = selectedSourceLearnerFieldBoost;
	}

}
