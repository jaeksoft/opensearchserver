/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class DocumentsResult {

	@XmlElement(name = "document")
	final public List<DocumentResult> documents;

	@XmlElement(name = "indexDocument")
	final public List<IndexDocumentResult> indexDocuments;

	@XmlElement(name = "k")
	final public String[] uniqueKeys;

	public DocumentsResult() {
		documents = null;
		indexDocuments = null;
		uniqueKeys = null;
	}

	public DocumentsResult(ResultDocumentsInterface<?> result,
			boolean indexDocument) throws SearchLibException, IOException {
		uniqueKeys = null;
		if (indexDocument) {
			documents = null;
			indexDocuments = new ArrayList<IndexDocumentResult>(1);
			result.populate(indexDocuments);
		} else {
			documents = DocumentResult.populateDocumentList(result,
					new ArrayList<DocumentResult>(1));
			indexDocuments = null;
		}
	}

	public DocumentsResult(String[] uniqueKeys) {
		this.documents = null;
		this.indexDocuments = null;
		this.uniqueKeys = uniqueKeys;
	}
}
