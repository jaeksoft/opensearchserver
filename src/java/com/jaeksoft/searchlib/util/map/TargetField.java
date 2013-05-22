/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util.map;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.DomUtils;

public class TargetField implements Comparable<TargetField> {

	private String name;

	private String analyzer;

	private CompiledAnalyzer cachedAnalyzer;

	public TargetField(String name, String analyzer) {
		this.name = name;
		this.analyzer = analyzer;
		this.cachedAnalyzer = null;
	}

	public TargetField(String name) {
		this(name, (String) null);
	}

	public TargetField(String name, Node node) {
		this(name, DomUtils.getAttributeText(node, "analyzer"));
	}

	/**
	 * @return the name
	 */
	final public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	final public void setName(String name) {
		this.name = name;
	}

	@Override
	final public String toString() {
		return name;
	}

	final public String toXmlAttribute() {
		return name;
	}

	@Override
	final public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof TargetField))
			return false;
		TargetField tf = (TargetField) o;
		return StringUtils.equals(tf.name, this.name);
	}

	@Override
	final public int compareTo(TargetField o) {
		return name.compareTo(o.name);
	}

	/**
	 * @return the analyzer
	 */
	final public String getAnalyzer() {
		return analyzer;
	}

	/**
	 * @param analyzer
	 *            the analyzer to set
	 */
	final public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
		this.cachedAnalyzer = null;
	}

	final public void setCachedAnalyzer(AnalyzerList analyzerList,
			LanguageEnum lang) throws SearchLibException {
		cachedAnalyzer = null;
		if (analyzer == null)
			return;
		Analyzer a = analyzerList.get(analyzer, lang);
		if (a == null)
			a = analyzerList.get(analyzer, null);
		if (a == null)
			return;
		cachedAnalyzer = a.getIndexAnalyzer();
	}

	public void add(FieldValueItem[] fieldValueItems, IndexDocument document)
			throws IOException {
		if (fieldValueItems == null)
			return;
		FieldContent fc = document.getFieldContent(name);
		if (cachedAnalyzer == null) {
			for (FieldValueItem fvi : fieldValueItems)
				fc.add(fvi);
		} else {
			for (FieldValueItem fvi : fieldValueItems)
				cachedAnalyzer.populate(fvi.getValue(), fc);
		}
	}

	public void add(String value, IndexDocument document) throws IOException {
		if (value == null)
			return;
		FieldContent fc = document.getFieldContent(name);
		if (cachedAnalyzer == null)
			fc.add(new FieldValueItem(FieldValueOriginEnum.EXTERNAL, value));
		else
			cachedAnalyzer.populate(value, fc);
	}

	public void add(List<String> values, IndexDocument document)
			throws IOException {
		if (values == null)
			return;
		FieldContent fc = document.getFieldContent(name);
		if (cachedAnalyzer == null) {
			for (String value : values)
				fc.add(new FieldValueItem(FieldValueOriginEnum.EXTERNAL, value));
		} else {
			for (String value : values)
				cachedAnalyzer.populate(value, fc);
		}
	}

}
