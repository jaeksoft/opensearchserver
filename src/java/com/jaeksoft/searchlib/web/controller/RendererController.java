/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererManager;
import com.jaeksoft.searchlib.schema.SchemaField;

public class RendererController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 481885249271682931L;

	private Renderer selectedRenderer;
	private Renderer currentRenderer;

	public RendererController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentRenderer = null;
		selectedRenderer = null;
	}

	public Renderer[] getRenderers() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getRendererManager().getArray();
	}

	public Set<String> getRequestList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSearchRequestMap().getNameList();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedRenderer == null ? "Create a new renderer"
				: "Edit the selected renderer";
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

	public boolean isEditing() {
		return currentRenderer != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public boolean isSelected() {
		return selectedRenderer != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public void onNew() {
		currentRenderer = new Renderer();
		reloadPage();
	}

	public void onCancel() {
		currentRenderer = null;
		selectedRenderer = null;
		reloadPage();
	}

	public void onSave() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return;
		RendererManager manager = client.getRendererManager();
		if (selectedRenderer != null) {
			manager.replace(selectedRenderer, currentRenderer);
		} else
			manager.add(currentRenderer);
		client.save(currentRenderer);
		onCancel();
	}

	public void onDelete() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getRendererManager().remove(selectedRenderer);
		client.delete(selectedRenderer);
		onCancel();
	}

	public Renderer getCurrentRenderer() {
		return currentRenderer;
	}

	public Renderer getSelectedClassifier() {
		return selectedRenderer;
	}

	public void setSelectedRenderer(Renderer renderer) {
		if (renderer == null)
			return;
		selectedRenderer = renderer;
		currentRenderer = new Renderer(renderer);
		reloadPage();
	}

}
