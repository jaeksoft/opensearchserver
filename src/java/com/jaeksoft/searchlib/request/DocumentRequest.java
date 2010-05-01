/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.util.External;

public class DocumentRequest implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8390479489794895730L;

	protected String indexName;

	public int doc;

	protected int pos;

	public DocumentRequest() {
	}

	protected DocumentRequest(ResultScoreDoc resultScoreDoc, int pos) {
		this.indexName = resultScoreDoc.indexName;
		this.doc = resultScoreDoc.doc;
		this.pos = pos;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		indexName = External.readUTF(in);
		doc = in.readInt();
		pos = in.readInt();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeUTF(indexName, out);
		out.writeInt(doc);
		out.writeInt(pos);
	}
}
