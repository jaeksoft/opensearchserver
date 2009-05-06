/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.External;

public class Filter implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3935917370775417249L;

	private transient Query query;

	private String queryString;

	private Source source;

	public enum Source {
		CONFIGXML, REQUEST
	}

	public Filter() {
		query = null;
	}

	public Filter(String req, Source src) {
		this.source = src;
		this.queryString = req;
		this.query = null;
	}

	public Query getQuery(Field defaultField, Analyzer analyzer)
			throws ParseException {
		if (query != null)
			return query;
		query = new QueryParser(defaultField.getName(), analyzer)
				.parse(queryString);
		return query;
	}

	public String getQueryString() {
		return this.queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
		this.query = null;
	}

	public Source getSource() {
		return this.source;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		queryString = External.readUTF(in);
		source = Source.valueOf(External.readUTF(in));
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeUTF(queryString, out);
		External.writeUTF(source.name(), out);
	}

}
