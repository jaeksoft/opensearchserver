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
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class LearningController extends CommonController implements
		InfoCallback {

	private Learner selectedLearner;
	private Learner currentLearner;

	private String info;

	private transient String selectedSourceIndexField;
	private transient String selectedSourceLearnerField;
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
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
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
		return selectedLearner == null ? "Create a new learner"
				: "Edit the selected learner";
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
		client.getRequestMap().getNameList(requestList,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
		return requestList;
	}

	@Command
	@NotifyChange("*")
	public void onNewLearner() throws SearchLibException {
		currentLearner = new Learner();
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		currentLearner = null;
		selectedLearner = null;
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return;
		LearnerManager lm = client.getLearnerManager();
		if (selectedLearner != null) {
			lm.replace(selectedLearner, currentLearner);
		} else
			lm.add(currentLearner);
		client.saveLearner(currentLearner);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onDelete() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getLearnerManager().remove(selectedLearner);
		client.deleteLearner(selectedLearner);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onCheck() throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		if (currentLearner == null)
			return;
		currentLearner.checkInstance(client);
		new AlertController("Learner successfully checked");
	}

	@Command
	@NotifyChange("*")
	public void onClassify() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		if (currentLearner == null)
			return;
		learnerResultItems = currentLearner.classify(client, testText, null,
				null);
	}

	public LearnerResultItem[] getLearnerResultItems() {
		return learnerResultItems;
	}

	public Learner getCurrentLearner() {
		return currentLearner;
	}

	public Learner getSelectedLearner() {
		return selectedLearner;
	}

	public void setSelectedLearner(Learner learner) throws SearchLibException {
		if (learner == null)
			return;
		selectedLearner = learner;
		currentLearner = new Learner(learner);
		reload();
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
	public void onReset() throws SearchLibException, IOException {
		if (currentLearner == null)
			return;
		Client client = getClient();
		if (client == null)
			return;
		currentLearner.reset(client);
	}

	@Command
	@NotifyChange("*")
	public void onLearn() throws SearchLibException {
		if (currentLearner == null)
			return;
		Client client = getClient();
		if (client == null)
			return;
		currentLearner.learn(client, this);
	}

	@Command
	@NotifyChange("*")
	public void onSourceLinkAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (selectedSourceLearnerField == null
				|| selectedSourceIndexField == null || currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getSourceFieldMap();
		fieldMap.add(new SourceField(selectedSourceIndexField),
				new TargetField(selectedSourceLearnerField));
	}

	@Command
	@NotifyChange("*")
	public void onSourceLinkRemove(
			@BindingParam("link") GenericLink<SourceField, TargetField> link)
			throws SearchLibException, TransformerConfigurationException,
			SAXException, IOException, XPathExpressionException,
			ParserConfigurationException {
		if (currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getSourceFieldMap();
		fieldMap.remove(link);
	}

	@Command
	@NotifyChange("*")
	public void onTargetLinkAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (selectedTargetLearnerField == null
				|| selectedTargetIndexField == null || currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getTargetFieldMap();
		fieldMap.add(new SourceField(selectedTargetLearnerField),
				new TargetField(selectedTargetIndexField.getName()));
	}

	@Command
	@NotifyChange("*")
	public void onTargetLinkRemove(
			@BindingParam("link") GenericLink<SourceField, TargetField> link)
			throws SearchLibException, TransformerConfigurationException,
			SAXException, IOException, XPathExpressionException,
			ParserConfigurationException {
		if (currentLearner == null)
			return;
		FieldMap fieldMap = currentLearner.getTargetFieldMap();
		fieldMap.remove(link);
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

	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

}
