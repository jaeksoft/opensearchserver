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

package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.result.Result;

public class Pagination {

	private int totalPage = 0;

	private int currentPage = 0;

	private List<Integer> navPages;

	private int pageSize;

	public Pagination(int totalCount, int currentCount, int pageSize,
			int pagesArround) {
		this.pageSize = pageSize;
		if (totalCount > 0)
			totalPage = totalCount / pageSize + 1;
		currentPage = currentCount / pageSize;
		int min = currentPage - pagesArround;
		if (min < 1)
			min = 1;
		int max = currentPage + pagesArround;
		if (max > totalPage)
			max = totalPage;
		navPages = new ArrayList<Integer>();
		for (int i = min; i <= max; i++)
			navPages.add(i);
	}

	public Pagination(Result<?> result, int pagesArround) {
		this(result.getNumFound(), result.getRequest().getStart(), result
				.getRequest().getRows(), pagesArround);
	}

	public List<Integer> getPages() {
		return navPages;
	}

	public int getStart(Integer p) {
		return (p - 1) * pageSize;
	}
}
