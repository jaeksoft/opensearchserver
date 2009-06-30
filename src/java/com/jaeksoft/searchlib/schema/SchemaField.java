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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SchemaField extends Field {

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
		super(field.name);
		this.stored = field.stored;
		this.indexed = field.indexed;
		this.termVector = field.termVector;
		this.indexAnalyzer = field.indexAnalyzer;
	}

	private SchemaField(String name, String stored, String indexed,
			String termVector, String indexAnalyzer) {
		super(name);
		this.indexAnalyzer = indexAnalyzer;
		this.stored = Stored.fromValue(stored);
		this.indexed = Indexed.fromValue(indexed);
		this.termVector = TermVector.fromValue(termVector);
	}

	public org.apache.lucene.document.Field getLuceneField(String value) {
		try {
			return new org.apache.lucene.document.Field(name, value, stored
					.getLuceneStore(), indexed.getLuceneIndex(indexAnalyzer),
					termVector.getLuceneTermVector());
		} catch (java.lang.NullPointerException e) {
			throw new NullPointerException("Erreur on field " + name);
		}
	}

	@Override
	public Field duplicate() {
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
			fieldList.add(new SchemaField(name, stored, indexed, termVector,
					indexAnalyzer));
		}
		fieldList.setDefaultField(XPathParser.getAttributeString(parentNode,
				"default"));
		fieldList.setUniqueField(XPathParser.getAttributeString(parentNode,
				"unique"));
		return fieldList;
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("field", "name", name, "analyzer", indexAnalyzer,
				"indexed", getIndexed().getValue(), "stored", getStored()
						.getValue(), "termVector", getTermVector().getValue());
		writer.endElement();
	}
}
