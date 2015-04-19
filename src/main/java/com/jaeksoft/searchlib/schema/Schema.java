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

import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Schema {

	private Config config;

	private SchemaFieldList fieldList;

	private AnalyzerList analyzers;

	private Map<String, PerFieldAnalyzer> langQueryAnalyzers;

	private Map<String, PerFieldAnalyzer> langIndexAnalyzers;

	private ReadWriteLock rwl = new ReadWriteLock();

	private Schema(Config config) {
		this.config = config;
		fieldList = null;
		analyzers = null;
		langQueryAnalyzers = new TreeMap<String, PerFieldAnalyzer>();
		langIndexAnalyzers = new TreeMap<String, PerFieldAnalyzer>();
	}

	public static Schema fromXmlConfig(Config config, Node parentNode,
			XPathParser xpp) throws XPathExpressionException,
			SearchLibException, DOMException, ClassNotFoundException {

		Schema schema = new Schema(config);

		schema.analyzers = AnalyzerList.fromXmlConfig(config, xpp,
				xpp.getNode(parentNode, "analyzers"));

		schema.fieldList = SchemaField.fromXmlConfig(xpp,
				xpp.getNode(parentNode, "fields"));

		return schema;
	}

	/**
	 * Create or update an existing field.
	 * 
	 * @param name
	 *            The name of the field
	 * @param stored
	 *            The storage status
	 * @param indexed
	 *            The indexed status
	 * @param termVector
	 *            The vector status
	 * @param analyzer
	 *            The name of an analyzer
	 * @param copyOf
	 *            A list of fields
	 * @throws SearchLibException
	 */
	public void setField(String name, Stored stored, Indexed indexed,
			TermVector termVector, String analyzer, String... copyOf)
			throws SearchLibException {
		getFieldList().put(
				new SchemaField(name, stored, indexed, termVector, analyzer,
						copyOf));
		config.saveConfig();
	}

	/**
	 * Set the default and the unique field.
	 * 
	 * @param defaultField
	 *            The name of the default field
	 * @param uniqueField
	 *            The name of the unique field
	 * @throws SearchLibException
	 */
	public void setDefaultUniqueField(String defaultField, String uniqueField)
			throws SearchLibException {
		SchemaFieldList schemaFieldList = getFieldList();
		schemaFieldList.setDefaultField(defaultField);
		schemaFieldList.setUniqueField(uniqueField);
		config.saveConfig();
	}

	/**
	 * The unique field is the primary key of an index
	 * 
	 * @return the name of the unique field
	 */
	public String getUniqueField() {
		SchemaField field = getFieldList().getUniqueField();
		return field == null ? null : field.getName();
	}

	/**
	 * @return the name of the default field
	 */
	public String getDefaultField() {
		SchemaField field = getFieldList().getDefaultField();
		return field == null ? null : field.getName();
	}

	/**
	 * Remove the field. No error is thrown if the field does not exist.
	 * 
	 * @param fieldName
	 *            The name of the field
	 * @throws SearchLibException
	 */
	public void removeField(String fieldName) throws SearchLibException {
		SchemaFieldList schemaFieldList = getFieldList();
		schemaFieldList.remove(fieldName);
		config.saveConfig();
	}

	/**
	 * Retrieve a copy of a field instance.
	 * 
	 * @param fieldName
	 * @return
	 */
	public SchemaField getField(String fieldName) {
		SchemaFieldList schemaFieldList = getFieldList();
		SchemaField field = schemaFieldList.get(fieldName);
		return field == null ? null : new SchemaField(field);
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		rwl.r.lock();
		try {
			writer.startElement("schema");
			analyzers.writeXmlConfig(writer);
			fieldList.writeXmlConfig(writer);
			writer.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public AnalyzerList getAnalyzerList() {
		return analyzers;
	}

	public SchemaFieldList getFieldList() {
		return fieldList;
	}

	public void recompileAnalyzers() {
		rwl.w.lock();
		try {
			analyzers.recompile();
			langQueryAnalyzers.clear();
			langIndexAnalyzers.clear();
		} finally {
			rwl.w.unlock();
		}
	}

	public Analyzer getAnalyzer(SchemaField schemaField, LanguageEnum lang) {
		rwl.r.lock();
		try {
			String analyzerName = schemaField.getIndexAnalyzer();
			if (analyzerName == null)
				return null;
			if (analyzers == null)
				return null;
			Analyzer analyzer = analyzers.get(analyzerName, lang);
			if (analyzer == null)
				analyzer = analyzers.get(analyzerName, null);
			return analyzer;
		} finally {
			rwl.r.unlock();
		}
	}

	public PerFieldAnalyzer getQueryPerFieldAnalyzer(LanguageEnum lang)
			throws SearchLibException {
		if (lang == null)
			lang = LanguageEnum.UNDEFINED;
		rwl.r.lock();
		try {
			PerFieldAnalyzer pfa = langQueryAnalyzers.get(lang.getCode());
			if (pfa != null)
				return pfa;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			PerFieldAnalyzer pfa = langQueryAnalyzers.get(lang.getCode());
			if (pfa != null)
				return pfa;
			Map<String, CompiledAnalyzer> mapField = new TreeMap<String, CompiledAnalyzer>();
			for (SchemaField field : fieldList) {
				Analyzer analyzer = getAnalyzer(field, lang);
				if (analyzer != null)
					mapField.put(field.name, analyzer.getQueryAnalyzer());
			}
			pfa = new PerFieldAnalyzer(mapField);
			langQueryAnalyzers.put(lang.getCode(), pfa);
			return pfa;
		} finally {
			rwl.w.unlock();
		}
	}

	public PerFieldAnalyzer getIndexPerFieldAnalyzer(LanguageEnum lang)
			throws SearchLibException {
		if (lang == null)
			lang = LanguageEnum.UNDEFINED;
		rwl.r.lock();
		try {
			PerFieldAnalyzer pfa = langIndexAnalyzers.get(lang.getCode());
			if (pfa != null)
				return pfa;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			PerFieldAnalyzer pfa = langIndexAnalyzers.get(lang.getCode());
			if (pfa != null)
				return pfa;
			Map<String, CompiledAnalyzer> mapField = new TreeMap<String, CompiledAnalyzer>();
			for (SchemaField field : fieldList) {
				Analyzer analyzer = getAnalyzer(field, lang);
				if (analyzer != null)
					mapField.put(field.name, analyzer.getIndexAnalyzer());
			}
			pfa = new PerFieldAnalyzer(mapField);
			langIndexAnalyzers.put(lang.getCode(), pfa);
			return pfa;
		} finally {
			rwl.w.unlock();
		}
	}

}
