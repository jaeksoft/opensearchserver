/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import org.zkoss.zk.ui.Sessions;

public enum ScopeAttribute {

	BASKET_CURRENT_DOCUMENT(Component.SESSION_SCOPE),

	QUERY_REQUEST(Component.SESSION_SCOPE),

	QUERY_SEARCH_RESULT(Component.SESSION_SCOPE),

	UPDATE_FORM_INDEX_DOCUMENT(Component.SESSION_SCOPE),

	UPDATE_XML_MAP(Component.SESSION_SCOPE),

	CURRENT_CLIENT(Component.SESSION_SCOPE),

	LOGGED_USER(Component.SESSION_SCOPE),

	SEARCH_URL_HOST(Component.SESSION_SCOPE),

	SEARCH_URL_SUBHOST(Component.SESSION_SCOPE),

	SEARCH_URL_RESPONSE_CODE(Component.SESSION_SCOPE),

	SEARCH_URL_LANG(Component.SESSION_SCOPE),

	SEARCH_URL_LANG_METHOD(Component.SESSION_SCOPE),

	SEARCH_URL_CONTENT_BASE_TYPE(Component.SESSION_SCOPE),

	SEARCH_URL_CONTENT_TYPE_CHARSET(Component.SESSION_SCOPE),

	SEARCH_URL_CONTENT_ENCODING(Component.SESSION_SCOPE),

	SEARCH_URL_MIN_CONTENT_LENGTH(Component.SESSION_SCOPE),

	SEARCH_URL_MAX_CONTENT_LENGTH(Component.SESSION_SCOPE),

	SEARCH_URL_FETCH_STATUS(Component.SESSION_SCOPE),

	SEARCH_URL_PARSER_STATUS(Component.SESSION_SCOPE),

	SEARCH_URL_INDEX_STATUS(Component.SESSION_SCOPE),

	SEARCH_URL_ROBOTSTXT_STATUS(Component.SESSION_SCOPE),

	SEARCH_URL_LIKE(Component.SESSION_SCOPE),

	SEARCH_URL_SHEET_ROWS(Component.SESSION_SCOPE),

	SEARCH_URL_DATE_START(Component.SESSION_SCOPE),

	SEARCH_URL_DATE_END(Component.SESSION_SCOPE),

	SEARCH_URL_DATE_MODIFIED_START(Component.SESSION_SCOPE),

	SEARCH_URL_DATE_MODIFIED_END(Component.SESSION_SCOPE),

	SEARCH_FILE_DATE_START(Component.SESSION_SCOPE),

	SEARCH_FILE_DATE_END(Component.SESSION_SCOPE),

	SEARCH_FILE_DATE_MODIFIED_START(Component.SESSION_SCOPE),

	SEARCH_FILE_DATE_MODIFIED_END(Component.SESSION_SCOPE),

	SEARCH_FILE_LANG(Component.SESSION_SCOPE),

	SEARCH_FILE_FILE_TYPE(Component.SESSION_SCOPE),

	SEARCH_FILE_FILE_EXTENSION(Component.SESSION_SCOPE),

	SEARCH_FILE_LANG_METHOD(Component.SESSION_SCOPE),

	SEARCH_FILE_MIN_CONTENT_LENGTH(Component.SESSION_SCOPE),

	SEARCH_FILE_MAX_CONTENT_LENGTH(Component.SESSION_SCOPE),

	SEARCH_FILE_FETCH_STATUS(Component.SESSION_SCOPE),

	SEARCH_FILE_PARSER_STATUS(Component.SESSION_SCOPE),

	SEARCH_FILE_INDEX_STATUS(Component.SESSION_SCOPE),

	SEARCH_FILE_FILE_NAME(Component.SESSION_SCOPE),

	SEARCH_FILE_SHEET_ROWS(Component.SESSION_SCOPE),

	SEARCH_FILE_REPOSITORY(Component.SESSION_SCOPE),

	RENDERER_IFRAME_WIDTH(Component.SESSION_SCOPE),

	RENDERER_IFRAME_HEIGHT(Component.SESSION_SCOPE);

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

	public void remove(Component component) {
		switch (scope) {
		case Component.SESSION_SCOPE:
			Sessions.getCurrent().removeAttribute(name());
			break;
		default:
			component.removeAttribute(name(), scope);
			break;
		}

	}
}
