/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SortField extends AbstractField<SortField> implements
		Comparable<SortField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3269790150800596793L;

	private int joinNumber;

	private boolean desc;

	private boolean nullFirst;

	private final static Pattern sortPattern = Pattern
			.compile("^([0-9]+)?([+-]{1})?(.*)(_\\$null\\$)?$");

	public SortField(final String requestSort) {
		super();
		Matcher matcher = RegExpUtils.matcher(sortPattern, requestSort);
		matcher.matches();
		joinNumber = matcher.group(1) == null ? 0 : Integer.parseInt(matcher
				.group(1));
		desc = matcher.group(2) == null ? false : "-".equals(matcher.group(2));
		name = matcher.group(3);
		nullFirst = matcher.group(4) != null;
	}

	public static void main(String[] args) {
		System.out.println(new SortField("0+fieldname_$null$"));
		System.out.println(new SortField("+fieldname_$null$"));
		System.out.println(new SortField("fieldname_$null$"));
		System.out.println(new SortField("-fieldname_$null$"));
		System.out.println(new SortField("0+fieldname"));
		System.out.println(new SortField("+fieldname"));
		System.out.println(new SortField("fieldname"));
		System.out.println(new SortField("-fieldname"));
	}

	public SortField(int joinNumber, String fieldName, boolean desc,
			boolean nullFirst) {
		super(fieldName);
		this.desc = desc;
		this.joinNumber = joinNumber;
		this.nullFirst = nullFirst;
	}

	public SortField(Node node) {
		super(DomUtils.getAttributeText(node, "name"));
		setDirection(DomUtils.getAttributeText(node, "direction"));
		setJoinNumber(DomUtils.getAttributeInteger(node, "joinNumber", 0));
		setNullFirst(DomUtils.getAttributeBoolean(node, "nullFirst", false));
	}

	public boolean isDesc() {
		return desc;
	}

	public String getDirection() {
		return desc ? "descending" : "ascending";
	}

	final static public String[] ASCENDING_ARRAYs = { "+", "asc", "ascendant",
			"ascending" };

	public void setDirection(String v) {
		for (String asc : ASCENDING_ARRAYs) {
			if (asc.equalsIgnoreCase(v)) {
				desc = false;
				return;
			}
		}
		desc = true;
	}

	@Override
	public SortField duplicate() {
		return new SortField(joinNumber, name, desc, nullFirst);
	}

	final public boolean isScore() {
		return name.equals("score");
	}

	final public boolean isDistance() {
		return name.equals("__distance__");
	}

	/**
	 * @return the joinNumber
	 */
	public int getJoinNumber() {
		return joinNumber;
	}

	/**
	 * @param joinNumber
	 *            the joinNumber to set
	 */
	public void setJoinNumber(int joinNumber) {
		this.joinNumber = joinNumber;
	}

	/**
	 * @return the nullFirst
	 */
	public boolean isNullFirst() {
		return nullFirst;
	}

	/**
	 * @param nullFirst
	 *            the nullFirst to set
	 */
	public void setNullFirst(boolean nullFirst) {
		this.nullFirst = nullFirst;
	}

	public SorterAbstract getSorter(final CollectorInterface collector,
			final ReaderAbstract reader) throws IOException {
		if (isScore()) {
			if (desc)
				return new DescScoreSorter(collector);
			else
				return new AscScoreSorter(collector);
		}
		if (joinNumber == 0) {
			if (desc)
				return new DescStringIndexSorter(collector,
						reader.getStringIndex(name), nullFirst);
			else
				return new AscStringIndexSorter(collector,
						reader.getStringIndex(name), nullFirst);
		} else {
			if (desc)
				return new DescJoinStringIndexSorter(collector, joinNumber - 1,
						name, nullFirst);
			else
				return new AscJoinStringIndexSorter(collector, joinNumber - 1,
						name, nullFirst);
		}
	}

	@Override
	final public int compareTo(final SortField o) {
		int c;
		if ((c = super.compareTo(o)) != 0)
			return c;
		if ((c = joinNumber - o.joinNumber) != 0)
			return c;
		if (desc == o.desc)
			return 0;
		return desc ? -1 : 1;
	}

	@Override
	final public void toString(final StringBuilder sb) {
		if (joinNumber > 0)
			sb.append(joinNumber);
		if (desc)
			sb.append('-');
		else
			sb.append('+');
		sb.append(name);
		if (nullFirst)
			sb.append("_$null$");
	}

	@Override
	final public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name, "direction",
				isDesc() ? "desc" : "asc", "joinNumber",
				Integer.toString(joinNumber), "nullFirst",
				Boolean.toString(nullFirst));
		xmlWriter.endElement();
	}

}
