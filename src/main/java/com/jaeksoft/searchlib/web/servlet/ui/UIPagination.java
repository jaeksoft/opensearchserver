/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.List;

public class UIPagination {

	final int listSize;
	final int lastPageNumber;
	final int currentPage;
	final int start;
	final int end;
	final List<Integer> pages;

	UIPagination(UITransaction transaction, String pageParameter, int pageSize,
			Collection<?> collection) {
		listSize = collection.size();
		lastPageNumber = listSize == 0 ? 0 : (listSize - 1) / pageSize;
		;
		int p = transaction.getRequestParameterInteger(pageParameter, 0);
		currentPage = p > lastPageNumber ? lastPageNumber : p;
		start = currentPage * pageSize;
		int e = start + pageSize;
		end = e > listSize ? listSize : e;

		int pageStart = currentPage - 4;
		if (pageStart < 0)
			pageStart = 0;
		int pageEnd = currentPage + 4;
		if (pageEnd > lastPageNumber)
			pageEnd = lastPageNumber;
		pages = new ArrayList<Integer>();
		for (p = pageStart; p <= pageEnd; p++)
			pages.add(p);
	}

	/**
	 * 
	 * @return a new ArrayList with the exact page size
	 */
	public <T> ArrayList<T> getNewPageList() {
		return new ArrayList<T>(end - start);
	}

	public boolean isPrev() {
		return lastPageNumber > 0 && currentPage > 0;
	}

	public boolean isNext() {
		return lastPageNumber > 0 && currentPage < lastPageNumber;
	}

	public List<Integer> getPages() {
		return pages;
	}

	/**
	 * @return the listSize
	 */
	public int getListSize() {
		return listSize;
	}

	/**
	 * @return the lastPageNumber
	 */
	public int getLastPageNumber() {
		return lastPageNumber;
	}

	/**
	 * @return the page
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}
}
