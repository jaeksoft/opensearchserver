/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.XPathParser;

public class IndexConfig {

	private String name;

	private String path;

	private int searchCache;

	private int filterCache;

	private int documentCache;

	private String remoteUrl;

	private String keyField;

	private String keyMd5RegExp;

	public IndexConfig(XPathParser xpp, Node node) {
		name = XPathParser.getAttributeString(node, "name");
		path = XPathParser.getAttributeString(node, "path");
		searchCache = XPathParser.getAttributeValue(node, "searchCache");
		filterCache = XPathParser.getAttributeValue(node, "filterCache");
		documentCache = XPathParser.getAttributeValue(node, "documentCache");
		remoteUrl = XPathParser.getAttributeString(node, "remoteUrl");
		keyField = XPathParser.getAttributeString(node, "keyField");
		keyMd5RegExp = XPathParser.getAttributeString(node, "keyMd5RegExp");
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the searchCache
	 */
	public int getSearchCache() {
		return searchCache;
	}

	/**
	 * @param searchCache
	 *            the searchCache to set
	 */
	public void setSearchCache(int searchCache) {
		this.searchCache = searchCache;
	}

	/**
	 * @return the filterCache
	 */
	public int getFilterCache() {
		return filterCache;
	}

	/**
	 * @param filterCache
	 *            the filterCache to set
	 */
	public void setFilterCache(int filterCache) {
		this.filterCache = filterCache;
	}

	/**
	 * @return the documentCache
	 */
	public int getDocumentCache() {
		return documentCache;
	}

	/**
	 * @param documentCache
	 *            the documentCache to set
	 */
	public void setDocumentCache(int documentCache) {
		this.documentCache = documentCache;
	}

	/**
	 * @return the remoteUrl
	 */
	public String getRemoteUrl() {
		return remoteUrl;
	}

	/**
	 * @param remoteUrl
	 *            the remoteUrl to set
	 */
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	/**
	 * @return the keyField
	 */
	public String getKeyField() {
		return keyField;
	}

	/**
	 * @param keyField
	 *            the keyField to set
	 */
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}

	/**
	 * @return the keyMd5RegExp
	 */
	public String getKeyMd5RegExp() {
		return keyMd5RegExp;
	}

	/**
	 * @param keyMd5RegExp
	 *            the keyMd5RegExp to set
	 */
	public void setKeyMd5RegExp(String keyMd5RegExp) {
		this.keyMd5RegExp = keyMd5RegExp;
	}
}
