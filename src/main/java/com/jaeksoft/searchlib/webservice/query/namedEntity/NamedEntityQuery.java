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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import com.jaeksoft.searchlib.webservice.query.QueryAbstract;

@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class NamedEntityQuery extends QueryAbstract {

	final public String text;

	final public String searchRequest;

	final public String namedEntityField;

	final public List<String> returnedFields;

	public NamedEntityQuery() {
		text = null;
		searchRequest = null;
		namedEntityField = null;
		returnedFields = null;
	}

	public NamedEntityQuery(NamedEntityExtractionRequest request) {
		text = request.getText();
		searchRequest = request.getSearchRequest();
		namedEntityField = request.getNamedEntityField();
		returnedFields = null;
	}

	@Override
	protected void apply(AbstractRequest req) {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) req;
		if (text != null)
			request.setText(text);
		if (searchRequest != null)
			request.setSearchRequest(searchRequest);
		if (namedEntityField != null)
			request.setNamedEntityField(namedEntityField);
		if (returnedFields != null)
			request.setReturnedFields(returnedFields);
	}
}
