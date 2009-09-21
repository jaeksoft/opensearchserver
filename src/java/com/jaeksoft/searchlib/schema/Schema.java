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
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Schema {

	private SchemaFieldList fieldList;

	private AnalyzerList analyzers;

	private Map<String, PerFieldAnalyzerWrapper> langQueryAnalyzers;

	private Schema() {
		fieldList = null;
		analyzers = null;
		langQueryAnalyzers = new TreeMap<String, PerFieldAnalyzerWrapper>();
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

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("schema");
		analyzers.writeXmlConfig(writer);
		fieldList.writeXmlConfig(writer);
		writer.endElement();
	}

	public AnalyzerList getAnalyzerList() {
		return analyzers;
	}

	public SchemaFieldList getFieldList() {
		return fieldList;
	}

	public PerFieldAnalyzerWrapper getQueryPerFieldAnalyzer(LanguageEnum lang) {
		synchronized (langQueryAnalyzers) {
			if (lang == null)
				lang = LanguageEnum.UNDEFINED;
			PerFieldAnalyzerWrapper pfa = langQueryAnalyzers
					.get(lang.getCode());
			if (pfa != null)
				return pfa;
			pfa = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
			for (SchemaField field : fieldList) {
				Analyzer analyzer = null;
				String analyzerName = field.getIndexAnalyzer();
				if (analyzerName != null) {
					analyzer = analyzers.get(field.getIndexAnalyzer(), lang);
					if (analyzer == null)
						analyzer = analyzers
								.get(field.getIndexAnalyzer(), null);
				}
				if (analyzer != null)
					pfa.addAnalyzer(field.name, analyzer);
			}
			langQueryAnalyzers.put(lang.getCode(), pfa);
			return pfa;
		}
	}
}
