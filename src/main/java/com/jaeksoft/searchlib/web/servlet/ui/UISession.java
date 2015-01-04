/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web.servlet.ui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.user.UserManager;

public class UISession {

	public enum Attributes {
		MESSAGES_LIST, LOGGED_USER;
	}

	private final HttpSession session;
	private final LinkedHashSet<UIMessage> messages;

	UISession(HttpSession session) {
		this.session = session;
		@SuppressWarnings("unchecked")
		LinkedHashSet<UIMessage> msg = (LinkedHashSet<UIMessage>) session
				.getAttribute(Attributes.MESSAGES_LIST.name());
		if (msg == null) {
			msg = new LinkedHashSet<UIMessage>();
			session.setAttribute(Attributes.MESSAGES_LIST.name(), msg);
		}
		messages = msg;
	}

	<T> T getAttribute(Attributes attr, Class<T> type) {
		return type.cast(session.getAttribute(attr.name()));
	}

	void setAttribute(Attributes attr, Object object) {
		if (object == null)
			session.removeAttribute(attr.name());
		else
			session.setAttribute(attr.name(), object);
	}

	public User getLoggedUser() {
		return getAttribute(Attributes.LOGGED_USER, User.class);
	}

	public boolean isLogged() throws SearchLibException {
		if (getLoggedUser() != null)
			return true;
		return isNoUsers();
	}

	public boolean isNoUsers() throws SearchLibException {
		return UserManager.getInstance().isEmpty();
	}

	void setLoggedUser(User user) {
		setAttribute(Attributes.LOGGED_USER, user);
	}

	public List<UIMessage> getMessages() {
		synchronized (messages) {
			return new ArrayList<UIMessage>(messages);
		}
	}

	public String printMessage(UIMessage message) {
		synchronized (messages) {
			messages.remove(message);
			return message.getMessage();
		}
	}

	void addMessage(UIMessage message) {
		synchronized (messages) {
			messages.add(message);
		}
	}
}
