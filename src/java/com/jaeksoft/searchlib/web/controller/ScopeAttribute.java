/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;

public enum ScopeAttribute {

	BASKET_CURRENT_DOCUMENT(Component.SESSION_SCOPE),

	QUERY_SEARCH_REQUEST(Component.SESSION_SCOPE),

	QUERY_SEARCH_RESULT(Component.SESSION_SCOPE),

	UPDATE_FORM_INDEX_DOCUMENT(Component.SESSION_SCOPE),

	CURRENT_CLIENT(Component.SESSION_SCOPE),

	LOGGED_USER(Component.SESSION_SCOPE);

	private int scope;

	private ScopeAttribute(int scope) {
		this.scope = scope;
	}

	public void set(Component component, Object value) {
		switch (scope) {
		case Component.SESSION_SCOPE:
			if (value == null)
				Sessions.getCurrent().removeAttribute(name());
			else
				Sessions.getCurrent().setAttribute(name(), value);
			break;
		default:
			if (value == null)
				component.removeAttribute(name());
			else
				component.setAttribute(name(), value, scope);
			break;
		}
	}

	public Object get(Component component) {
		switch (scope) {
		case Component.SESSION_SCOPE:
			return Sessions.getCurrent().getAttribute(name());
		default:
			return component.getAttribute(name(), scope);
		}
	}

}
