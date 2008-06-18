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
