/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.knallgrau.utils.textcat.TextCategorizer;

import com.jaeksoft.searchlib.analysis.ClassFactory;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.Lang;

public abstract class Parser extends ClassFactory {

	private long sizeLimit;

	private IndexDocument sourceDocument;

	private IndexDocument parserDocument;

	private IndexDocument directDocument;

	private ParserFieldEnum[] fieldList;

	private ClassPropertyEnum[] propertyList;

	private FieldMap fieldMap;

	private UrlFilterItem[] urlFilterList;

	private String defaultCharset;

	private LimitReader limitReader;

	private LimitInputStream limitInputStream;

	protected Parser(ParserFieldEnum[] fieldList,
			ClassPropertyEnum[] propertyList) {

		this.fieldList = fieldList;
		this.propertyList = propertyList;
		sizeLimit = 0;
		sourceDocument = null;
		directDocument = null;
		parserDocument = new IndexDocument();
		defaultCharset = null;
		urlFilterList = null;
		limitReader = null;
		limitInputStream = null;
		for (ClassPropertyEnum props : propertyList) {
			addProperty(props, null, null);
		}
	}

	public void setFieldMap(FieldMap fieldMap) {
		this.fieldMap = fieldMap;
	}

	public void populate(IndexDocument indexDocument) {
		fieldMap.mapIndexDocument(parserDocument, indexDocument);
		if (directDocument != null)
			indexDocument.add(directDocument);
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

	public ClassPropertyEnum[] getPropertyList() {
		return propertyList;
	}

	public void addField(ParserFieldEnum field, String value) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), new FieldValueItem(value));
	}

	public void addDirectFields(String[] fields, String value) {
		if (directDocument == null)
			directDocument = new IndexDocument();
		for (String field : fields)
			directDocument.add(field, new FieldValueItem(value));
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
		FieldValueItem valueItem = parserDocument.getFieldValue(field.name(),
				pos);
		if (valueItem == null)
			return null;
		return valueItem.getValue();
	}

	public String getMergedBodyText(int maxChar, String separator,
			ParserFieldEnum field) {
		FieldContent fc = getFieldContent(field);
		if (fc == null)
			return "";
		return fc.getMergedValues(maxChar, separator);
	}

	protected Locale langDetection(int textLength, ParserFieldEnum parserField) {
		Locale lang = null;
		String langMethod = null;
		String text = getMergedBodyText(textLength, " ", parserField);
		if (text == null)
			return null;
		langMethod = "ngram recognition";
		String textcat = new TextCategorizer().categorize(text, text.length());
		lang = Lang.findLocaleDescription(textcat);

		if (lang == null)
			return null;

		addField(ParserFieldEnum.lang, lang.getLanguage());
		addField(ParserFieldEnum.lang_method, langMethod);
		return lang;
	}

	protected abstract void parseContent(LimitInputStream inputStream)
			throws IOException;

	protected abstract void parseContent(LimitReader reader) throws IOException;

	final public void parseContent(InputStream inputStream) throws IOException {
		limitInputStream = new LimitInputStream(inputStream, sizeLimit);
		parseContent(limitInputStream);
	}

	final public void parseContent(Reader reader) throws IOException {
		limitReader = new LimitReader(reader, sizeLimit);
		parseContent(limitReader);
	}

	final public void parseContent(FileInstanceAbstract fileInstance)
			throws IOException {
		if (!requireContent())
			return;
		InputStream is = null;
		try {
			is = fileInstance.getInputStream();
			parseContent(is);
		} finally {
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Returns TRUE if the content need to be downloaded for text extraction.
	 * 
	 * Return FALSE for listing only
	 * 
	 * @return
	 */
	protected boolean requireContent() {
		return true;
	}

	public LimitInputStream getLimitInputStream() {
		return limitInputStream;
	}

	public LimitReader getLimitReader() {
		return limitReader;
	}

	final public void parseContent(byte[] byteData) throws IOException {
		ByteArrayInputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(byteData);
			parseContent(inputStream);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	final public void parseContent(String stringData) throws IOException {
		StringReader stringReader = null;
		try {
			stringReader = new StringReader(stringData);
			parseContent(stringReader);
		} finally {
			if (stringReader != null)
				stringReader.close();
		}
	}

	public void parseContent(File file) throws IOException {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			parseContent(fileInputStream);
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
		}
	}

	public String getMd5size() throws NoSuchAlgorithmException {
		String hash = null;
		if (limitInputStream != null)
			hash = limitInputStream.getMD5Hash() + '_'
					+ limitInputStream.getSize();
		else if (limitReader != null)
			hash = limitReader.getMD5Hash() + '_' + limitReader.getSize();
		return hash;
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

	/**
	 * @param urlFilterList
	 *            the urlFilterList to set
	 */
	public void setUrlFilterList(UrlFilterItem[] urlFilterList) {
		this.urlFilterList = urlFilterList;
	}

	/**
	 * @return the urlFilterList
	 */
	public UrlFilterItem[] getUrlFilterList() {
		return urlFilterList;
	}

}
