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

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public enum ScopeAttribute {

	QUERY_REQUEST(),

	QUERY_RESULT(),

	UPDATE_FORM_INDEX_DOCUMENT(),

	UPDATE_XML_MAP(),

	CURRENT_CLIENT(),

	LOGGED_USER(),

	SEARCH_URL_HOST(),

	SEARCH_URL_SUBHOST(),

	SEARCH_URL_RESPONSE_CODE(),

	SEARCH_URL_LANG(),

	SEARCH_URL_LANG_METHOD(),

	SEARCH_URL_CONTENT_BASE_TYPE(),

	SEARCH_URL_CONTENT_TYPE_CHARSET(),

	SEARCH_URL_CONTENT_ENCODING(),

	SEARCH_URL_MIN_CONTENT_LENGTH(),

	SEARCH_URL_MAX_CONTENT_LENGTH(),

	SEARCH_URL_FETCH_STATUS(),

	SEARCH_URL_PARSER_STATUS(),

	SEARCH_URL_INDEX_STATUS(),

	SEARCH_URL_ROBOTSTXT_STATUS(),

	SEARCH_URL_LIKE(),

	SEARCH_URL_SHEET_ROWS(),

	SEARCH_URL_DATE_START(),

	SEARCH_URL_DATE_END(),

	SEARCH_URL_DATE_MODIFIED_START(),

	SEARCH_URL_DATE_MODIFIED_END(),

	SEARCH_FILE_DATE_START(),

	SEARCH_FILE_DATE_END(),

	SEARCH_FILE_DATE_MODIFIED_START(),

	SEARCH_FILE_DATE_MODIFIED_END(),

	SEARCH_FILE_LANG(),

	SEARCH_FILE_FILE_TYPE(),

	SEARCH_FILE_FILE_EXTENSION(),

	SEARCH_FILE_LANG_METHOD(),

	SEARCH_FILE_MIN_CONTENT_LENGTH(),

	SEARCH_FILE_MAX_CONTENT_LENGTH(),

	SEARCH_FILE_FETCH_STATUS(),

	SEARCH_FILE_PARSER_STATUS(),

	SEARCH_FILE_INDEX_STATUS(),

	SEARCH_FILE_FILE_NAME(),

	SEARCH_FILE_SHEET_ROWS(),

	SEARCH_FILE_REPOSITORY(),

	RENDERER_IFRAME_WIDTH(),

	RENDERER_IFRAME_HEIGHT(),

	FILEPATHITEM_SELECTED(),

	FILEPATHITEM_EDIT(),

	JOBITEM_SELECTED(),

	JOBITEM_EDIT();

	public void set(Session session, Object value) {
		if (session == null) {
			session = Sessions.getCurrent();
			System.err.println("SESSION WAS NULL " + session);
		}
		if (value == null)
			remove(session);
		else
			session.setAttribute(name(), value);
	}

	public Object get(Session session) {
		if (session == null)
			session = Sessions.getCurrent();
		return session.getAttribute(name());
	}

	public void remove(Session session) {
		if (session == null)
			session = Sessions.getCurrent();
		session.removeAttribute(name());
	}
}
