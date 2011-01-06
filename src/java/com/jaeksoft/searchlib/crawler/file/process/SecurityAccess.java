/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process;

import com.jaeksoft.searchlib.index.IndexDocument;

public class SecurityAccess {

	public enum Grant {
		ALLOW, DENY;
	}

	public enum Type {
		USER, GROUP;
	}

	private Grant grant;

	private Type type;

	private String id;

	public SecurityAccess() {
		this.grant = null;
		this.type = null;
		this.id = null;
	}

	/**
	 * @return the grant
	 */
	public Grant getGrant() {
		return grant;
	}

	/**
	 * @param grant
	 *            the grant to set
	 */
	public void setGrant(Grant grant) {
		this.grant = grant;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public boolean isValid() {
		if (grant == null)
			return false;
		if (type == null)
			return false;
		if (id == null)
			return false;
		return true;
	}

	final private void populate(IndexDocument indexDocument,
			String allowGroupFieldName, String denyGroupFieldName,
			String allowUserFieldName, String denyUserFieldName) {
		if (!isValid())
			return;
		if (type == Type.USER) {
			if (grant == Grant.ALLOW)
				indexDocument.add(allowUserFieldName, id);
			else
				indexDocument.add(denyUserFieldName, id);
		} else if (type == Type.GROUP) {
			if (grant == Grant.ALLOW)
				indexDocument.add(allowGroupFieldName, id);
			else
				indexDocument.add(denyGroupFieldName, id);
		}
	}

	public static void populate(SecurityAccess[] accessList,
			IndexDocument indexDocument, String allowGroupFieldName,
			String denyGroupFieldName, String allowUserFieldName,
			String denyUserFieldName) {
		if (accessList == null)
			return;
		for (SecurityAccess access : accessList)
			access.populate(indexDocument, allowGroupFieldName,
					denyGroupFieldName, allowUserFieldName, denyUserFieldName);
	}
}
