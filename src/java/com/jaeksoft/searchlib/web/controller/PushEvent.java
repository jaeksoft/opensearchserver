/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

public enum PushEvent {

	FLUSH_PRIVILEGES;

	private Event newEvent(Object data) {
		return new Event(name(), null, data);
	}

	private Event newEvent() {
		return new Event(name());
	}

	private static EventQueue getQueue() {
		return EventQueues.lookup("OSS", EventQueues.APPLICATION, true);
	}

	public void publish() {
		getQueue().publish(newEvent());
	}

	public void publish(Object data) {
		getQueue().publish(newEvent(data));
	}

	public void subscribe(EventListener eventListener) {
		getQueue().subscribe(eventListener);
	}

	public static PushEvent isEvent(Event event) {
		return PushEvent.valueOf(event.getName());
	}
}
