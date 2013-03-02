/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.util.List;

public class HostUrlList {

	private List<UrlItem> urlList;
	private NamedItem namedItem;
	private ListType listType;

	public enum ListType {

		OLD_URL("old"),

		NEW_URL("new"),

		MANUAL("manual"),

		DBCRAWL("dbcrawl"),

		PRIORITY_URL("priority");

		private String label;

		private ListType(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}

		public String getLabel() {
			return label;
		}
	}

	public HostUrlList(List<UrlItem> urlList, NamedItem namedItem) {
		this.urlList = urlList;
		this.namedItem = namedItem;
		this.setListType(null);
	}

	/**
	 * @return the urlList
	 */
	public List<UrlItem> getUrlList() {
		return urlList;
	}

	/**
	 * @return the namedItem
	 */
	public NamedItem getNamedItem() {
		return namedItem;
	}

	/**
	 * @param listType
	 *            the listType to set
	 */
	public void setListType(ListType listType) {
		this.listType = listType;
	}

	/**
	 * @return the listType
	 */
	public ListType getListType() {
		return listType;
	}

}
