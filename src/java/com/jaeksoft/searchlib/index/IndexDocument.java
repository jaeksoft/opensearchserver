/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;

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
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 * @throws IOException
	 */
	public IndexDocument(ParserSelector parserSelector, XPathParser xpp,
			Node documentNode) throws XPathExpressionException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
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
				String textContent = xpp.getNodeString(valueNode);
				if (removeTag)
					textContent = StringUtils.removeTag(textContent);
				Float boost = XPathParser.getAttributeFloat(valueNode, "boost");
				add(fieldName, textContent, boost);
			}
		}
		NodeList binaryNodes = xpp.getNodeList(documentNode, "binary");
		int binaryCount = binaryNodes.getLength();
		for (int i = 0; i < binaryCount; i++) {
			Node node = binaryNodes.item(i);
			String filename = XPathParser.getAttributeString(node, "fileName");
			String contentType = XPathParser.getAttributeString(node,
					"contentType");
			Parser parser = parserSelector.getParser(filename, contentType);
			if (parser == null)
				continue;
			byte[] binaryDocument = Base64.decodeBase64(xpp.getNodeString(node)
					.getBytes());
			parser.parseContent(binaryDocument);
			parser.populate(this);
		}
	}

	public IndexDocument(IndexDocument sourceDocument) {
		this.lang = sourceDocument.lang;
		for (Map.Entry<String, FieldContent> entry : sourceDocument.fields
				.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	private FieldContent getFieldContent(String field) {
		FieldContent fc = fields.get(field);
		if (fc == null) {
			fc = new FieldContent(field);
			fields.put(field, fc);
		}
		return fc;
	}

	public void add(String field, FieldValueItem fieldValueItem) {
		FieldContent fc = getFieldContent(field);
		fc.add(fieldValueItem);
		fieldContentArray = null;
	}

	public void add(String field, String value, Float boost) {
		if (value == null || value.length() == 0)
			return;
		add(field, new FieldValueItem(value, boost));
	}

	public void addObject(String field, Object object) {
		if (object == null)
			return;
		addString(field, object.toString());
	}

	public void addString(String field, String value) {
		if (value == null)
			return;
		add(field, new FieldValueItem(value));
	}

	public void addFieldValueList(String field, List<FieldValueItem> values) {
		if (values == null)
			return;
		for (FieldValueItem value : values)
			add(field, value);
	}

	public void addObjectList(String field, List<Object> values) {
		if (values == null)
			return;
		for (Object value : values)
			addObject(field, value.toString());
	}

	public void addStringList(String field, List<String> values) {
		if (values == null)
			return;
		for (String value : values)
			addString(field, value);
	}

	public void add(String field, FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addFieldValueList(field, fieldContent.getValues());
	}

	private void addIfNotAlreadyHere(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		FieldContent fc = getFieldContent(fieldContent.getField());
		if (fc.checkIfAlreadyHere(fieldContent))
			return;
		fc.add(fieldContent);
	}

	public void addIfNotAlreadyHere(IndexDocument source) {
		for (FieldContent fc : source.fields.values())
			addIfNotAlreadyHere(fc);
	}

	public void add(Map<String, FieldValueItem> fieldMap) {
		for (Map.Entry<String, FieldValueItem> entry : fieldMap.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	public void add(IndexDocument source) {
		for (FieldContent fc : source.fields.values())
			add(fc.getField(), fc);
	}

	public void setString(String field, String value) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		add(field, value, null);
	}

	public void setStringList(String field, List<String> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addStringList(field, values);
	}

	public void setFieldValueItems(String field, List<FieldValueItem> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addFieldValueList(field, values);
	}

	public void setObjectList(String field, List<Object> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addObjectList(field, values);
	}

	public void setObject(String field, Object value) {
		setString(field, value.toString());
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

	public FieldValueItem getFieldValue(String fieldName, int pos) {
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

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
		lang = LanguageEnum.findByCode(External.readUTF(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(fields.values(), out);
		External.writeUTF(lang == null ? LanguageEnum.UNDEFINED.getCode()
				: lang.getCode(), out);
	}

	@Override
	public void addObject(FieldContent fieldContent) {
		fields.put(fieldContent.getField(), fieldContent);
		fieldContentArray = null;
	}

	@Override
	public Iterator<FieldContent> iterator() {
		return fields.values().iterator();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (fields != null) {
			for (String key : fields.keySet()) {
				FieldContent value = (FieldContent) fields.get(key);
				result.append(value.toString()).append("\n");
			}
		}
		return result.toString();
	}

}
