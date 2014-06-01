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
package com.jaeksoft.searchlib.webservice.query.namedEntity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.result.ResultNamedEntityExtraction;
import com.jaeksoft.searchlib.webservice.query.document.DocumentResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class NamedEntityResult {

	@XmlElement(name = "document")
	final public List<DocumentResult> documents;
	final public CharSequence enrichedText;

	public NamedEntityResult() {
		documents = null;
		enrichedText = null;
	}

	public NamedEntityResult(ResultNamedEntityExtraction result)
			throws SearchLibException {
		documents = new ArrayList<DocumentResult>(1);
		DocumentResult.populateDocumentList(result, documents);
		enrichedText = result.getEnrichedText();
	}

}
