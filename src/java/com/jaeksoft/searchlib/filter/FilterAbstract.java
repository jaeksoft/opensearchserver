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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class FilterAbstract {

	private Source source;

	private boolean negative;

	public enum Source {
		CONFIGXML, REQUEST
	}

	protected FilterAbstract(Source source, boolean negative) {
		this.source = source;
		this.negative = negative;
	}

	public Source getSource() {
		return this.source;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public abstract String getCacheKey(Field defaultField, Analyzer analyzer)
			throws ParseException;

	public abstract void writeXmlConfig(XmlWriter xmlWriter)
			throws SAXException;

	public abstract FilterHits getFilterHits(ReaderLocal reader,
			Field defaultField, Analyzer analyzer, Timer timer)
			throws ParseException, IOException;

}
