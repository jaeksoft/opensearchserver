/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.util.Iterator;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.Timer;

public class ResultDocumentIterator implements Iterator<ResultDocument> {

	private ResultDocumentsInterface<?> result;
	private int pos;
	private int end;
	private Timer timer;

	public ResultDocumentIterator(ResultDocumentsInterface<?> result,
			Timer timer) {
		this.result = result;
		pos = result.getRequestStart();
		if (pos < 0)
			pos = 0;
		end = result.getDocumentCount() + pos;
	}

	@Override
	public boolean hasNext() {
		return pos < end;
	}

	@Override
	public ResultDocument next() {
		try {
			return result.getDocument(pos++, timer);
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}
}