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

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.XPathParser;

public class IndexConfig {

	private String name;

	private int searchCache;

	private int filterCache;

	private int fieldCache;

	private URI remoteUri;

	private String keyField;

	private String keyMd5RegExp;

	public IndexConfig(XPathParser xpp, Node node) throws URISyntaxException {
		name = XPathParser.getAttributeString(node, "name");
		searchCache = XPathParser.getAttributeValue(node, "searchCache");
		filterCache = XPathParser.getAttributeValue(node, "filterCache");
		fieldCache = XPathParser.getAttributeValue(node, "fieldCache");
		if (fieldCache == 0)
			fieldCache = XPathParser.getAttributeValue(node, "documentCache");
		String s = XPathParser.getAttributeString(node, "remoteUrl");
		remoteUri = s == null ? null : new URI(s);
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
	public int getFieldCache() {
		return fieldCache;
	}

	/**
	 * @param documentCache
	 *            the documentCache to set
	 */
	public void setFieldCache(int fieldCache) {
		this.fieldCache = fieldCache;
	}

	/**
	 * @return the remoteUri
	 */
	public URI getRemoteUri() {
		return remoteUri;
	}

	/**
	 * @param remoteUri
	 *            the remoteUri to set
	 */
	public void setRemoteUri(URI remoteUri) {
		this.remoteUri = remoteUri;
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
