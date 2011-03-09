/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
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
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.AbstractServlet;

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

	@Override
	public void afterCompose() {
		binder = new AnnotateDataBinder(this);
		binder.loadAll();
		PushEvent.suscribe(this);
		isComposed = true;
	}

	final protected static StringBuffer getBaseUrl(Execution exe) {
		int port = exe.getServerPort();
		StringBuffer sb = new StringBuffer();
		sb.append(exe.getScheme());
		sb.append("://");
		sb.append(exe.getServerName());
		if (port != 80) {
			sb.append(":");
			sb.append(port);
		}
		sb.append(exe.getContextPath());
		return sb;

	}

	final public static StringBuffer getBaseUrl() {
		Execution exe = Executions.getCurrent();
		return getBaseUrl(exe);
	}

	final public static StringBuffer getApiUrl(String servletPathName)
			throws UnsupportedEncodingException {
		Execution exe = Executions.getCurrent();
		StringBuffer sb = getBaseUrl();
		Client client = (Client) exe.getSession().getAttribute(
				ScopeAttribute.CURRENT_CLIENT.name());
		User user = (User) exe.getSession().getAttribute(
				ScopeAttribute.LOGGED_USER.name());
		return AbstractServlet.getApiUrl(sb, servletPathName, client, user);
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute,
			Object defaultValue) {
		Object o = scopeAttribute.get(this);
		return o == null ? defaultValue : o;
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute) {
		return scopeAttribute.get(this);
	}

	protected void setAttribute(ScopeAttribute scopeAttribute, Object value) {
		if (isComposed)
			scopeAttribute.set(this, value);
	}

	protected Object getRecursiveComponentAttribute(Component component,
			String attributeName) {
		Object attr = null;
		while (component != null)
			if ((attr = component.getAttribute(attributeName)) != null)
				return attr;
			else
				component = component.getParent();
		return null;
	}

	public Client getClient() throws SearchLibException {
		return (Client) getAttribute(ScopeAttribute.CURRENT_CLIENT);
	}

	protected void setClient(Client client) {
		setAttribute(ScopeAttribute.CURRENT_CLIENT, client);
		PushEvent.CLIENT_CHANGE.publish();
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

	public boolean isAdminOrMonitoringOrNoUser() throws SearchLibException {
		if (isNoUserList())
			return true;
		User user = getLoggedUser();
		if (user == null)
			return false;
		return user.isAdmin() || user.isMonitoring();
	}

	public boolean isLogged() throws SearchLibException {
		if (isNoUserList())
			return true;
		return getLoggedUser() != null;
	}

	public void reloadComponent(String compId) {
		reloadComponent(getFellow(compId));
	}

	public void reloadComponent(Component component) {
		if (binder != null) {
			component.invalidate();
			binder.loadComponent(component);
		}
	}

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
		PushEvent.LOG_OUT.publish();
		Executions.sendRedirect("/");
	}

	private boolean sameClient(Event event) throws SearchLibException {
		Client client = (Client) event.getData();
		if (client == null)
			return false;
		Client localClient = getClient();
		if (localClient == null)
			return false;
		return client.getIndexName().equals(localClient.getIndexName());
	}

	private boolean sameUser(Event event) throws SearchLibException {
		User user = (User) event.getData();
		if (user == null)
			return false;
		if (!isLogged())
			return false;
		return user.equals(getLoggedUser());
	}

	@Override
	final public void onEvent(Event event) throws UiException {
		PushEvent pushEvent = PushEvent.isEvent(event);
		if (pushEvent == null)
			return;
		try {
			if (pushEvent == PushEvent.FLUSH_PRIVILEGES) {
				ClientCatalog.flushPrivileges();
				if (sameUser(event)) {
					eventFlushPrivileges();
					onLogout();
				}
			} else if (pushEvent == PushEvent.CLIENT_CHANGE)
				eventClientChange();
			else if (pushEvent == PushEvent.CLIENT_SWITCH)
				eventClientSwitch((Client) event.getData());
			else if (pushEvent == PushEvent.DOCUMENT_UPDATED) {
				if (sameClient(event))
					eventDocumentUpdate();
			} else if (pushEvent == PushEvent.REQUEST_LIST_CHANGED) {
				if (sameClient(event))
					eventRequestListChange();
			} else if (pushEvent == PushEvent.SCHEMA_CHANGED) {
				if (sameClient(event))
					eventSchemaChange();
			} else if (pushEvent == PushEvent.LOG_OUT)
				eventLogout();
			else if (pushEvent == PushEvent.QUERY_EDIT_REQUEST)
				eventQueryEditRequest((SearchRequest) event.getData());
			else if (pushEvent == PushEvent.QUERY_EDIT_RESULT)
				eventQueryEditResult((Result) event.getData());
			else if (pushEvent == PushEvent.JOB_EDIT)
				eventJobEdit((JobItem) event.getData());
			else if (pushEvent == PushEvent.FILEPATH_EDIT)
				eventFilePathEdit((FilePathItem) event.getData());
		} catch (SearchLibException e) {
			throw new UiException(e);
		}
	}

	protected abstract void reset() throws SearchLibException;

	protected void eventClientChange() throws SearchLibException {
		reset();
		reloadPage();
	}

	protected void eventClientSwitch(Client client) throws SearchLibException {
		if (client == null)
			return;
		Client currentClient = getClient();
		if (currentClient == null)
			return;
		if (!client.getIndexName().equals(currentClient.getIndexName()))
			return;
		reset();
		reloadPage();
	}

	protected void eventFlushPrivileges() throws SearchLibException {
		reset();
		reloadPage();
	}

	protected void eventDocumentUpdate() throws SearchLibException {
	}

	protected void eventRequestListChange() throws SearchLibException {
	}

	protected void eventSchemaChange() throws SearchLibException {
	}

	protected void eventJobEdit(JobItem jobItem) throws SearchLibException {
	}

	protected void eventFilePathEdit(FilePathItem filePathItem)
			throws SearchLibException {
	}

	protected void eventLogout() throws SearchLibException {
		reset();
		reloadPage();
	}

	protected void eventQueryEditResult(Result data) {
	}

	protected void eventQueryEditRequest(SearchRequest data) {
	}

	protected String getIndexName() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return getClient().getIndexName();
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
