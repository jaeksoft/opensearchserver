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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class SchemaField extends AbstractField<SchemaField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2134622919973613819L;

	private String indexAnalyzer;

	private Stored stored;

	private Indexed indexed;

	private TermVector termVector;

	public SchemaField() {
		super("");
		indexAnalyzer = null;
		stored = Stored.NO;
		indexed = Indexed.YES;
		termVector = TermVector.NO;
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
	}

	private SchemaField(String name, String stored, String indexed,
			String termVector, String indexAnalyzer) {
		super(name);
		this.indexAnalyzer = indexAnalyzer;
		this.stored = Stored.fromValue(stored);
		this.indexed = Indexed.fromValue(indexed);
		this.termVector = TermVector.fromValue(termVector);
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
			throw new NullPointerException("Erreur on field " + name);
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

	public boolean isStored() {
		return stored == Stored.YES || stored == Stored.COMPRESS;
	}

	public boolean isCompressed() {
		return stored == Stored.COMPRESS;
	}

	public boolean isIndexed() {
		return indexed == Indexed.YES;
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
		if (indexAnalyzer != null)
			if (indexAnalyzer.length() == 0)
				indexAnalyzer = null;
		this.indexAnalyzer = indexAnalyzer;
	}

	/**
	 * Construit une liste de champs bas� sur le fichier de config XML
	 * 
	 * @param analyzers
	 * @param document
	 * @param xPath
	 * @throws XPathExpressionException
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws IOException
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
			fieldList.put(new SchemaField(name, stored, indexed, termVector,
					indexAnalyzer));
		}
		fieldList.setDefaultField(XPathParser.getAttributeString(parentNode,
				"default"));
		fieldList.setUniqueField(XPathParser.getAttributeString(parentNode,
				"unique"));
		return fieldList;
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
		return new SchemaField(name, stored, indexed, termVector, indexAnalyzer);
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("field", "name", name, "analyzer", indexAnalyzer,
				"indexed", getIndexed().getValue(), "stored", getStored()
						.getValue(), "termVector", getTermVector().getValue());
		writer.endElement();
	}

	public boolean valid() throws SearchLibException {
		if (name == null || name.trim().length() == 0)
			throw new SearchLibException("Field name cannot be empty!");
		if (termVector != TermVector.NO && !isIndexed())
			throw new SearchLibException(
					"TermVector request Indexed to be set to yes!");
		return true;
	}

}
