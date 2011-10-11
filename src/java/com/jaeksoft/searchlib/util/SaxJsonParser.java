/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author naveen
 * 
 */
public class SaxJsonParser extends DefaultHandler {

	public static final String JSON_VALUE = "value";

	JSONObject result;
	List<JSONObject> stack;

	public SaxJsonParser() {
	}

	public JSONObject getJson() {
		return result;
	}

	public String attributeName(String name) {
		return name;
	}

	public void startDocument() throws SAXException {
		stack = new ArrayList<JSONObject>();
		stack.add(0, new JSONObject());
	}

	public void endDocument() throws SAXException {
		result = stack.remove(0);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		JSONObject work = new JSONObject();
		for (int ix = 0; ix < attributes.getLength(); ix++)
			work.put(attributeName(attributes.getLocalName(ix)),
					attributes.getValue(ix));
		stack.add(0, work);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		JSONObject pop = stack.remove(0);
		Object stashable = pop;
		if (pop.containsKey(JSON_VALUE)) {
			String value = pop.getString(JSON_VALUE).trim();
			if (pop.keySet().size() == 1)
				stashable = value;
			else if (StringUtils.isBlank(value))
				pop.remove(JSON_VALUE);
		}
		JSONObject parent = stack.get(0);
		if (!parent.containsKey(localName)) {
			parent.put(localName, stashable);
		} else {
			Object work = parent.get(localName);
			if (work instanceof JSONArray) {
				((JSONArray) work).add(stashable);
			} else {
				parent.put(localName, new JSONArray());
				parent.getJSONArray(localName).add(work);
				parent.getJSONArray(localName).add(stashable);
			}
		}
	}

	public void characters(char ch[], int start, int length)
			throws SAXException {
		JSONObject work = stack.get(0);
		String value = (work.containsKey(JSON_VALUE) ? work
				.getString(JSON_VALUE) : "");
		work.put(JSON_VALUE, value + new String(ch, start, length));
	}

	public void warning(SAXParseException e) throws SAXException {
		System.out.println("warning  e=" + e.getMessage());
	}

	public void error(SAXParseException e) throws SAXException {
		System.err.println("error  e=" + e.getMessage());
	}

	public void fatalError(SAXParseException e) throws SAXException {
		System.err.println("fatalError  e=" + e.getMessage());
		throw e;
	}
}