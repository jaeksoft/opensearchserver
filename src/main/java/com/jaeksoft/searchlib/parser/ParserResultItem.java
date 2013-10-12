/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
import java.util.List;
import java.util.Locale;

import com.cybozu.labs.langdetect.LangDetectException;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.Lang;

public class ParserResultItem {

	private final Parser parser;

	private final IndexDocument parserDocument;

	private IndexDocument directDocument;

	public ParserResultItem(Parser parser) {
		this.parser = parser;
		directDocument = null;
		parserDocument = new IndexDocument();
		addField(ParserFieldEnum.parser_name, parser.getParserName());
	}

	public void populate(IndexDocument indexDocument) throws IOException {
		parser.getFieldMap().mapIndexDocument(parserDocument, indexDocument);
		if (directDocument != null)
			indexDocument.add(directDocument);
	}

	public IndexDocument getParserDocument() {
		return parserDocument;
	}

	public void addField(ParserFieldEnum field, String value) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), new FieldValueItem(
				FieldValueOriginEnum.EXTERNAL, value));
	}

	protected void addField(ParserFieldEnum field, String value, Float boost) {
		if (value == null)
			return;
		if (value.length() == 0)
			return;
		parserDocument.add(field.name(), new FieldValueItem(
				FieldValueOriginEnum.EXTERNAL, value, boost));
	}

	protected void addDirectFields(String[] fields, String value) {
		if (directDocument == null)
			directDocument = new IndexDocument();
		for (String field : fields)
			directDocument.add(field, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, value));
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

	protected String getMergedBodyText(int maxChar, String separator,
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
		try {
			lang = Lang.langDetection(text, text.length());
		} catch (LangDetectException e) {
			Logging.warn(e);
			return null;
		}
		if (lang == null)
			return null;

		addField(ParserFieldEnum.lang, lang.getLanguage());
		addField(ParserFieldEnum.lang_method, langMethod);
		return lang;
	}

}
