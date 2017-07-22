/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.webservice.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.document.FieldValueList;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "result")
@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ParserDocumentsResult extends CommonResult {

	final public String mimeType;

	final public String parserName;

	@XmlElement(name = "document")
	@JsonProperty("documents")
	final public List<ParserDocument> documents;

	public ParserDocumentsResult() {
		documents = null;
		mimeType = null;
		parserName = null;
	}

	public ParserDocumentsResult(String mimeType, String parserName, List<ParserResultItem> parserResultList) {
		this.mimeType = mimeType;
		this.parserName = parserName;
		if (CollectionUtils.isEmpty(parserResultList)) {
			documents = null;
			return;
		}
		documents = new ArrayList<ParserDocument>(parserResultList.size());
		for (ParserResultItem parserResultItem : parserResultList)
			documents.add(new ParserDocument(parserResultItem));
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	static public class ParserDocument {

		@XmlElement(name = "field")
		@JsonProperty("fields")
		public final List<FieldValueList> fields;

		public ParserDocument() {
			fields = null;
		}

		public ParserDocument(ParserResultItem parserResultItem) {
			fields = FieldValueList.getNewList(parserResultItem.getParserDocument());
		}
	}
}
