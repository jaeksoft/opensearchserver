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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.XPathParser;

public class SchemaField extends FieldValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7179322665792749748L;

	private String defaultAnalyzer;

	private String highlightAnalyzer;

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
		this.highlightAnalyzer = field.highlightAnalyzer;
		this.indexAnalyzer = field.indexAnalyzer;
	}

	@Override
	public Object clone() {
		return new SchemaField(this);
	}

	public SchemaField(String name, String store, String index,
			String termVector, String defaultAnalyzer,
			String highlightAnalyzer, String indexAnalyzer) {
		super(name);
		this.store = org.apache.lucene.document.Field.Store.NO;
		if ("compress".equalsIgnoreCase(store))
			this.store = org.apache.lucene.document.Field.Store.COMPRESS;
		else if ("yes".equalsIgnoreCase(store))
			this.store = org.apache.lucene.document.Field.Store.YES;
		this.index = org.apache.lucene.document.Field.Index.NO;
		if ("yes".equalsIgnoreCase(index)) {
			if (defaultAnalyzer != null)
				this.index = org.apache.lucene.document.Field.Index.TOKENIZED;
			else
				this.index = org.apache.lucene.document.Field.Index.UN_TOKENIZED;
		}
		this.termVector = org.apache.lucene.document.Field.TermVector.NO;
		if ("yes".equalsIgnoreCase(termVector))
			this.termVector = org.apache.lucene.document.Field.TermVector.YES;
		else if ("offsets".equalsIgnoreCase(termVector))
			this.termVector = org.apache.lucene.document.Field.TermVector.WITH_OFFSETS;
		else if ("positions".equalsIgnoreCase(termVector))
			this.termVector = org.apache.lucene.document.Field.TermVector.WITH_POSITIONS;
		else if ("positions_offsets".equalsIgnoreCase(termVector))
			this.termVector = org.apache.lucene.document.Field.TermVector.WITH_POSITIONS_OFFSETS;
		this.defaultAnalyzer = defaultAnalyzer;
		this.highlightAnalyzer = highlightAnalyzer;
		this.indexAnalyzer = indexAnalyzer;
	}

	public org.apache.lucene.document.Field getLuceneField(String value) {
		try {
			return new org.apache.lucene.document.Field(name, value, store,
					index, termVector);
		} catch (java.lang.NullPointerException e) {
			System.err.println("Erreur on field " + name);
			throw e;
		}

	}

	@Override
	public String toString() {
		return this.getClass().getName() + "/" + this.name + "/";
	}

	@Override
	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.print("<field name=\"" + name + "\"");
		writer.print(" stored=\"" + store + "\"");
		writer.print(" indexed=\"" + index + "\"");
		writer.print(" termVector=\"" + termVector + "\"");
		if (defaultAnalyzer != null)
			writer.print(" defaultAnalyzer=\"" + defaultAnalyzer + "\"");
		if (highlightAnalyzer != null)
			writer.print(" highlightAnalyzer=\"" + highlightAnalyzer + "\"");
		writer.println("/>");
	}

	public String getDefaultAnalyzer() {
		return defaultAnalyzer;
	}

	public String getHighlightAnalyzer() {
		if (highlightAnalyzer == null)
			return defaultAnalyzer;
		return highlightAnalyzer;
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
	public static FieldList<SchemaField> fromXmlConfig(XPathParser xpp,
			Node parentNode) throws XPathExpressionException {
		FieldList<SchemaField> fieldList = new FieldList<SchemaField>();
		NodeList nodes = xpp.getNodeList(parentNode, "field");
		if (nodes == null)
			return fieldList;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = XPathParser.getAttributeString(node, "name");
			String analyzer = XPathParser.getAttributeString(node, "analyzer");
			String highlightAnalyzer = XPathParser.getAttributeString(node,
					"highlightAnalyzer");
			String indexAnalyzer = XPathParser.getAttributeString(node,
					"indexAnalyzer");
			String stored = XPathParser.getAttributeString(node, "stored");
			String indexed = XPathParser.getAttributeString(node, "indexed");
			String termVector = XPathParser.getAttributeString(node,
					"termVector");
			fieldList.add(new SchemaField(name, stored, indexed, termVector,
					analyzer, highlightAnalyzer, indexAnalyzer));
		}
		fieldList.setDefaultField(XPathParser.getAttributeString(parentNode,
				"default"));
		fieldList.setUniqueField(XPathParser.getAttributeString(parentNode,
				"unique"));
		return fieldList;
	}

}
