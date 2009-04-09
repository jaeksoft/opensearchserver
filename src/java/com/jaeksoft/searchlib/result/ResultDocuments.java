/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import com.jaeksoft.searchlib.util.External;

public class ResultDocuments implements Externalizable,
		Iterable<ResultDocument> {

	private static final long serialVersionUID = 6099476851625264882L;

	private ResultDocument[] resultDocuments;

	public ResultDocuments() {
		resultDocuments = null;
	}

	public ResultDocuments(int size) {
		resultDocuments = new ResultDocument[size];
	}

	public ResultDocument set(int pos, ResultDocument resultDocument) {
		return resultDocuments[pos] = resultDocument;
	}

	public ResultDocument get(int pos) {
		return resultDocuments[pos];
	}

	public int size() {
		if (resultDocuments == null)
			return 0;
		else
			return resultDocuments.length;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int l = in.readInt();
		if (l > 0) {
			resultDocuments = new ResultDocument[l];
			External.readObjectArray(in, resultDocuments);
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObjectArray(resultDocuments, out);
	}

	private class ResultDocumentIterator implements Iterator<ResultDocument> {

		private int pos;

		ResultDocumentIterator() {
			pos = 0;
		}

		public boolean hasNext() {
			if (resultDocuments == null)
				return false;
			return pos < resultDocuments.length;
		}

		public ResultDocument next() {
			return resultDocuments[pos++];
		}

		public void remove() {
			throw new RuntimeException("Not permitted");
		}

	}

	public Iterator<ResultDocument> iterator() {
		return new ResultDocumentIterator();
	}

}
