/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SchemaField extends Field {

	private String defaultAnalyzer;

	private String indexAnalyzer;

	private org.apache.lucene.document.Field.Store store;

	private org.apache.lucene.document.Field.Index index;

	private org.apache.lucene.document.Field.TermVector termVector;

	public SchemaField(SchemaField field) {
		super(field.name);
		this.store = field.store;
		this.index = field.index;
		this.termVector = field.termVector;
		this.defaultAnalyzer = field.defaultAnalyzer;
		this.indexAnalyzer = field.indexAnalyzer;
	}

	public SchemaField(String name, String store, String index,
			String termVector, String defaultAnalyzer, String indexAnalyzer) {
		super(name);
		this.store = org.apache.lucene.document.Field.Store.NO;
		if ("compress".equalsIgnoreCase(store))
			this.store = Store.COMPRESS;
		else if ("yes".equalsIgnoreCase(store))
			this.store = Store.YES;
		this.index = org.apache.lucene.document.Field.Index.NO;
		if ("yes".equalsIgnoreCase(index)) {
			if (defaultAnalyzer != null)
				this.index = Index.ANALYZED;
			else
				this.index = Index.NOT_ANALYZED;
		}
		this.termVector = TermVector.NO;
		if ("yes".equalsIgnoreCase(termVector))
			this.termVector = TermVector.YES;
		else if ("offsets".equalsIgnoreCase(termVector))
			this.termVector = TermVector.WITH_OFFSETS;
		else if ("positions".equalsIgnoreCase(termVector))
			this.termVector = TermVector.WITH_POSITIONS;
		else if ("positions_offsets".equalsIgnoreCase(termVector))
			this.termVector = TermVector.WITH_POSITIONS_OFFSETS;
		this.defaultAnalyzer = defaultAnalyzer;
		this.indexAnalyzer = indexAnalyzer;
	}

	public org.apache.lucene.document.Field getLuceneField(String value) {
		try {
			return new org.apache.lucene.document.Field(name, value, store,
					index, termVector);
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
		return store == Store.YES || store == Store.COMPRESS;
	}

	public boolean isCompressed() {
		return store == Store.COMPRESS;
	}

	public boolean isIndexed() {
		return index == Index.ANALYZED || index == Index.NOT_ANALYZED;
	}

	public String getStoreLabel() {
		if (store == Store.NO)
			return "no";
		if (store == Store.YES)
			return "yes";
		if (store == Store.COMPRESS)
			return "compress";
		return null;
	}

	public String getIndexLabel() {
		if (index == org.apache.lucene.document.Field.Index.NO)
			return "no";
		if (index == org.apache.lucene.document.Field.Index.ANALYZED)
			return "yes";
		if (index == org.apache.lucene.document.Field.Index.NOT_ANALYZED)
			return "yes";
		return null;
	}

	public String getTermVectorLabel() {
		if (termVector == TermVector.NO)
			return "no";
		if (termVector == TermVector.YES)
			return "yes";
		if (termVector == TermVector.WITH_OFFSETS)
			return "offsets";
		if (termVector == TermVector.WITH_POSITIONS)
			return "positions";
		if (termVector == TermVector.WITH_POSITIONS_OFFSETS)
			return "positions_offsets";
		return null;
	}

	public String getDefaultAnalyzer() {
		return defaultAnalyzer;
	}

	public String getIndexAnalyzer() {
		if (indexAnalyzer == null)
			return defaultAnalyzer;
		return indexAnalyzer;
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
			String analyzer = XPathParser.getAttributeString(node, "analyzer");
			String indexAnalyzer = XPathParser.getAttributeString(node,
					"indexAnalyzer");
			String stored = XPathParser.getAttributeString(node, "stored");
			String indexed = XPathParser.getAttributeString(node, "indexed");
			String termVector = XPathParser.getAttributeString(node,
					"termVector");
			fieldList.add(new SchemaField(name, stored, indexed, termVector,
					analyzer, indexAnalyzer));
		}
		fieldList.setDefaultField(XPathParser.getAttributeString(parentNode,
				"default"));
		fieldList.setUniqueField(XPathParser.getAttributeString(parentNode,
				"unique"));
		return fieldList;
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("field", "name", name, "analyzer",
				indexAnalyzer != defaultAnalyzer ? indexAnalyzer : null,
				"indexed", getIndexLabel(), "stored", getStoreLabel(),
				"termVector", getTermVectorLabel());
		writer.endElement();
	}
}
