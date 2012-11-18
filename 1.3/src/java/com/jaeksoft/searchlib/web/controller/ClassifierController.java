/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Component;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = -245936909199297182L;

	private Classifier selectedClassifier;
	private Classifier currentClassifier;
	private ClassifierItem selectedClassifierItem;
	private ClassifierItem currentClassifierItem;
	private LanguageEnum lang;

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
		lang = LanguageEnum.UNDEFINED;
	}

	public Classifier[] getClassifiers() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getClassifierManager().getArray();
	}

	public List<String> getRequestList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getRequestMap()
				.getNameList(RequestTypeEnum.SearchRequest);
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedClassifier == null ? "Create a new classifier"
				: "Edit the selected classifier";
	}

	public List<String> getFieldList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> fields = new ArrayList<String>();
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

	public void onNewClassifier() throws SearchLibException {
		currentClassifier = new Classifier();
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		currentClassifier = null;
		selectedClassifier = null;
		reloadPage();
	}

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
		reloadPage();
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

	public void setSelectedClassifierItem(ClassifierItem classifierItem)
			throws SearchLibException {
		if (classifierItem == null)
			return;
		selectedClassifierItem = classifierItem;
		currentClassifierItem = new ClassifierItem(classifierItem);
		reloadPage();
	}

	public void onSaveClassifierItem() throws SearchLibException {
		if (selectedClassifierItem != null)
			currentClassifier.replace(selectedClassifierItem,
					currentClassifierItem);
		else
			currentClassifier.add(currentClassifierItem);
		onCancelClassifierItem();
	}

	public void onCancelClassifierItem() throws SearchLibException {
		currentClassifierItem = new ClassifierItem();
		selectedClassifierItem = null;
		reloadPage();
	}

	private ClassifierItem getClassifierItem(Component component) {
		return (ClassifierItem) component.getParent().getAttribute(
				"classifierItem");
	}

	public void onRemoveClassifierItem(Component component)
			throws SearchLibException {
		ClassifierItem cf = getClassifierItem(component);
		currentClassifier.remove(cf);
		onCancelClassifierItem();
	}

	public void onTestClassifierItem() throws SearchLibException,
			InterruptedException {
		int n = currentClassifierItem.query(getClient(), lang);
		new AlertController(n + " document(s) found.");
	}

}
