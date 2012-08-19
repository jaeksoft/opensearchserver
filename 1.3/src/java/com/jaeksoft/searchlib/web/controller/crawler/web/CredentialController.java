/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class CredentialController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3206340453522545180L;

	private transient List<CredentialItem> credentialList;

	private transient String like;

	private transient String pattern;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	private transient CredentialItem selectedCredential;

	private transient CredentialItem currentCredential;

	private class DeleteAlert extends AlertController {

		private transient CredentialItem deleteCredential;

		protected DeleteAlert(CredentialItem deleteCredential)
				throws InterruptedException {
			super("Please, confirm that you want to delete the credential: "
					+ deleteCredential.getPattern(), Messagebox.YES
					| Messagebox.NO, Messagebox.QUESTION);
			this.deleteCredential = deleteCredential;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			CredentialManager credentialManager = client
					.getWebCredentialManager();
			credentialManager.delCredential(deleteCredential.getPattern());
			onCancel();
		}
	}

	public CredentialController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		credentialList = null;
		pattern = null;
		like = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selectedCredential = null;
		currentCredential = new CredentialItem();
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) throws SearchLibException {
				onPaging((PagingEvent) event);
			}
		});
	}

	public void setPageSize(int v) {
		pageSize = v;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setLike(String v) {
		if (v == like)
			return;
		like = v;
	}

	public String getLike() {
		return like;
	}

	public List<CredentialItem> getCredentialList() {
		synchronized (this) {
			if (credentialList != null)
				return credentialList;
			try {
				Client client = getClient();
				if (client == null)
					return null;
				CredentialManager credentialManager = client
						.getWebCredentialManager();
				credentialList = new ArrayList<CredentialItem>();
				totalSize = credentialManager.getCredentials(like,
						getActivePage() * getPageSize(), getPageSize(),
						credentialList);
				return credentialList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void onPaging(PagingEvent pagingEvent) throws SearchLibException {
		synchronized (this) {
			credentialList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() throws SearchLibException {
		synchronized (this) {
			credentialList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	public String getPattern() {
		synchronized (this) {
			return pattern;
		}
	}

	public void setPattern(String v) {
		synchronized (this) {
			pattern = v;
		}
	}

	/**
	 * 
	 * @return the current CredentialItem
	 */
	public CredentialItem getCurrentJob() {
		return currentCredential;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedCredential == null ? "Create a new credential"
				: "Edit the selected credential";
	}

	/**
	 * 
	 * @return the current credential item
	 */
	public CredentialItem getCurrentCredential() {
		return currentCredential;
	}

	/**
	 * 
	 * @return the selected credential item
	 */
	public CredentialItem getSelectedCredential() {
		return selectedCredential;
	}

	/**
	 * Set the selected credential
	 * 
	 * @param credential
	 * @throws SearchLibException
	 */
	public void setSelectedCredential(CredentialItem credential)
			throws SearchLibException {
		if (credential == null)
			return;
		selectedCredential = credential;
		selectedCredential.copy(currentCredential);
		reloadPage();
	}

	public boolean selected() {
		return selectedCredential != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	@Override
	public void reloadPage() throws SearchLibException {
		synchronized (this) {
			credentialList = null;
			super.reloadPage();
		}
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		if (selectedCredential == null)
			return;
		if (!isWebCrawlerEditPatternsRights())
			throw new SearchLibException("Not allowed");
		new DeleteAlert(selectedCredential);
		onCancel();
	}

	public void onSave() throws InterruptedException, SearchLibException,
			MalformedURLException {
		Client client = getClient();
		if (client == null)
			return;
		CredentialManager credentialManager = client.getWebCredentialManager();
		if (selectedCredential == null) {
			if (credentialManager.getCredential(currentCredential.getPattern()) != null) {
				new AlertController("The pattern already exists");
				return;
			}
			credentialManager.addCredential(currentCredential);
		} else
			selectedCredential.copy(currentCredential);
		client.saveJobs();
		onCancel();
	}
}