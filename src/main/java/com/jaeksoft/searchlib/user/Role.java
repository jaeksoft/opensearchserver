/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.user;

import com.jaeksoft.searchlib.util.EnumerationUtils;

public enum Role {

	INDEX_QUERY("Index: query the index"),

	INDEX_UPDATE("Index: insert data"),

	INDEX_SCHEMA("Index: edit the schema"),

	CRAWLER_EDIT("Crawler: edit parameters"),

	CRAWLER_EXECUTE("Execute a crawler"),

	PARSER_EDIT("Crawler: edit parameters"),

	JOB_EDIT("Create / edit a job"),

	JOB_EXECUTE("Run a job");

	private String label;

	public static Role[] GROUP_INDEX = { INDEX_QUERY, INDEX_UPDATE,
			INDEX_SCHEMA };

	public static Role[] GROUP_CRAWLER = { CRAWLER_EDIT, CRAWLER_EXECUTE };

	public static Role[] GROUP_JOB = { JOB_EDIT, JOB_EXECUTE };

	private Role(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static Role find(String roleName) {
		return EnumerationUtils.lookup(Role.class, roleName, null);
	}

}
