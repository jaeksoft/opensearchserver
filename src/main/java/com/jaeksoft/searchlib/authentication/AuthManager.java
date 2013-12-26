/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.authentication;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.UpdateInterfaces;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public class AuthManager implements UpdateInterfaces.Before {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private File authFile = null;

	private boolean enabled = false;

	private String userAllowField = null;

	private String groupAllowField = null;

	private String userDenyField = null;

	private String groupDenyField = null;

	private String defaultUser = null;

	private String defaultGroup = null;

	private final static String AUTH_CONFIG_FILENAME = "auth.xml";
	private final static String AUTH_ITEM_ROOT_NODE = "auth";
	private final static String AUTH_ATTR_ENABLED = "enabled";
	private final static String AUTH_ATTR_USER_ALLOW_FIELD = "userAllowField";
	private final static String AUTH_ATTR_USER_DENY_FIELD = "userDenyField";
	private final static String AUTH_ATTR_GROUP_ALLOW_FIELD = "groupAllowField";
	private final static String AUTH_ATTR_GROUP_DENY_FIELD = "groupDenyField";
	private final static String AUTH_ATTR_DEFAULT_USER = "defaultUser";
	private final static String AUTH_ATTR_DEFAULT_GROUP = "defaultGroup";

	public AuthManager(Config config, File indexDir) throws SAXException,
			IOException, ParserConfigurationException {
		authFile = new File(indexDir, AUTH_CONFIG_FILENAME);
		if (!authFile.exists())
			return;
		StreamSource streamSource = new StreamSource(authFile);
		Node docNode = DomUtils.readXml(streamSource, false);
		Node authNode = DomUtils.getFirstNode(docNode, AUTH_ITEM_ROOT_NODE);
		if (authNode == null)
			return;
		enabled = DomUtils.getAttributeBoolean(authNode, AUTH_ATTR_ENABLED,
				false);
		userAllowField = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_USER_ALLOW_FIELD);
		userDenyField = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_USER_DENY_FIELD);
		groupAllowField = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_GROUP_ALLOW_FIELD);
		groupDenyField = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_GROUP_DENY_FIELD);
		defaultUser = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_DEFAULT_USER);
		defaultGroup = DomUtils.getAttributeText(authNode,
				AUTH_ATTR_DEFAULT_GROUP);
	}

	/**
	 * @return the userAllowField
	 */
	public String getUserAllowField() {
		rwl.r.lock();
		try {
			return userAllowField;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param userAllowField
	 *            the userAllowField to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setUserAllowField(String userAllowField) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.userAllowField = userAllowField;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the groupAllowField
	 */
	public String getGroupAllowField() {
		rwl.r.lock();
		try {
			return groupAllowField;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param groupAllowField
	 *            the groupAllowField to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setGroupAllowField(String groupAllowField) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.groupAllowField = groupAllowField;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the userDenyField
	 */
	public String getUserDenyField() {
		rwl.r.lock();
		try {
			return userDenyField;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param userDenyField
	 *            the userDenyField to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setUserDenyField(String userDenyField) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.userDenyField = userDenyField;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the groupDenyField
	 */
	public String getGroupDenyField() {
		rwl.r.lock();
		try {
			return groupDenyField;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param groupDenyField
	 *            the groupDenyField to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setGroupDenyField(String groupDenyField) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.groupDenyField = groupDenyField;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		rwl.r.lock();
		try {
			return enabled;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setEnabled(boolean enabled) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.enabled = enabled;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the defaultUser
	 */
	public String getDefaultUser() {
		rwl.r.lock();
		try {
			return defaultUser;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param defaultUser
	 *            the defaultUser to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setDefaultUser(String defaultUser) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.defaultUser = defaultUser;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the defaultGroup
	 */
	public String getDefaultGroup() {
		rwl.r.lock();
		try {
			return defaultGroup;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param defaultGroup
	 *            the defaultGroup to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setDefaultGroup(String defaultGroup) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			this.defaultGroup = defaultGroup;
			save_noLock();
		} finally {
			rwl.w.unlock();
		}
	}

	private void save_noLock() throws IOException, SearchLibException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(authFile, "UTF-8");
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement(AUTH_ITEM_ROOT_NODE, AUTH_ATTR_ENABLED,
					Boolean.toString(enabled), AUTH_ATTR_USER_ALLOW_FIELD,
					userAllowField, AUTH_ATTR_USER_DENY_FIELD, userDenyField,
					AUTH_ATTR_GROUP_ALLOW_FIELD, groupAllowField,
					AUTH_ATTR_GROUP_DENY_FIELD, groupDenyField,
					AUTH_ATTR_DEFAULT_USER, defaultUser,
					AUTH_ATTR_DEFAULT_GROUP, defaultGroup);
			xmlWriter.endElement();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(pw);
		}
	}

	@Override
	public void update(Schema schema, IndexDocument document)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (!enabled)
				return;
			if (!StringUtils.isEmpty(defaultUser)
					&& !StringUtils.isEmpty(userAllowField)) {
				if (!document.hasContent(userAllowField))
					document.add(userAllowField, defaultUser, null);
			}
			if (!StringUtils.isEmpty(defaultGroup)
					&& !StringUtils.isEmpty(groupAllowField)) {
				if (!document.hasContent(groupAllowField))
					document.add(groupAllowField, defaultGroup, null);
			}
		} finally {
			rwl.r.unlock();
		}
	}
}
