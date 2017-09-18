/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class SchemaField extends AbstractField<SchemaField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2134622919973613819L;

	private String indexAnalyzer;

	private List<String> copyOf;

	private Stored stored;

	private Indexed indexed;

	private TermVector termVector;

	public SchemaField() {
		super("");
		indexAnalyzer = null;
		stored = Stored.NO;
		indexed = Indexed.YES;
		termVector = TermVector.NO;
		copyOf = null;
	}

	public SchemaField(String name, Stored stored, Indexed indexed,
			TermVector termVector, String analyzer, String... copyOf) {
		super(name);
		this.indexAnalyzer = analyzer == null ? null : analyzer.intern();
		this.stored = stored;
		this.indexed = indexed;
		this.termVector = termVector;
		this.copyOf = copyOf != null && copyOf.length > 0 ? Arrays
				.asList(copyOf) : null;
	}

	public SchemaField(SchemaField field) {
		super();
		copyFrom(field);
	}

	@Override
	public void copyFrom(SchemaField sourceField) {
		super.copyFrom(sourceField);
		SchemaField sc = (SchemaField) sourceField;
		this.stored = sc.stored;
		this.indexed = sc.indexed;
		this.termVector = sc.termVector;
		this.indexAnalyzer = sc.indexAnalyzer;
		this.copyOf = sc.copyOf != null && sc.copyOf.size() > 0 ? new ArrayList<String>(
				sc.copyOf) : null;
	}

	private SchemaField(String name, String stored, String indexed,
			String termVector, String indexAnalyzer) {
		this(name, Stored.fromValue(stored), Indexed.fromValue(indexed),
				TermVector.fromValue(termVector), indexAnalyzer);
	}

	private SchemaField(String name, String stored, String indexed,
			String termVector, String indexAnalyzer, String... copyOf) {
		this(name, stored, indexed, termVector, indexAnalyzer);
		this.copyOf = copyOf != null && copyOf.length > 0 ? Arrays
				.asList(copyOf) : null;
	}

	private SchemaField(String name, String stored, String indexed,
			String termVector, String indexAnalyzer, List<String> copyOf) {
		this(name, stored, indexed, termVector, indexAnalyzer);
		this.copyOf = copyOf != null && copyOf.size() > 0 ? new ArrayList<String>(
				copyOf) : null;
	}

	final public org.apache.lucene.document.Field getLuceneField(String value,
			Float boost) {
		try {
			org.apache.lucene.document.Field field = new org.apache.lucene.document.Field(
					name, value, stored.luceneStore,
					indexed.getLuceneIndex(indexAnalyzer),
					termVector.luceneTermVector);
			if (boost != null)
				field.setBoost(boost);
			return field;
		} catch (java.lang.NullPointerException e) {
			throw new NullPointerException("Error on field " + name);
		}
	}

	@Override
	public SchemaField duplicate() {
		return new SchemaField(this);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "/" + this.name + "/";
	}

	public boolean checkStored(Stored... storedList) {
		for (Stored st : storedList)
			if (stored == st)
				return true;
		return false;
	}

	public boolean checkIndexed(Indexed... indexedList) {
		for (Indexed idx : indexedList)
			if (indexed == idx)
				return true;
		return false;
	}

	public Stored getStored() {
		return stored;
	}

	public void setStored(Stored stored) {
		this.stored = stored;
	}

	public Indexed getIndexed() {
		return indexed;
	}

	public void setIndexed(Indexed indexed) {
		this.indexed = indexed;
	}

	public TermVector getTermVector() {
		return termVector;
	}

	public void setTermVector(TermVector termVector) {
		this.termVector = termVector;
	}

	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public void setIndexAnalyzer(String indexAnalyzer) {
		this.indexAnalyzer = StringUtils.isEmpty(indexAnalyzer) ? null
				: indexAnalyzer.intern();
	}

	/**
	 * Construit une liste de champs bas� sur le fichier de config XML
	 * 
	 * @param xpp
	 * @param parentNode
	 * @throws XPathExpressionException
	 * @throws DOMException
	 */
	public static SchemaFieldList fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		SchemaFieldList fieldList = new SchemaFieldList();
		if (parentNode == null)
			return fieldList;
		NodeList nodes = xpp.getNodeList(parentNode, "field");
		if (nodes == null)
			return fieldList;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = XPathParser.getAttributeString(node, "name");
			String indexAnalyzer = XPathParser.getAttributeString(node,
					"analyzer");
			String stored = XPathParser.getAttributeString(node, "stored");
			String indexed = XPathParser.getAttributeString(node, "indexed");
			String termVector = XPathParser.getAttributeString(node,
					"termVector");
			List<String> copyOfList = null;
			List<Node> nodeList = DomUtils.getNodes(node, "copyOf");
			if (nodeList != null) {
				copyOfList = new ArrayList<String>(nodeList.size());
				for (Node no : nodeList)
					copyOfList.add(DomUtils.getAttributeText(no, "field"));
			}
			fieldList.put(new SchemaField(name, stored, indexed, termVector,
					indexAnalyzer, copyOfList));
		}
		fieldList.setDefaultField(XPathParser.getAttributeString(parentNode,
				"default"));
		fieldList.setUniqueField(XPathParser.getAttributeString(parentNode,
				"unique"));
		return fieldList;
	}

	/**
	 * @return the copyOf
	 */
	public List<String> getCopyOf() {
		return copyOf;
	}

	public void addCopyOf(String field) {
		if (field == null)
			return;
		if (field.length() == 0)
			return;
		if (copyOf == null)
			copyOf = new ArrayList<String>(1);
		copyOf.add(field);
	}

	public void removeCopyOf(String field) {
		if (copyOf == null)
			return;
		copyOf.remove(field);
		if (copyOf.size() == 0)
			copyOf = null;
	}

	/**
	 * @param copyOf
	 *            the copyOf to set
	 */
	public void setCopyOf(List<String> copyOf) {
		if (CollectionUtils.isEmpty(copyOf)) {
			this.copyOf = null;
		} else {
			this.copyOf = new ArrayList<String>(copyOf.size());
			for (String cf : copyOf)
				this.copyOf.add(cf.intern());
			setStored(Stored.NO);
		}
	}

	public static SchemaField fromHttpRequest(ServletTransaction transaction)
			throws SearchLibException {
		String name = transaction.getParameterString("field.name");
		if (name == null)
			throw new SearchLibException("No field name");
		String indexAnalyzer = transaction.getParameterString("field.analyzer");
		String stored = transaction.getParameterString("field.stored");
		String indexed = transaction.getParameterString("field.indexed");
		String termVector = transaction.getParameterString("field.termVector");
		String[] copyOf = transaction.getParameterValues("field.copyOf");
		return new SchemaField(name, stored, indexed, termVector,
				indexAnalyzer, copyOf);
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("field", "name", name, "analyzer", indexAnalyzer,
				"indexed", getIndexed().getValue(), "stored", getStored()
						.getValue(), "termVector", getTermVector().getValue());
		if (copyOf != null) {
			for (String f : copyOf) {
				writer.startElement("copyOf", "field", f);
				writer.endElement();
			}
		}
		writer.endElement();
	}

	public boolean valid() throws SearchLibException {
		if (name == null || name.trim().length() == 0)
			throw new SearchLibException("Field name cannot be empty!");
		if (termVector != TermVector.NO && indexed != Indexed.YES)
			throw new SearchLibException(
					"TermVector request Indexed to be set to yes!");
		return true;
	}

}
