/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Listitem;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.user.User;

public abstract class CommonComposer extends GenericForwardComposer implements
		EventInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2779225243528937766L;

	protected AnnotateDataBinder binder;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		binder = new AnnotateDataBinder(comp);
		binder.loadAll();
		PushEvent.suscribe(this);
	}

	protected AbstractRequest getRequest() throws SearchLibException {
		return (AbstractRequest) getAttribute(ScopeAttribute.QUERY_REQUEST);
	}

	protected final Object getAttribute(ScopeAttribute scopeAttribute) {
		return session.getAttribute(scopeAttribute.name());
	}

	protected void setAttribute(ScopeAttribute scopeAttribute, Object value) {
		session.setAttribute(scopeAttribute.name(), value);
	}

	@Override
	public Client getClient() throws SearchLibException {
		return (Client) getAttribute(ScopeAttribute.CURRENT_CLIENT);
	}

	@Override
	public User getLoggedUser() {
		return (User) getAttribute(ScopeAttribute.LOGGED_USER);
	}

	protected Event getOriginalEvent(Event event) {
		if (event instanceof ForwardEvent)
			return getOriginalEvent(((ForwardEvent) event).getOrigin());
		return event;
	}

	protected Component findParentByType(Component component,
			Class<? extends Component> cl) {
		if (component == null)
			return null;
		if (component.getClass().equals(cl))
			return component;
		return findParentByType(component.getParent(), cl);
	}

	protected Object getListItemValue(Event event) {
		event = getOriginalEvent(event);
		Listitem listitem = (Listitem) findParentByType(event.getTarget(),
				Listitem.class);
		if (listitem == null)
			return null;
		return listitem.getValue();
	}

	public void reloadComponent(Component component) {
		if (binder != null)
			binder.loadComponent(component);
	}

	public void reloadPage() {
		if (binder == null)
			return;
		binder.loadAll();
	}

	protected abstract void reset() throws SearchLibException;

	@Override
	public void eventClientChange() throws SearchLibException {
		reset();
		reloadPage();
	}

	@Override
	public void eventClientSwitch(Client client) throws SearchLibException {
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

	@Override
	public void eventFlushPrivileges() throws SearchLibException {
		reset();
		reloadPage();
	}

	@Override
	public void eventDocumentUpdate() throws SearchLibException {
	}

	@Override
	public void eventRequestListChange() throws SearchLibException {
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
	}

	@Override
	public void eventLogout() throws SearchLibException {
		reset();
		reloadPage();
	}

	@Override
	public final void onEvent(Event event) throws Exception {
		super.onEvent(event);
		EventDispatch.dispatch(this, event);

	}
}
