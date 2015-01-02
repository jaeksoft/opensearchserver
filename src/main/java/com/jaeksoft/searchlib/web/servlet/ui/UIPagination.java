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

public class UIPagination {

	final int listSize;
	final int pageNumber;
	final int currentPage;
	final int start;
	final int end;

	UIPagination(UITransaction transaction, String pageParameter, int pageSize,
			Collection<?> collection) {
		listSize = collection.size();
		pageNumber = listSize / pageSize;
		int p = transaction.getRequestParameterInteger(pageParameter, 0);
		currentPage = p > pageNumber ? pageNumber : p;
		start = currentPage * pageSize;
		int e = start + pageSize;
		end = e > listSize ? listSize : e;
	}

	/**
	 * 
	 * @return a new ArrayList with the exact page size
	 */
	public <T> ArrayList<T> getNewPageList() {
		return new ArrayList<T>(end - start);
	}

	/**
	 * @return the listSize
	 */
	public int getListSize() {
		return listSize;
	}

	/**
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return pageNumber;
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
