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
package com.jaeksoft.searchlib.webservice.query.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.webservice.query.QueryAbstract;

@JsonInclude(Include.NON_EMPTY)
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentsQuery extends QueryAbstract {

	final public List<String> uniqueKeys;

	final public List<String> returnedFields;

	public DocumentsQuery() {
		uniqueKeys = null;
		returnedFields = null;
	}

	public DocumentsQuery(DocumentsRequest request) {
		ReturnFieldList rfl = request.getReturnFieldList();
		if (rfl != null) {
			returnedFields = new ArrayList<String>(rfl.size());
			rfl.toNameList(returnedFields);
		} else
			returnedFields = null;
		uniqueKeys = new ArrayList<String>(request.getUniqueKeyList());
	}

	@Override
	protected void apply(AbstractRequest req) {
		super.apply(req);
		DocumentsRequest request = (DocumentsRequest) req;
		if (uniqueKeys != null)
			request.addUniqueKeys(uniqueKeys);
		if (returnedFields != null)
			request.addReturnFields(returnedFields);
	}
}
