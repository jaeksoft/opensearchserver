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

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.User;

public class EventDispatch {

	final private static boolean sameClient(EventInterface receiver, Event event)
			throws SearchLibException {
		Client client = (Client) event.getData();
		if (client == null)
			return false;
		Client localClient = receiver.getClient();
		if (localClient == null)
			return false;
		return client.getIndexName().equals(localClient.getIndexName());
	}

	final private static boolean sameUser(EventInterface receiver, Event event)
			throws SearchLibException {
		User user = (User) event.getData();
		if (user == null)
			return false;
		return user.equals(receiver.getLoggedUser());
	}

	final public static void dispatch(EventInterface receiver, Event event)
			throws UiException {
		PushEvent pushEvent = PushEvent.isEvent(event);
		if (pushEvent == null)
			return;
		try {
			if (pushEvent == PushEvent.FLUSH_PRIVILEGES) {
				ClientCatalog.flushPrivileges();
				if (EventDispatch.sameUser(receiver, event))
					receiver.eventFlushPrivileges();
			} else if (pushEvent == PushEvent.CLIENT_CHANGE)
				receiver.eventClientChange();
			else if (pushEvent == PushEvent.CLIENT_SWITCH)
				receiver.eventClientSwitch((Client) event.getData());
			else if (pushEvent == PushEvent.DOCUMENT_UPDATED) {
				if (sameClient(receiver, event))
					receiver.eventDocumentUpdate();
			} else if (pushEvent == PushEvent.REQUEST_LIST_CHANGED) {
				if (sameClient(receiver, event))
					receiver.eventRequestListChange();
			} else if (pushEvent == PushEvent.SCHEMA_CHANGED) {
				if (sameClient(receiver, event))
					receiver.eventSchemaChange();
			} else if (pushEvent == PushEvent.LOG_OUT)
				receiver.eventLogout();
			else if (pushEvent == PushEvent.QUERY_EDIT_REQUEST)
				receiver.eventQueryEditRequest((AbstractRequest) event
						.getData());
			else if (pushEvent == PushEvent.QUERY_EDIT_RESULT)
				receiver.eventQueryEditResult((AbstractResult<?>) event
						.getData());
			else if (pushEvent == PushEvent.JOB_EDIT)
				receiver.eventJobEdit((JobItem) event.getData());
			else if (pushEvent == PushEvent.FILEPATH_EDIT)
				receiver.eventFilePathEdit((FilePathItem) event.getData());
		} catch (SearchLibException e) {
			throw new UiException(e);
		}
	}
}
