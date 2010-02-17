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

package com.jaeksoft.searchlib.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;

public abstract class Parser {

	private long sizeLimit;

	private IndexDocument sourceDocument;

	private IndexDocument parserDocument;

	private ParserFieldEnum[] fieldList;

	private FieldMap fieldMap;

	private String defaultCharset;

	protected Parser(ParserFieldEnum[] fieldList) {
		this.fieldList = fieldList;
		sizeLimit = 0;
		sourceDocument = null;
		parserDocument = new IndexDocument();
		defaultCharset = null;
	}

	public void setFieldMap(FieldMap fieldMap) {
		this.fieldMap = fieldMap;
	}

	public void populate(IndexDocument indexDocument) {
		fieldMap.mapIndexDocument(parserDocument, indexDocument);
	}

	public void setSizeLimit(long l) {
		sizeLimit = l;
	}

	public IndexDocument getSourceDocument() {
		return sourceDocument;
	}

	public void setSourceDocument(IndexDocument sourceDocument) {
		this.sourceDocument = sourceDocument;
	}

	public IndexDocument getParserDocument() {
		return parserDocument;
	}

	public ParserFieldEnum[] getFieldList() {
		return fieldList;
	}

	public abstract ParserFieldEnum[] getParserFieldList();

	public void addField(ParserFieldEnum field, String value) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), value);
	}

	protected void addField(ParserFieldEnum field, Object object) {
		if (object == null)
			return;
		addField(field, object.toString());
	}

	public FieldContent getFieldContent(ParserFieldEnum field) {
		return parserDocument.getField(field.name());
	}

	public String getFieldValue(ParserFieldEnum field, int pos) {
		return parserDocument.getFieldValue(field.name(), pos);
	}

	public String getMergedBodyText(int maxChar, String sep,
			ParserFieldEnum field) {
		StringBuffer sb = new StringBuffer();
		FieldContent fc = getFieldContent(field);
		if (fc != null) {
			for (String value : fc.getValues()) {
				sb.append(value);
				if (sb.length() > maxChar)
					break;
				sb.append(sep);
			}
		}
		if (sb.length() > maxChar)
			return sb.substring(0, maxChar);
		return sb.toString();
	}

	protected abstract void parseContent(LimitInputStream inputStream)
			throws IOException;

	protected abstract void parseContent(LimitReader reader) throws IOException;

	public void parseContent(InputStream inputStream) throws IOException {
		parseContent(new LimitInputStream(inputStream, sizeLimit));
	}

	public void parseContent(Reader reader) throws IOException {
		parseContent(new LimitReader(reader, sizeLimit));
	}

	public void parseContent(byte[] byteData) throws IOException {
		ByteArrayInputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(byteData);
			parseContent(inputStream);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	public void parseContent(String stringData) throws IOException {
		StringReader stringReader = null;
		try {
			new StringReader(stringData);
			parseContent(stringReader);
		} finally {
			if (stringReader != null)
				stringReader.close();
		}
	}

	public boolean equals(Parser one) {
		return this.getClass().getName().equals(one.getClass().getName());
	}

	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}

}
