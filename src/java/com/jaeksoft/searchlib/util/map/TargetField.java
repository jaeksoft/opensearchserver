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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

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
		this(name, null);
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
		if (!(o instanceof TargetField))
			return false;
		return this.name.equals(((TargetField) o).name);
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

	final public void add(FieldValueItem fvi, IndexDocument document)
			throws IOException {
		if (cachedAnalyzer == null)
			document.add(name, fvi);
		else
			cachedAnalyzer.populate(fvi.getValue(),
					document.getFieldContent(name));
	}
}
