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

package com.jaeksoft.searchlib.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class FieldMap extends FieldMapGeneric<SourceField, TargetField> {

	final private char concatSeparator;

	public FieldMap() {
		concatSeparator = '|';
	}

	public FieldMap(String multilineText, char fieldSeparator,
			char concatSeparator) throws IOException {
		StringReader sr = new StringReader(multilineText);
		BufferedReader br = new BufferedReader(sr);
		this.concatSeparator = concatSeparator;
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = StringUtils.split(line, fieldSeparator);
				if (cols == null || cols.length < 2)
					continue;
				String source = cols[0];
				String target = cols[1];
				String analyzer = cols.length > 2 ? cols[2] : null;
				add(new SourceField(source, concatSeparator), new TargetField(
						target, analyzer));
			}
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(sr);
		}
	}

	public FieldMap(File file) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		super(file, "/map");
		concatSeparator = '|';
	}

	public FieldMap(XPathParser xpp, Node node) throws XPathExpressionException {
		super(xpp, node);
		concatSeparator = '|';
	}

	@Override
	protected TargetField loadTarget(String targetName, Node node) {
		return new TargetField(targetName);
	}

	@Override
	protected SourceField loadSource(String sourceName) {
		return new SourceField(sourceName, concatSeparator);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, TargetField target)
			throws SAXException {
	}

	private void addFieldContent(FieldContent fc, TargetField targetField,
			IndexDocument target) throws IOException {
		if (fc == null)
			return;
		FieldValueItem[] values = fc.getValues();
		if (values != null)
			for (FieldValueItem valueItem : values)
				targetField.add(valueItem, target);
	}

	public void mapIndexDocument(IndexDocument source, IndexDocument target)
			throws IOException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			SourceField sourceField = link.getSource();
			if (sourceField.isUnique()) {
				FieldContent fc = sourceField.getUniqueString(source);
				if (fc == null)
					fc = sourceField.getUniqueString(target);
				addFieldContent(fc, link.getTarget(), target);
			} else {
				String value = sourceField.getConcatString(source, target);
				if (value != null)
					link.getTarget().add(
							new FieldValueItem(FieldValueOriginEnum.EXTERNAL,
									value), target);
			}
		}
	}

	public void mapIndexDocument(ResultDocument source, IndexDocument target)
			throws IOException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			SourceField sourceField = link.getSource();
			if (sourceField.isUnique()) {
				FieldValueItem[] fvi = sourceField.getUniqueString(source);
				if (fvi != null)
					for (FieldValueItem valueItem : fvi)
						link.getTarget().add(valueItem, target);
			} else {
				String value = sourceField.getConcatString(source, target);
				if (value != null)
					link.getTarget().add(
							new FieldValueItem(FieldValueOriginEnum.EXTERNAL,
									value), target);
			}
		}
	}

	public void cacheAnalyzers(AnalyzerList analyzerList, LanguageEnum lang)
			throws SearchLibException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			TargetField target = link.getTarget();
			if (target == null)
				throw new SearchLibException("No target field for "
						+ link.getSource());
			target.setCachedAnalyzer(analyzerList, lang);
		}
	}

}
