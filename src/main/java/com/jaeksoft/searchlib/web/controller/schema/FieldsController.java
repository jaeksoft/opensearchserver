/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class FieldsController extends CommonController {

	private transient SchemaField field;

	private transient SchemaField selectedField;

	private transient String selectedCopyOf;

	public FieldsController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		field = new SchemaField();
		selectedField = null;
		selectedCopyOf = null;
	}

	public SchemaField getField() {
		return field;
	}

	public SchemaField getSelectedField() {
		return selectedField;
	}

	@Command
	public void onCancel() throws SearchLibException {
		field = new SchemaField();
		selectedField = null;
		selectedCopyOf = null;
		reload();
	}

	@Command
	public void onDelete() throws SearchLibException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		Client client = getClient();
		Schema schema = client.getSchema();
		schema.getFieldList().remove(selectedField.getName());
		field = new SchemaField();
		selectedField = null;
		client.saveConfig();
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		try {
			field.valid();
		} catch (SearchLibException e) {
			new AlertController(e.getMessage(), Messagebox.OK, Messagebox.ERROR);
			return;
		}
		Client client = getClient();
		Schema schema = client.getSchema();
		if (selectedField != null)
			selectedField.copyFrom(field);
		else
			schema.getFieldList().put(field);
		field = new SchemaField();
		selectedField = null;
		client.saveConfig();
	}

	@NotifyChange("*")
	public void setSelectedField(SchemaField selectedField) {
		this.selectedField = selectedField;
		field.copyFrom(selectedField);
	}

	public boolean isSelected() {
		return getSelectedField() != null;
	}

	public boolean isNotSelected() {
		return getSelectedField() == null;
	}

	public Stored[] getStoredList() {
		return Stored.values();
	}

	public Indexed[] getIndexedList() {
		return Indexed.values();
	}

	public TermVector[] getTermVectorList() {
		return TermVector.values();
	}

	public List<String> getCopyOfFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<String> fieldList = new ArrayList<String>();
			client.getSchema().getFieldList().toNameList(fieldList);
			return fieldList;
		}
	}

	@Command
	@NotifyChange("*")
	public void onCopyOfAdd() {
		if (selectedCopyOf == null || selectedCopyOf.length() == 0)
			return;
		this.field.addCopyOf(selectedCopyOf);
	}

	@Command
	@NotifyChange("*")
	public void onCopyOfRemove(@BindingParam("field") String field) {
		this.field.removeCopyOf(field);
	}

	public List<String> getOtherSchemaFields() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> nameList = new ArrayList<String>(1);
		nameList.add("");
		client.getSchema().getFieldList().toNameList(nameList);
		String fname = field != null ? field.getName() : null;
		if (fname != null && fname.length() > 0)
			nameList.remove(fname);
		return nameList;
	}

	public List<SchemaField> getList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getSchema().getFieldList().getList();
		}
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedField == null ? "Create a new field" : "Edit the field "
				+ selectedField.getName();
	}

	public List<String> getIndexedFields() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<String> indexedFields = new ArrayList<String>(0);
			indexedFields.add(null);
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkIndexed(Indexed.YES))
					indexedFields.add(field.getName());
			return indexedFields;
		}
	}

	public String getSelectedUnique() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		SchemaField field = client.getSchema().getFieldList().getUniqueField();
		if (field == null)
			return null;
		return field.getName();
	}

	public void setSelectedUnique(String field) throws SearchLibException,
			InterruptedException {
		Client client = getClient();
		Schema schema = client.getSchema();
		schema.getFieldList().setUniqueField(field);
		client.saveConfig();
	}

	public String getSelectedDefault() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		SchemaField field = client.getSchema().getFieldList().getDefaultField();
		if (field == null)
			return null;
		return field.getName();
	}

	public void setSelectedDefault(String field) throws SearchLibException,
			InterruptedException {
		Client client = getClient();
		Schema schema = client.getSchema();
		schema.getFieldList().setDefaultField(field);
		client.saveConfig();
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}

	/**
	 * @return the selectedCopyOf
	 */
	public String getSelectedCopyOf() {
		return selectedCopyOf;
	}

	/**
	 * @param selectedCopyOf
	 *            the selectedCopyOf to set
	 */
	public void setSelectedCopyOf(String selectedCopyOf) {
		this.selectedCopyOf = selectedCopyOf;
	}

}
