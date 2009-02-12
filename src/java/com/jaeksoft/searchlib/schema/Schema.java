/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Schema implements XmlInfo {

	private FieldList<SchemaField> fieldList;

	private AnalyzerList analyzers;

	private HashMap<String, PerFieldAnalyzerWrapper> langQueryAnalyzers;

	private Schema() {
		fieldList = null;
		analyzers = null;
		langQueryAnalyzers = new HashMap<String, PerFieldAnalyzerWrapper>();
	}

	public static Schema fromXmlConfig(Node parentNode, XPathParser xpp)
			throws XPathExpressionException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, DOMException,
			IOException {

		Schema schema = new Schema();

		schema.analyzers = AnalyzerList.fromXmlConfig(xpp, xpp.getNode(
				parentNode, "analyzers"));

		schema.fieldList = SchemaField.fromXmlConfig(xpp, xpp.getNode(
				parentNode, "fields"));

		return schema;
	}

	public AnalyzerList getAnalyzerList() {
		return analyzers;
	}

	public FieldList<SchemaField> getFieldList() {
		return fieldList;
	}

	public PerFieldAnalyzerWrapper getQueryPerFieldAnalyzer(String lang) {
		synchronized (langQueryAnalyzers) {
			PerFieldAnalyzerWrapper pfa = langQueryAnalyzers.get(lang);
			if (pfa != null)
				return pfa;
			pfa = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
			for (SchemaField field : this.fieldList) {
				Analyzer analyzer = analyzers.get(field.getDefaultAnalyzer(),
						lang);
				if (analyzer == null)
					analyzer = analyzers.get(field.getDefaultAnalyzer(), null);
				if (analyzer != null)
					pfa.addAnalyzer(field.name, analyzer);
			}
			langQueryAnalyzers.put(lang, pfa);
			return pfa;
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<schema>");
		analyzers.xmlInfo(writer, classDetail);
		fieldList.xmlInfo(writer, classDetail);
		writer.println("</schema>");

	}

}
