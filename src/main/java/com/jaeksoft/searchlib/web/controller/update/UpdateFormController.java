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

package com.jaeksoft.searchlib.web.controller.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

@AfterCompose(superclass = true)
public class UpdateFormController extends CommonController {

	private transient IndexDocument indexDocument;

	private transient List<FieldDocument> fieldDocumentList;

	private transient SchemaField selectedField;

	public UpdateFormController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		indexDocument = null;
		fieldDocumentList = null;
		selectedField = null;
	}

	public List<SchemaField> getFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
			if (list.size() > 0 && selectedField == null)
				selectedField = list.get(0);
			return list;
		}
	}

	public void setSelectedField(SchemaField field) {
		synchronized (this) {
			selectedField = field;
		}
	}

	public SchemaField getSelectedField() {
		synchronized (this) {
			return selectedField;
		}
	}

	@Command
	public void onAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedField == null)
				return;
			IndexDocument idxDoc = getIndexDocument();
			idxDoc.add(selectedField.getName(), new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, ""));
			reload();
		}
	}

	public IndexDocument getIndexDocument() {
		synchronized (this) {
			if (indexDocument != null)
				return indexDocument;
			indexDocument = (IndexDocument) getAttribute(ScopeAttribute.UPDATE_FORM_INDEX_DOCUMENT);
			if (indexDocument == null) {
				indexDocument = new IndexDocument();
				setAttribute(ScopeAttribute.UPDATE_FORM_INDEX_DOCUMENT,
						indexDocument);
			}
			return indexDocument;
		}
	}

	public List<FieldDocument> getFieldDocumentList() {
		synchronized (this) {
			if (fieldDocumentList != null)
				return fieldDocumentList;
			fieldDocumentList = new ArrayList<FieldDocument>();
			for (FieldContent fieldContent : getIndexDocument()) {
				if (fieldContent.getValues().length > 0)
					fieldDocumentList.add(new FieldDocument(fieldContent));
			}
			return fieldDocumentList;
		}
	}

	public LanguageEnum getLang() {
		return getIndexDocument().getLang();
	}

	public void setLang(LanguageEnum lang) {
		getIndexDocument().setLang(lang);
	}

	public class FieldDocument {

		private List<FieldValue> fieldValueList;

		private String fieldName;

		private FieldDocument(FieldContent fieldContent) {
			fieldName = fieldContent.getField();
			fieldValueList = new ArrayList<FieldValue>(
					fieldContent.getValues().length);
			int i = 0;
			for (@SuppressWarnings("unused")
			FieldValueItem valueItem : fieldContent.getValues())
				fieldValueList.add(new FieldValue(i++, fieldContent));
		}

		public List<FieldValue> getFieldValueList() {
			return fieldValueList;
		}

		public String getField() {
			return fieldName;
		}
	}

	public static class FieldValue {

		private int index;

		private FieldContent fieldContent;

		private FieldValue(int index, FieldContent fieldContent) {
			this.index = index;
			this.fieldContent = fieldContent;
		}

		public String getValue() {
			return fieldContent.getValue(index).getValue();
		}

		public void setValue(String value) {
			fieldContent.setValue(index, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, value, getBoost()));
		}

		public float getBoost() {
			Float b = fieldContent.getValue(index).getBoost();
			return b == null ? 1.0f : b;
		}

		public void setBoost(Double boost) {
			Float b = boost == null ? null : boost.floatValue();
			fieldContent.setValue(index, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, getValue(), b));
		}

		public void remove() {
			fieldContent.remove(index);
		}
	}

	@Command
	public void onValueRemove(@BindingParam("fieldValue") FieldValue fieldValue)
			throws SearchLibException {
		fieldValue.remove();
		reload();
	}

	@Command
	public void onUpdate() throws NoSuchAlgorithmException, IOException,
			URISyntaxException, SearchLibException, InterruptedException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		getClient().updateDocument(getIndexDocument());
		new AlertController("Document updated");
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			fieldDocumentList = null;
			super.reload();
		}
	}

}
