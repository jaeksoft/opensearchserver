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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Messagebox;

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
	private boolean isTestable;

	private class DeleteAlert extends AlertController {

		private Renderer deleteRenderer;

		protected DeleteAlert(Renderer deleteRenderer)
				throws InterruptedException {
			super("Please, confirm that you want to delete the renderer: "
					+ deleteRenderer.getName(), Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.deleteRenderer = deleteRenderer;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getRendererManager().remove(deleteRenderer);
			client.delete(deleteRenderer);
			onCancel();
		}
	}

	public RendererController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentRenderer = null;
		selectedRenderer = null;
		isTestable = false;
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

	private Renderer getRenderer(Component comp) {
		return (Renderer) getRecursiveComponentAttribute(comp, "rendererItem");
	}

	public void doEdit(Component comp) {
		Renderer renderer = getRenderer(comp);
		if (renderer == null)
			return;
		selectedRenderer = renderer;
		currentRenderer = new Renderer(renderer);
		reloadPage();
	}

	public void doDelete(Component comp) throws InterruptedException {
		Renderer renderer = getRenderer(comp);
		if (renderer == null)
			return;
		new DeleteAlert(renderer);
	}

	public void onNew() {
		currentRenderer = new Renderer();
		reloadPage();
	}

	public void onCancel() {
		currentRenderer = null;
		selectedRenderer = null;
		isTestable = false;
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

	public void onTest(Component comp) throws UnsupportedEncodingException {
		isTestable = true;
		Iframe iframe = (Iframe) comp.getFellow("iframetest", true);
		iframe.setSrc(null);
		if (currentRenderer != null)
			iframe.setSrc(currentRenderer.getApiUrl());
		reloadPage();
	}

	public boolean isTestable() {
		return isTestable;
	}

	public Renderer getCurrentRenderer() {
		return currentRenderer;
	}

	public Renderer getSelectedClassifier() {
		return selectedRenderer;
	}

	public void setSelectedRenderer(Renderer renderer) {
	}

	public Integer getIframeWidth() {
		return (Integer) getAttribute(ScopeAttribute.RENDERER_IFRAME_WIDTH,
				new Integer(700));
	}

	public Integer getIframeHeight() {
		return (Integer) getAttribute(ScopeAttribute.RENDERER_IFRAME_HEIGHT,
				new Integer(400));
	}

	public String getIframeWidthPx() {
		return getIframeWidth() + "px";
	}

	public String getIframeHeightPx() {
		return getIframeHeight() + "px";
	}

	public void setIframeWidth(Integer width) {
		setAttribute(ScopeAttribute.RENDERER_IFRAME_WIDTH, width);
	}

	public void setIframeHeight(Integer height) {
		setAttribute(ScopeAttribute.RENDERER_IFRAME_HEIGHT, height);
	}
}
