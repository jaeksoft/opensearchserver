/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UpdateFormController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1542486102205983915L;

	private IndexDocument indexDocument;

	private List<FieldDocument> fieldDocumentList;

	private SchemaField selectedField;

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

	public void onAdd() {
		synchronized (this) {
			if (selectedField == null)
				return;
			IndexDocument idxDoc = getIndexDocument();
			idxDoc.addEmptyField(selectedField.getName());
			reloadPage();
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
			for (FieldContent fieldContent : getIndexDocument())
				fieldDocumentList.add(new FieldDocument(fieldContent));
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

			fieldValueList = new ArrayList<FieldValue>(fieldContent.getValues()
					.size());
			int i = 0;
			for (@SuppressWarnings("unused")
			String value : fieldContent.getValues())
				fieldValueList.add(new FieldValue(i++, fieldContent));
		}

		public List<FieldValue> getFieldValueList() {
			return fieldValueList;
		}

		public String getField() {
			return fieldName;
		}
	}

	public class FieldValue implements EventListener {

		private int index;

		private FieldContent fieldContent;

		private FieldValue(int index, FieldContent fieldContent) {
			this.index = index;
			this.fieldContent = fieldContent;
		}

		public String getValue() {
			return fieldContent.getValue(index);
		}

		public void setValue(String value) {
			fieldContent.setValue(index, value);
		}

		public void remove() {
			fieldContent.remove(index);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Textbox textbox = (Textbox) event.getTarget();
			setValue(textbox.getValue());
		}

	}

	@Override
	public void render(Listitem item, Object data) throws Exception {
		FieldValue fieldValue = (FieldValue) data;
		Listcell listcell = new Listcell();
		Textbox textbox = new Textbox();
		textbox.setWidth("100%");
		textbox.setRows(3);
		textbox.setValue(fieldValue.getValue());
		textbox.addEventListener("onChange", fieldValue);
		textbox.setParent(listcell);
		listcell.setParent(item);

		listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onValueRemove", fieldValue);
		image.setParent(listcell);
		listcell.setParent(item);
	}

	public void onValueRemove(Event event) {
		FieldValue fieldValue = (FieldValue) event.getData();
		fieldValue.remove();
		reloadPage();
	}

	public void onUpdate() throws NoSuchAlgorithmException, IOException,
			URISyntaxException, SearchLibException, InterruptedException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!isUpdateRights())
			throw new SearchLibException("Not allowed");
		getClient().updateDocument(getIndexDocument());
		new AlertController("Document updated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jaeksoft.searchlib.web.controller.CommonController#reloadPage()
	 */
	@Override
	public void reloadPage() {
		synchronized (this) {
			fieldDocumentList = null;
			super.reloadPage();
		}
	}

}
