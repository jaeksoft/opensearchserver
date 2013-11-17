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
package com.jaeksoft.searchlib.webservice.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class QueryTemplateResultList extends CommonResult {

	final public Collection<QueryTemplate> templates;

	public QueryTemplateResultList() {
		templates = null;
	}

	public QueryTemplateResultList(
			Set<Entry<String, AbstractRequest>> requests,
			RequestTypeEnum[] types) {
		super(true, null);
		templates = requests == null ? null : new ArrayList<QueryTemplate>(
				requests.size());
		if (requests != null)
			for (Entry<String, AbstractRequest> entry : requests)
				for (RequestTypeEnum type : types)
					if (entry.getValue().requestType == type)
						templates.add(new QueryTemplate(entry.getValue()));
		int n = templates == null ? 0 : templates.size();
		setInfo(n + " template(s)");
	}

	public class QueryTemplate {

		final public String name;
		final public String type;

		public QueryTemplate() {
			name = null;
			type = null;
		}

		public QueryTemplate(AbstractRequest request) {
			name = request.getRequestName();
			type = request.requestType.getLabel();
		}

	}
}
