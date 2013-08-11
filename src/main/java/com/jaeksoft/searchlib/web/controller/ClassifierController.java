/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.classifier.ClassificationMethodEnum;
import com.jaeksoft.searchlib.classifier.Classifier;
import com.jaeksoft.searchlib.classifier.ClassifierItem;
import com.jaeksoft.searchlib.classifier.ClassifierManager;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.schema.SchemaField;

public class ClassifierController extends CommonController {

	private Classifier selectedClassifier;
	private Classifier currentClassifier;
	private ClassifierItem selectedClassifierItem;
	private ClassifierItem currentClassifierItem;
	private LanguageEnum lang;

	private transient int totalSize;
	private transient int activePage;

	private List<ClassifierItem> classifierItemList;

	public ClassifierController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentClassifier = null;
		selectedClassifier = null;
		currentClassifierItem = new ClassifierItem();
		selectedClassifierItem = null;
		classifierItemList = null;
		totalSize = 0;
		activePage = 0;
		lang = LanguageEnum.UNDEFINED;
	}

	public Classifier[] getClassifiers() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getClassifierManager().getArray();
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

	public String getCurrentEditMode() throws SearchLibException {
		return selectedClassifier == null ? "Create a new classifier"
				: "Edit the selected classifier";
	}

	public List<String> getFieldList() throws SearchLibException {
		List<String> fields = new ArrayList<String>(0);
		Client client = getClient();
		if (client == null)
			return fields;
		fields.add(null);
		for (SchemaField field : client.getSchema().getFieldList())
			fields.add(field.getName());
		return fields;
	}

	public ClassificationMethodEnum[] getMethodList() {
		return ClassificationMethodEnum.values();
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	public boolean isEditing() {
		return currentClassifier != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public boolean isSelected() {
		return selectedClassifier != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Command
	@NotifyChange("*")
	public void onNewClassifier() throws SearchLibException {
		currentClassifier = new Classifier();
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		currentClassifier = null;
		selectedClassifier = null;
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return;
		ClassifierManager cm = client.getClassifierManager();
		if (selectedClassifier != null) {
			cm.replace(selectedClassifier, currentClassifier);
		} else
			cm.add(currentClassifier);
		client.saveClassifier(currentClassifier);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onDelete() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getClassifierManager().remove(selectedClassifier);
		client.deleteClassifier(selectedClassifier);
		onCancel();
	}

	public Classifier getCurrentClassifier() {
		return currentClassifier;
	}

	public Classifier getSelectedClassifier() {
		return selectedClassifier;
	}

	public void setSelectedClassifier(Classifier classifier)
			throws SearchLibException {
		if (classifier == null)
			return;
		selectedClassifier = classifier;
		currentClassifier = new Classifier(classifier);
		reload();
	}

	public boolean isItemSelected() {
		return selectedClassifierItem != null;
	}

	public boolean isNotItemSelected() {
		return !isItemSelected();
	}

	public ClassifierItem getCurrentClassifierItem() {
		return currentClassifierItem;
	}

	public ClassifierItem getSelectedClassifierItem() {
		return selectedClassifierItem;
	}

	@NotifyChange({ "currentClassifier", "currentClassifierItem",
			"selectedClassifierItem", "notItemSelected", "itemSelected" })
	public void setSelectedClassifierItem(ClassifierItem classifierItem)
			throws SearchLibException {
		if (classifierItem == null)
			return;
		selectedClassifierItem = classifierItem;
		currentClassifierItem = new ClassifierItem(classifierItem);
	}

	@Command
	@NotifyChange({ "currentClassifier", "currentClassifierItem",
			"classifierItemList", "selectedClassifierItem", "notItemSelected",
			"itemSelected" })
	public void onSaveClassifierItem() throws SearchLibException {
		if (selectedClassifierItem != null)
			currentClassifier.replace(selectedClassifierItem,
					currentClassifierItem);
		else
			currentClassifier.add(currentClassifierItem);
		computeClassifierItemList();
		onCancelClassifierItem();
	}

	@Command
	@NotifyChange({ "currentClassifier", "currentClassifierItem",
			"classifierItemList", "selectedClassifierItem", "notItemSelected",
			"itemSelected" })
	public void onCancelClassifierItem() throws SearchLibException {
		currentClassifierItem = new ClassifierItem();
		selectedClassifierItem = null;
	}

	@Command
	@NotifyChange({ "currentClassifier", "currentClassifierItem",
			"classifierItemList", "selectedClassifierItem", "notItemSelected",
			"itemSelected" })
	public void onRemoveClassifierItem(
			@BindingParam("classifierItem") ClassifierItem classifierItem)
			throws SearchLibException {
		currentClassifier.remove(classifierItem);
		computeClassifierItemList();
		onCancelClassifierItem();
	}

	@Command
	public void onTestClassifierItem() throws SearchLibException,
			InterruptedException {
		int n = currentClassifierItem.query(getClient(), lang);
		new AlertController(n + " document(s) found.");
	}

	private void computeClassifierItemList() {
		synchronized (this) {
			classifierItemList = null;
			totalSize = 0;
			if (currentClassifier == null)
				return;
			classifierItemList = new ArrayList<ClassifierItem>(0);
			ClassifierItem[] classifierItemArray = currentClassifier
					.getValueSet();
			if (classifierItemArray == null)
				return;
			totalSize = classifierItemArray.length;
			int start = getPageSize() * getActivePage();
			int end = start + getPageSize();
			if (end > totalSize)
				end = totalSize;
			for (int i = start; i < end; i++)
				classifierItemList.add(classifierItemArray[i]);
		}
	}

	public List<ClassifierItem> getClassifierItemList() {
		if (classifierItemList == null)
			computeClassifierItemList();
		return classifierItemList;
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
			computeClassifierItemList();
			reload();
		}
	}

	public int getTotalSize() {
		return totalSize;
	}

}
