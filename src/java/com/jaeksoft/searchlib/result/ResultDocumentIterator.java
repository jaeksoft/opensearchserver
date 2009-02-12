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

package com.jaeksoft.searchlib.result;

import java.util.Iterator;

public class ResultDocumentIterator implements Iterator<ResultDocument> {

	private ResultDocument[] resultDocuments;

	private int pos;

	protected ResultDocumentIterator(ResultDocument[] resultDocuments) {
		this.resultDocuments = resultDocuments;
		pos = 0;
	}

	public boolean hasNext() {
		return pos < resultDocuments.length;
	}

	public ResultDocument next() {
		return resultDocuments[pos++];
	}

	public void remove() {
		throw new RuntimeException("Not allowed");
	}

}
