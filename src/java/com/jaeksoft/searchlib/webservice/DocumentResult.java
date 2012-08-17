/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DocumentResult {

	@XmlElement(name = "pos")
	public int pos;

	@XmlElement(name = "score")
	public float score;

	@XmlElement(name = "collapseCount")
	public int collapseCount;

	@XmlElement(name = "field")
	public List<FieldValueList> fields;

	@XmlElement(name = "snippet")
	public List<SnippetValueList> snippets;

	public DocumentResult() {
		fields = null;
		snippets = null;
		collapseCount = 0;
		pos = 0;
		score = 0;
	}

	public DocumentResult(ResultDocument resultDocument, int collapseDocCount,
			int position, float docScore) {
		fields = new ArrayList<FieldValueList>();
		for (FieldValue fiedValue : resultDocument.getReturnFields().values())
			fields.add(new FieldValueList(fiedValue));
		snippets = new ArrayList<SnippetValueList>();
		for (SnippetFieldValue snippetFiedValue : resultDocument
				.getSnippetFields().values()) {
			boolean highlighted = resultDocument.isHighlighted(snippetFiedValue
					.getName());
			snippets.add(new SnippetValueList(highlighted, snippetFiedValue));
		}
		collapseCount = collapseDocCount;
		pos = position;
		score = docScore;
	}
}
