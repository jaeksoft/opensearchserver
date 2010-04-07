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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public abstract class CommonController extends Window implements AfterCompose,
		EventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3581269068713587866L;

	private boolean isComposed;

	private AnnotateDataBinder binder = null;

	public CommonController() throws SearchLibException {
		super();
		isComposed = false;
		reset();
	}

	public void afterCompose() {
		binder = new AnnotateDataBinder(this);
		binder.loadAll();
		PushEvent.FLUSH_PRIVILEGES.subscribe(this);
		isComposed = true;
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute) {
		return scopeAttribute.get(this);
	}

	protected void setAttribute(ScopeAttribute scopeAttribute, Object value) {
		if (isComposed)
			scopeAttribute.set(this, value);
	}

	public Client getClient() throws SearchLibException {
		return (Client) getAttribute(ScopeAttribute.CURRENT_CLIENT);
	}

	public void setClient(Client client) {
		setAttribute(ScopeAttribute.CURRENT_CLIENT, client);
	}

	public boolean isInstanceValid() throws SearchLibException {
		return getClient() != null;
	}

	public boolean isInstanceNotValid() throws SearchLibException {
		return getClient() == null;
	}

	public void invalidate(String id) {
		getFellow(id).invalidate();
	}

	public String getCurrentPage() throws SearchLibException {
		String page = isLogged() ? "controlpanel.zul" : "login.zul";
		return "WEB-INF/zul/" + page;
	}

	public User getLoggedUser() {
		return (User) getAttribute(ScopeAttribute.LOGGED_USER);
	}

	public boolean isAdmin() throws SearchLibException {
		User user = getLoggedUser();
		if (user == null)
			return false;
		return user.isAdmin();
	}

	public boolean isNoUserList() throws SearchLibException {
		return ClientCatalog.getUserList().isEmpty();
	}

	public boolean isAdminOrNoUser() throws SearchLibException {
		if (isNoUserList())
			return true;
		return isAdmin();
	}

	public boolean isLogged() throws SearchLibException {
		if (isNoUserList())
			return true;
		return getLoggedUser() != null;
	}

	private static void reloadComponent(Component component, boolean bReset) {
		if (component == null)
			return;
		if (component instanceof CommonController) {
			CommonController controller = (CommonController) component;
			if (bReset)
				controller.reset();
			controller.reloadPage();
		}
		List<?> children = component.getChildren();
		if (children != null)
			for (Object child : children)
				reloadComponent((Component) child, bReset);
	}

	private Iterator<?> getPagesIterator() {
		Desktop desktop = getDesktop();
		if (desktop == null)
			return null;
		Collection<?> pages = desktop.getPages();
		if (pages == null)
			return null;
		return pages.iterator();
	}

	private void reloadDesktop(boolean bReset) {
		Iterator<?> it = getPagesIterator();
		if (it == null)
			return;
		while (it.hasNext()) {
			Page page = (Page) it.next();
			if (page != null)
				reloadComponent(page.getFirstRoot(), bReset);
		}
	}

	protected void reloadDesktop() {
		reloadDesktop(false);
	}

	protected void resetDesktop() {
		reloadDesktop(true);
	}

	public void reloadComponent(String compId) {
		if (binder != null)
			binder.loadComponent(getFellow(compId));
	}

	public abstract void reset();

	public void reloadPage() {
		if (binder != null)
			binder.loadAll();
	}

	public void onReload() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		reloadPage();
	}

	public LanguageEnum[] getLanguageEnum() {
		return LanguageEnum.values();
	}

	protected void flushPrivileges(User user) {
		PushEvent.FLUSH_PRIVILEGES.publish(user);
	}

	public void onLogout() {
		for (ScopeAttribute attr : ScopeAttribute.values())
			setAttribute(attr, null);
		resetDesktop();
		Executions.sendRedirect("/");
	}

	public void onEvent(Event event) throws UiException {
		PushEvent pushEvent = PushEvent.isEvent(event);
		if (pushEvent != null && pushEvent == PushEvent.FLUSH_PRIVILEGES) {
			User user = (User) event.getData();
			try {
				ClientCatalog.flushPrivileges();
				if (isLogged()) {
					if (user.equals(getLoggedUser()))
						onLogout();
				}
			} catch (SearchLibException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getIndexName() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return getClient().getIndexDirectory().getName();
	}

	public boolean isUpdateRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.INDEX_UPDATE);
	}

	public boolean isSchemaRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.INDEX_SCHEMA);
	}

}
