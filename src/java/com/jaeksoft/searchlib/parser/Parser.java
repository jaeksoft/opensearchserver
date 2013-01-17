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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import org.knallgrau.utils.textcat.TextCategorizer;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.Lang;

public abstract class Parser extends ParserFactory {

	private IndexDocument sourceDocument;

	private IndexDocument parserDocument;

	private IndexDocument directDocument;

	private StreamLimiter streamLimiter;

	protected Parser(ParserFieldEnum[] fieldList) {
		super(fieldList);
		sourceDocument = null;
		directDocument = null;
		parserDocument = new IndexDocument();
		streamLimiter = null;
	}

	public void populate(IndexDocument indexDocument) {
		getFieldMap().mapIndexDocument(parserDocument, indexDocument);
		if (directDocument != null)
			indexDocument.add(directDocument);
	}

	public IndexDocument getSourceDocument() {
		return sourceDocument;
	}

	private void setSourceDocument(IndexDocument sourceDocument) {
		this.sourceDocument = sourceDocument;
	}

	public IndexDocument getParserDocument() {
		return parserDocument;
	}

	public void resetParserFieldList() {
		ParserFieldEnum[] parserFieldList = getFieldList();
		if (parserFieldList == null)
			return;
		for (ParserFieldEnum parserField : parserFieldList) {
			FieldContent fc = parserDocument.getField(parserField.name());
			if (fc != null)
				fc.clear();
		}
	}

	public void addField(ParserFieldEnum field, String value) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), new FieldValueItem(
				FieldValueOriginEnum.EXTERNAL, value));
	}

	public void addField(ParserFieldEnum field, String value, Float boost) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), new FieldValueItem(
				FieldValueOriginEnum.EXTERNAL, value, boost));
	}

	public void addDirectFields(String[] fields, String value) {
		if (directDocument == null)
			directDocument = new IndexDocument();
		for (String field : fields)
			directDocument.add(field, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, value));
	}

	public void addDirectFields(String[] fields, String value, Float boost) {
		if (directDocument == null)
			directDocument = new IndexDocument();
		for (String field : fields)
			directDocument.add(field, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, value, boost));
	}

	protected void addField(ParserFieldEnum field, Object object) {
		if (object == null)
			return;
		addField(field, object.toString());
	}

	protected void addField(ParserFieldEnum field, List<? extends Object> list) {
		if (list == null)
			return;
		for (Object object : list)
			addField(field, object.toString());
	}

	public FieldContent getFieldContent(ParserFieldEnum field) {
		return parserDocument.getFieldContent(field.name());
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

	protected abstract void parseContent(StreamLimiter streamLimiter,
			LanguageEnum lang) throws IOException;

	final public void doParserContent(IndexDocument sourceDocument,
			StreamLimiter streamLimiter, LanguageEnum lang) throws IOException {
		if (sourceDocument != null)
			setSourceDocument(sourceDocument);
		addField(ParserFieldEnum.parser_name, getParserName());
		parseContent(streamLimiter, lang);
	}

	final public StreamLimiter getStreamLimiter() {
		return streamLimiter;
	}

	public String getMd5size() throws NoSuchAlgorithmException, LimitException,
			IOException {
		String hash = null;
		if (streamLimiter != null)
			hash = streamLimiter.getMD5Hash() + '_' + streamLimiter.getSize();
		return hash;
	}

	public boolean equals(Parser one) {
		return this.getClass().getName().equals(one.getClass().getName());
	}

}
