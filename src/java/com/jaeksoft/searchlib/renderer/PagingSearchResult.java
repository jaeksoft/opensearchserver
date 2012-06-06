/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.renderer;

import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

public class PagingSearchResult {

	private int currentPage;
	private int totalPages;
	private int leftPage;
	private int rightPage;

	public PagingSearchResult(AbstractResultSearch result, int maxPages) {

		SearchRequest request = result.getRequest();
		int numFound = result.getNumFound() - result.getCollapsedDocCount();
		int start = request.getStart();
		int rows = request.getRows();

		if (numFound == 0) {
			currentPage = 0;
			totalPages = 0;
			leftPage = 0;
			rightPage = 0;
			return;
		}
		totalPages = (numFound + rows - 1) / rows;
		currentPage = (start + rows) / rows;
		leftPage = currentPage - maxPages / 2;
		if (leftPage < 1)
			leftPage = 1;
		rightPage = leftPage + maxPages - 1;
		if (rightPage > totalPages) {
			rightPage = totalPages;
			leftPage = rightPage - maxPages + 1;
			if (leftPage < 1)
				leftPage = 1;
		}
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * @return the leftPage
	 */
	public int getLeftPage() {
		return leftPage;
	}

	/**
	 * @return the rightPage
	 */
	public int getRightPage() {
		return rightPage;
	}
}
