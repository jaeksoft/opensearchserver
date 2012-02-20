/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

public enum PushEvent {

	/**
	 * The privilege of the current user has change
	 */
	FLUSH_PRIVILEGES(EventQueues.APPLICATION),

	/**
	 * The user selects another index
	 */
	CLIENT_CHANGE(EventQueues.DESKTOP),

	/**
	 * An index has been switched
	 */
	CLIENT_SWITCH(EventQueues.DESKTOP),

	/**
	 * The user logs out
	 */
	LOG_OUT(EventQueues.DESKTOP),

	/**
	 * Notify that document has been inserted or deleted.
	 */
	DOCUMENT_UPDATED(EventQueues.APPLICATION),

	/**
	 * Notify that a request list has changed
	 */
	REQUEST_LIST_CHANGED(EventQueues.APPLICATION),

	/**
	 * Notify that the schema has changes (fields or analyzers)
	 */
	SCHEMA_CHANGED(EventQueues.APPLICATION),

	/**
	 * The user load a request
	 */
	QUERY_EDIT_REQUEST(EventQueues.DESKTOP),

	/**
	 * The user does a search
	 */
	QUERY_EDIT_RESULT(EventQueues.DESKTOP),

	/**
	 * An job has been selected for edition
	 */
	JOB_EDIT(EventQueues.SESSION),

	/**
	 * A filePathItem has been selected for edition
	 */
	FILEPATH_EDIT(EventQueues.SESSION);

	private final String eventName;

	private final String scope;

	private PushEvent(String scope) {
		this.scope = scope;
		this.eventName = "OSS_EVENT_" + name();
	}

	private Event newEvent(Object data) {
		return new Event(eventName, null, data);
	}

	private Event newEvent() {
		return new Event(eventName);
	}

	private static EventQueue getQueue(String scope) {
		return EventQueues.lookup("OSS", scope, true);
	}

	private static EventQueue getQueue(WebApp webApp) {
		return EventQueues.lookup("OSS", webApp, true);
	}

	public void publish() {
		if (Executions.getCurrent() == null)
			return;
		getQueue(scope).publish(newEvent());
	}

	public void publish(WebApp webApp) {
		getQueue(webApp).publish(newEvent());
	}

	public void publish(Object data) {
		if (Executions.getCurrent() == null)
			return;
		getQueue(scope).publish(newEvent(data));
	}

	public void publish(WebApp webApp, Object data) {
		getQueue(webApp).publish(newEvent(data));
	}

	private void subscribe(EventListener eventListener) {
		if (Executions.getCurrent() == null)
			return;
		getQueue(scope).subscribe(eventListener);
	}

	public static PushEvent isEvent(Event event) {
		String evName = event.getName();
		if (evName == null)
			return null;
		for (PushEvent pv : PushEvent.values())
			if (evName.equals(pv.eventName))
				return pv;
		return null;
	}

	public static void suscribe(EventListener eventListener) {
		for (PushEvent pushEvent : PushEvent.values())
			pushEvent.subscribe(eventListener);
	}

}
