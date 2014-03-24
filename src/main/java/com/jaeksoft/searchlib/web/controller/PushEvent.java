/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;

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
	eventClientSwitch(EventQueues.APPLICATION),

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
	 * Notify that a file repository is edited
	 */
	eventEditFileRepository(EventQueues.DESKTOP),

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

	public final static String QUEUE_NAME = "oss";

	private PushEvent(String scope) {
		this.scope = scope;
	}

	public void publish() {
		publish(null);
	}

	public void publish(Object data) {
		Event event = new Event(name(), null, data);
		if (EventQueues.DESKTOP.equals(scope)) {
			if (Executions.getCurrent() == null)
				return;
			EventQueues.lookup(QUEUE_NAME, true).publish(event);
		} else if (EventQueues.APPLICATION.equals(scope)) {
			if (WebApps.getCurrent() == null)
				return;
			EventQueues.lookup(QUEUE_NAME, WebApps.getCurrent(), true).publish(
					event);
		}
	}
}
