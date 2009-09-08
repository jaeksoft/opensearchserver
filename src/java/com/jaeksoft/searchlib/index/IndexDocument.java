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

package com.jaeksoft.searchlib.index;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.basket.BasketCache;
import com.jaeksoft.searchlib.basket.BasketKey;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.External.Collecter;

public class IndexDocument implements Externalizable, Collecter<FieldContent>,
		Iterable<FieldContent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3144413917081822065L;

	private Map<String, FieldContent> fields;
	private LanguageEnum lang;
	private FieldContent[] fieldContentArray;

	public IndexDocument() {
		fields = new TreeMap<String, FieldContent>();
		this.lang = null;
		fieldContentArray = null;
	}

	public IndexDocument(LanguageEnum lang) {
		this();
		this.lang = lang;
	}

	public IndexDocument(Locale lang) {
		this();
		if (lang != null)
			this.lang = LanguageEnum.findByCode(lang.getLanguage());
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
	 * @throws SearchLibException
	 */
	public IndexDocument(XPathParser xpp, Node documentNode,
			BasketCache basketCache) throws XPathExpressionException,
			SearchLibException {
		this(LanguageEnum.findByCode(XPathParser.getAttributeString(
				documentNode, "lang")));
		NodeList fieldNodes = xpp.getNodeList(documentNode, "field");
		int fieldsCount = fieldNodes.getLength();
		for (int i = 0; i < fieldsCount; i++) {
			Node fieldNode = fieldNodes.item(i);
			String fieldName = XPathParser
					.getAttributeString(fieldNode, "name");
			NodeList valueNodes = xpp.getNodeList(fieldNode, "value");
			int valuesCount = valueNodes.getLength();
			for (int j = 0; j < valuesCount; j++) {
				Node valueNode = valueNodes.item(j);
				boolean removeTag = "yes".equalsIgnoreCase(XPathParser
						.getAttributeString(valueNode, "removeTag"));
				long basketId = XPathParser.getAttributeLong(valueNode,
						"basketId");
				if (basketId != 0) {
					IndexDocument basketDocument = basketCache
							.get(new BasketKey(basketId));
					if (basketDocument == null)
						throw new SearchLibException(
								"No basket document found: " + basketId);
					String basketField = XPathParser.getAttributeString(
							valueNode, "basketField");
					if (basketField == null) {
						for (FieldContent fieldContent : basketDocument)
							add(fieldName, fieldContent);
					} else
						add(fieldName, basketDocument.getField(basketField));
				}
				String textContent = xpp.getNodeString(valueNode);
				if (removeTag)
					textContent = textContent.replaceAll("<[^>]*>", "");
				add(fieldName, textContent);
			}
		}
	}

	public IndexDocument(IndexDocument sourceDocument) {
		this.lang = sourceDocument.lang;
		for (Map.Entry<String, FieldContent> entry : sourceDocument.fields
				.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	private void addField(String field, String value) {
		FieldContent fc = fields.get(field);
		if (fc == null) {
			fc = new FieldContent(field);
			fields.put(field, fc);
		}
		fc.add(value);
		fieldContentArray = null;
	}

	public void addEmptyField(String field) {
		addField(field, "");
	}

	public void add(String field, String value) {
		if (value.length() == 0)
			return;
		addField(field, value);
	}

	public void add(String field, List<String> values) {
		if (values == null)
			return;
		for (String value : values)
			add(field, value);
	}

	public void add(String field, FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		add(field, fieldContent.getValues());
	}

	public void add(String field, Object value) {
		if (value == null)
			throw new java.lang.NullPointerException("Null value on field "
					+ field);
		add(field, value.toString());
	}

	public void add(Map<?, ?> fieldMap) {
		for (Map.Entry<?, ?> entry : fieldMap.entrySet())
			add(entry.getKey().toString(), entry.getValue());
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

	public LanguageEnum getLang() {
		return lang;
	}

	public void setLang(LanguageEnum lang) {
		this.lang = lang;
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

	public FieldContent[] getFieldContentArray() {
		if (fieldContentArray != null)
			return fieldContentArray;
		fieldContentArray = new FieldContent[fields.size()];
		fields.values().toArray(fieldContentArray);
		return fieldContentArray;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
		lang = LanguageEnum.findByCode(External.readUTF(in));
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(fields.values(), out);
		External.writeUTF(lang.getCode(), out);
	}

	public void addObject(FieldContent fieldContent) {
		fields.put(fieldContent.getField(), fieldContent);
		fieldContentArray = null;
	}

	public Iterator<FieldContent> iterator() {
		return fields.values().iterator();
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (fields != null) {
			for (String key : fields.keySet()) {
				FieldContent value = (FieldContent) fields.get(key);
				result.append("key : ").append(key).append("  -- value :")
						.append(value.toString()).append("\n");

			}
		}

		return result.toString();
	}

}
