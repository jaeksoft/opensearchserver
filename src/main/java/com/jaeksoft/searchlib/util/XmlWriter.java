/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlWriter {

	private TransformerHandler transformerHandler;

	private AttributesImpl elementAttributes;

	private Stack<String> startedElementStack;

	private Matcher controlMatcher;

	public XmlWriter(PrintWriter out, String encoding) throws TransformerConfigurationException, SAXException {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		transformerHandler = tf.newTransformerHandler();
		Transformer serializer = transformerHandler.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformerHandler.setResult(streamResult);
		startedElementStack = new Stack<String>();
		transformerHandler.startDocument();
		elementAttributes = new AttributesImpl();
		Pattern p = Pattern.compile("\\p{Cntrl}");
		controlMatcher = p.matcher("");

	}

	public void textNode(Object data) throws SAXException {
		if (data == null)
			return;
		String value = data.toString();
		char[] chars = value.toCharArray();
		transformerHandler.characters(chars, 0, chars.length);
	}

	public void endDocument() throws SAXException {
		while (!startedElementStack.empty())
			endElement();
		transformerHandler.endDocument();
	}

	final public String escapeXml(String text) {
		if (text == null)
			return null;
		controlMatcher.reset(text);
		return StringEscapeUtils.escapeXml11(controlMatcher.replaceAll(""));
	}

	public void startElement(String name, String... attributes) throws SAXException {
		elementAttributes.clear();
		for (int i = 0; i < attributes.length; i++) {
			String attr = attributes[i];
			String value = escapeXml(attributes[++i]);
			if (attr != null && value != null)
				elementAttributes.addAttribute("", "", attr, "CDATA", value);
		}
		startedElementStack.push(name);
		transformerHandler.startElement("", "", name, elementAttributes);

	}

	public void endElement() throws SAXException {
		transformerHandler.endElement("", "", startedElementStack.pop());
	}

	public void writeSubTextNodeIfAny(String nodeName, String content) throws SAXException {
		if (content == null)
			return;
		startElement(nodeName);
		textNode(content);
		endElement();
	}

	public interface Interface {
		void writeXml(XmlWriter xmlWriter) throws SAXException, UnsupportedEncodingException;
	}
}
