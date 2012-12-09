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
import com.jaeksoft.searchlib.renderer.RendererField;
import com.jaeksoft.searchlib.renderer.RendererFieldType;
import com.jaeksoft.searchlib.renderer.RendererManager;
import com.jaeksoft.searchlib.request.SearchRequest;

public class RendererController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 481885249271682931L;

	private transient Renderer selectedRenderer;
	private transient Renderer currentRenderer;
	private transient boolean isTestable;
	private transient RendererField currentRendererField;
	private transient RendererField selectedRendererField;

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
		currentRendererField = null;
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
		return client.getRequestMap().getNameList();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedRenderer == null ? "Create a new renderer"
				: "Edit the selected renderer";
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

	public boolean isFieldSelected() {
		return selectedRendererField != null;
	}

	public boolean isNotFieldSelected() {
		return !isFieldSelected();
	}

	private Renderer getRenderer(Component comp) {
		return (Renderer) getRecursiveComponentAttribute(comp, "rendererItem");
	}

	public void doEdit(Component comp) throws SearchLibException {
		Renderer renderer = getRenderer(comp);
		if (renderer == null)
			return;
		selectedRenderer = renderer;
		currentRenderer = new Renderer(renderer);
		currentRendererField = new RendererField();
		reloadPage();
	}

	public void doDelete(Component comp) throws InterruptedException {
		Renderer renderer = getRenderer(comp);
		if (renderer == null)
			return;
		new DeleteAlert(renderer);
	}

	public void onNew() throws SearchLibException {
		currentRenderer = new Renderer();
		currentRendererField = new RendererField();
		reloadPage();
	}

	public void onRendererFieldSave() throws SearchLibException {
		if (selectedRendererField == null)
			currentRenderer.addField(currentRendererField);
		else
			currentRendererField.copyTo(selectedRendererField);
		onRendererFieldCancel();
		reloadPage();
	}

	private RendererField getRendererField(Component comp) {
		return (RendererField) getRecursiveComponentAttribute(comp,
				"rendererFieldItem");
	}

	public void onRendererFieldRemove(Component comp) throws SearchLibException {
		currentRenderer.removeField(getRendererField(comp));
		reloadPage();
	}

	public void onRendererFieldUp(Component comp) throws SearchLibException {
		currentRenderer.fieldUp(getRendererField(comp));
		reloadPage();
	}

	public void onRendererFieldDown(Component comp) throws SearchLibException {
		currentRenderer.fieldDown(getRendererField(comp));
		reloadPage();
	}

	public void onRendererFieldCancel() throws SearchLibException {
		currentRendererField = new RendererField();
		selectedRendererField = null;
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		currentRenderer = null;
		selectedRenderer = null;
		isTestable = false;
		reloadPage();
	}

	public void onCssDefault() throws SearchLibException {
		currentRenderer.setDefaultCss();
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
	}

	public void onSaveAndClose() throws UnsupportedEncodingException,
			SearchLibException {
		onSave();
		onCancel();
	}

	public void onTest(Component comp) throws UnsupportedEncodingException,
			SearchLibException {
		isTestable = true;
		Iframe iframe = (Iframe) comp.getFellow("iframetest", true);
		iframe.setSrc(null);
		if (currentRenderer != null)
			iframe.setSrc(currentRenderer.getApiUrl());
		reloadPage();
	}

	public String getIFrameHtmlCode() throws UnsupportedEncodingException,
			InterruptedException {
		if (currentRenderer == null)
			return null;
		return currentRenderer.getIFrameHtmlCode(getIframeWidthPx(),
				getIframeHeightPx());
	}

	public boolean isTestable() {
		return isTestable;
	}

	public Renderer getCurrentRenderer() {
		return currentRenderer;
	}

	public RendererField getCurrentRendererField() {
		return currentRendererField;
	}

	public RendererField getSelectedRendererField() {
		return selectedRendererField;
	}

	public void setSelectedRendererField(RendererField field)
			throws SearchLibException {
		selectedRendererField = field;
		currentRendererField = new RendererField(field);
		reloadPage();
	}

	public Renderer getSelectedClassifier() {
		return selectedRenderer;
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

	public RendererFieldType[] getFieldTypeList() {
		return RendererFieldType.values();
	}

	public List<String> getFieldList() throws SearchLibException {
		if (currentRendererField == null || currentRenderer == null)
			return null;
		Client client = getClient();
		if (client == null)
			return null;
		SearchRequest request = (SearchRequest) client.getRequestMap().get(
				currentRenderer.getRequestName());
		if (request == null)
			return null;
		List<String> nameList = new ArrayList<String>();
		nameList.add(null);
		request.getReturnFieldList().toNameList(nameList);
		return nameList;
	}

	public List<String> getFieldOrSnippetList() throws SearchLibException {
		if (currentRendererField == null || currentRenderer == null)
			return null;
		Client client = getClient();
		if (client == null)
			return null;
		SearchRequest request = (SearchRequest) client.getRequestMap().get(
				currentRenderer.getRequestName());
		if (request == null)
			return null;
		List<String> nameList = new ArrayList<String>();
		nameList.add(null);
		if (currentRendererField.getFieldType() == RendererFieldType.FIELD)
			request.getReturnFieldList().toNameList(nameList);
		else if (currentRendererField.getFieldType() == RendererFieldType.SNIPPET)
			request.getSnippetFieldList().toNameList(nameList);
		return nameList;
	}
}
