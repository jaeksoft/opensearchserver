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

package com.jaeksoft.searchlib.index;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class IndexDocument implements Serializable, XmlInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3144413917081822065L;

	private HashMap<String, FieldContent> fields;
	private String lang;

	public IndexDocument() {
		fields = new HashMap<String, FieldContent>();
		this.lang = null;
	}

	public IndexDocument(String lang) {
		this();
		this.lang = lang;
	}

	public IndexDocument(Locale lang) {
		this();
		if (lang != null)
			this.lang = lang.getLanguage();
	}

	/**
	 * Create a new instance of IndexDocument from an XML structure <br/>
	 * <field name="FIELDNAME"><br/>
	 * &nbsp;&nbsp;<value>VALUE1</value><br/>
	 * &nbsp;&nbsp;<value>VALUE2</value><br/>
	 * </field>
	 * 
	 * @param xpp
	 * @param documentNode
	 * @throws XPathExpressionException
	 */
	public IndexDocument(XPathParser xpp, Node documentNode)
			throws XPathExpressionException {
		this(XPathParser.getAttributeString(documentNode, "lang"));
		NodeList fieldNodes = xpp.getNodeList(documentNode, "field");
		int fieldsCount = fieldNodes.getLength();
		for (int i = 0; i < fieldsCount; i++) {
			Node fieldNode = fieldNodes.item(i);
			String fieldName = XPathParser
					.getAttributeString(fieldNode, "name");
			NodeList valueNodes = xpp.getNodeList(fieldNode, "value");
			int valuesCount = valueNodes.getLength();
			for (int j = 0; j < valuesCount; j++)
				add(fieldName, xpp.getNodeString(valueNodes.item(j)));
		}
	}

	public void add(String field, String value) {
		if (value == null)
			throw new java.lang.NullPointerException("Null value on field "
					+ field);
		FieldContent fc = fields.get(field);
		if (fc == null) {
			fc = new FieldContent(field);
			fields.put(field, fc);
		}
		fc.add(value);
	}

	public void add(String field, Object value) {
		if (value == null)
			throw new java.lang.NullPointerException("Null value on field "
					+ field);
		add(field, value.toString());
	}

	protected void set(FieldContent fieldContent) {
		fields.put(fieldContent.getField(), fieldContent);
	}

	public void set(String field, String value) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		add(field, value);
	}

	public void set(String field, Object value) {
		set(field, value.toString());
	}

	public String getLang() {
		return lang;
	}

	public FieldContent getField(String fieldName) {
		return fields.get(fieldName);
	}

	public String getFieldValue(String fieldName, int pos) {
		if (fields == null)
			return null;
		FieldContent fc = fields.get(fieldName);
		if (fc == null)
			return null;
		return fc.getValue(pos);
	}

	public Collection<FieldContent> getFields() {
		return fields.values();
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.print("<document>");
		for (FieldContent field : fields.values())
			field.xmlInfo(writer, classDetail);
		writer.println("</document>");

	}

}
