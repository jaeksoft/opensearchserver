/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ResultScoreDocCollapse extends ResultScoreDoc {

	public static final ResultScoreDocCollapse[] EMPTY_ARRAY = new ResultScoreDocCollapse[0];

	private ResultScoreDoc[] collapsedDocs;

	private int collapseCount;

	public ResultScoreDocCollapse(ResultScoreDoc rsd) {
		super(rsd);
		this.collapsedDocs = ResultScoreDoc.EMPTY_ARRAY;
		this.collapseCount = 0;
	}

	public ResultScoreDocCollapse(ResultScoreDocCollapse rsdc) {
		super(rsdc);
		this.collapsedDocs = rsdc.collapsedDocs;
		this.collapseCount = rsdc.collapseCount;
	}

	private ResultScoreDocCollapse(ResultScoreDocCollapse rsdc, int offset) {
		super(rsdc);
		this.collapsedDocs = ArrayUtils.<ResultScoreDoc> subarray(
				rsdc.collapsedDocs, offset, rsdc.collapsedDocs.length);
	}

	final public void addCollapsed(ResultScoreDoc rsd, int max) {
		collapseCount++;
		if (max != 0 && collapseCount < max)
			ArrayUtils.<ResultScoreDoc> add(collapsedDocs, rsd);
	}

	final public int getCollapseCount() {
		return collapseCount;
	}

	final public ResultScoreDoc[] getCollapsedDocs() {
		return collapsedDocs;
	}

	@Override
	public ResultScoreDocCollapse newCollapseInstance() {
		return new ResultScoreDocCollapse(this);
	}

	final public void populateList(List<ResultScoreDoc> collapsedList, int max) {
		if (collapseCount < max) {
			collapsedList.add(new ResultScoreDoc(this));
			for (ResultScoreDoc rsd : collapsedDocs)
				collapsedList.add(rsd);
			return;
		}
		collapsedList.add(this);
	}
}
