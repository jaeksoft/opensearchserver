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

package com.jaeksoft.searchlib.sort;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SortField extends AbstractField<SortField> implements
		CacheKeyInterface<SortField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3269790150800596793L;

	private boolean desc;

	public SortField(String requestSort) {
		super();
		int c = requestSort.charAt(0);
		desc = (c == '-');
		name = (c == '+' || c == '-') ? requestSort.substring(1) : requestSort;
	}

	public SortField(String fieldName, boolean desc) {
		super(fieldName);
		this.desc = desc;
	}

	public SortField(Node node) {
		super(DomUtils.getAttributeText(node, "name"));
		setDesc(DomUtils.getAttributeText(node, "direction"));
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(String v) {
		final String[] ascArray = { "+", "asc", "ascendant", "ascending" };
		for (String asc : ascArray) {
			if (asc.equalsIgnoreCase(v)) {
				desc = false;
				return;
			}
		}
		desc = true;
	}

	@Override
	public SortField duplicate() {
		return new SortField(name, desc);
	}

	final public boolean isScore() {
		return name.equals("score");
	}

	public SorterAbstract getSorter(DocIdInterface collector, ReaderLocal reader)
			throws IOException {
		if (isScore()) {
			if (desc)
				return new DescScoreSorter(collector);
			else
				return new AscScoreSorter(collector);
		}
		if (desc)
			return new DescStringIndexSorter(collector,
					reader.getStringIndex(name));
		else
			return new AscStringIndexSorter(collector,
					reader.getStringIndex(name));
	}

	@Override
	public int compareTo(SortField o) {
		int c = super.compareTo(o);
		if (c != 0)
			return c;
		if (desc == o.desc)
			return 0;
		return desc ? -1 : 1;
	}

	@Override
	public void toString(StringBuffer sb) {
		if (desc)
			sb.append('-');
		else
			sb.append('+');
		sb.append(name);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name, "direction",
				isDesc() ? "desc" : "asc");
		xmlWriter.endElement();
	}

}
