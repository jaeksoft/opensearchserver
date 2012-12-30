/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.Map;
import java.util.TreeMap;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.User;

public enum PushEvent {

	/**
	 * The privilege of the current user has change
	 */
	eventFlushPrivileges(EventQueues.APPLICATION),

	/**
	 * The user selects another index
	 */
	eventClientChange(EventQueues.DESKTOP),

	/**
	 * An index has been switched
	 */
	eventClientSwitch(EventQueues.DESKTOP),

	/**
	 * The user logs out
	 */
	eventLogout(EventQueues.DESKTOP),

	/**
	 * Notify that document has been inserted or deleted.
	 */
	eventDocumentUpdate(EventQueues.APPLICATION),

	/**
	 * Notify that a request list has changed
	 */
	eventRequestListChange(EventQueues.APPLICATION),

	/**
	 * Notify that a request is edited
	 */
	eventEditRequest(EventQueues.DESKTOP),

	/**
	 * Notify that a scheduler is edited
	 */
	eventEditScheduler(EventQueues.DESKTOP),

	/**
	 * Notify that the edited request has a new result
	 */
	eventEditRequestResult(EventQueues.DESKTOP),

	/**
	 * Notify that the schema has changes (fields or analyzers)
	 */
	eventSchemaChange(EventQueues.DESKTOP);

	private final String scope;

	private PushEvent(String scope) {
		this.scope = scope;
	}

	public void publish() {
		if (Executions.getCurrent() == null)
			return;
		Logging.debug("publish " + name());
		BindUtils.postGlobalCommand(null, scope, name(), null);
	}

	private void publish(String name, Object data) {
		if (Executions.getCurrent() == null)
			return;
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put(name, data);
		Logging.debug("publish " + name() + " " + data);
		BindUtils.postGlobalCommand(null, scope, name(), map);
	}

	public void publish(Client client) {
		publish("client", client);
	}

	public void publish(User user) {
		publish("user", user);
	}

	public void publish(AbstractRequest request) {
		publish("request", request);
	}

	public void publish(AbstractResult<?> result) {
		publish("result", result);
	}

	public void publish(JobItem jobItem) {
		publish("jobItem", jobItem);
	}

}
