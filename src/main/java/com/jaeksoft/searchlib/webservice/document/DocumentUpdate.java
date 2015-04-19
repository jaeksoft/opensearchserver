/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class DocumentUpdate {

	@XmlAttribute
	public final LanguageEnum lang;

	public final List<Field> fields;

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@JsonInclude(Include.NON_EMPTY)
	public static class Field {

		@XmlAttribute
		final public String name;

		@XmlAttribute
		final public Float boost;

		@XmlValue
		final public String value;

		public Field() {
			name = null;
			boost = null;
			value = null;
		}

		public Field(String name, String value, Float boost) {
			this.name = name;
			this.value = value;
			this.boost = boost;
		}
	}

	public DocumentUpdate(LanguageEnum lang) {
		this.lang = lang;
		fields = new ArrayList<Field>();
	}

	public DocumentUpdate() {
		lang = LanguageEnum.UNDEFINED;
		fields = null;
	}

	public DocumentUpdate(IndexDocument indexDocument) {
		lang = indexDocument.getLang();
		List<Field> fieldList = new ArrayList<Field>();
		for (FieldContent fieldContent : indexDocument) {
			List<FieldValueItem> fieldValueItems = fieldContent.getValues();
			if (fieldValueItems == null)
				continue;
			String fieldName = fieldContent.getField();
			for (FieldValueItem fieldValueItem : fieldValueItems)
				fieldList.add(new Field(fieldName, fieldValueItem.value,
						fieldValueItem.boost));
		}
		fields = fieldList;
	}

	public void populateDocument(IndexDocument indexDocument) {
		if (fields == null)
			return;
		for (Field field : fields)
			indexDocument.add(field.name, field.value, field.boost);
	}

	public static final IndexDocument getIndexDocument(
			DocumentUpdate documentUpdate) {
		IndexDocument indexDocument = documentUpdate.lang == null ? new IndexDocument()
				: new IndexDocument(documentUpdate.lang);
		documentUpdate.populateDocument(indexDocument);
		return indexDocument;
	}

}
