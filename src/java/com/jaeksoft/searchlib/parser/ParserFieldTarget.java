/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.TargetField;

public class ParserFieldTarget extends TargetField {

	private String captureRegexp;

	private boolean removeTag;

	private Pattern captureRegexpPattern;

	public ParserFieldTarget(String name, String captureRegexp,
			String analyzer, boolean removeTag) {
		super(name, analyzer);
		this.captureRegexp = captureRegexp;
		this.removeTag = removeTag;
		checkRegexpPattern();
	}

	public ParserFieldTarget(String name, Node node) {
		super(name, node);
		List<Node> nl = DomUtils.getNodes(node, "captureRegexp");
		if (nl.size() > 0)
			captureRegexp = StringEscapeUtils.unescapeXml(nl.get(0)
					.getTextContent());
		nl = DomUtils.getNodes(node, "removeTag");
		removeTag = nl.size() > 0;
		checkRegexpPattern();
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		if (captureRegexp != null) {
			xmlWriter.startElement("captureRegexp");
			xmlWriter.textNode(StringEscapeUtils.escapeXml(captureRegexp));
			xmlWriter.endElement();
		}
		if (removeTag) {
			xmlWriter.startElement("removeTag");
			xmlWriter.endElement();
		}
	}

	public final boolean hasRegexpPattern() {
		return (captureRegexp != null);
	}

	private void checkRegexpPattern() {
		if (captureRegexp != null)
			if (captureRegexp.trim().length() == 0)
				captureRegexp = null;
		captureRegexpPattern = captureRegexp == null ? null : Pattern
				.compile(captureRegexp);
	}

	/**
	 * @return the captureRegexp
	 */
	public String getCaptureRegexp() {
		return captureRegexp;
	}

	/**
	 * @param replaceRegexpTag
	 *            the replaceRegexpTag to set
	 */
	public void setCaptureRegexp(String captureRegexp) {
		this.captureRegexp = captureRegexp;
	}

	@Override
	final public void add(FieldValueItem[] fieldValueItems,
			IndexDocument targetDocument) throws IOException {
		if (fieldValueItems == null)
			return;
		List<String> values = new ArrayList<String>(0);
		if (captureRegexpPattern == null) {
			for (FieldValueItem fieldValueItem : fieldValueItems)
				values.add(fieldValueItem.getValue());
		} else {
			synchronized (captureRegexpPattern) {
				for (FieldValueItem fieldValueItem : fieldValueItems)
					for (String value : RegExpUtils.getGroups(
							captureRegexpPattern, fieldValueItem.getValue()))
						values.add(value);
			}
		}

		if (removeTag) {
			int pos = 0;
			for (String value : values)
				values.set(pos++, StringUtils.removeTag(value));
		}
		add(values, targetDocument);
	}

	/**
	 * @return the removeTag
	 */
	public boolean isRemoveTag() {
		return removeTag;
	}

	/**
	 * @param removeTag
	 *            the removeTag to set
	 */
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}
}
