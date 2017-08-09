/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetCounter;
import com.jaeksoft.searchlib.webservice.CommonResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HostnamesResult extends CommonResult {

	@JsonInclude(Include.NON_NULL)
	public final Map<String, Long> hostnames;

	public HostnamesResult() {
		hostnames = null;
	}

	public HostnamesResult(Facet facet) {
		super(true, null);
		hostnames = new LinkedHashMap<>();
		if (facet != null)
			for (Map.Entry<String, FacetCounter> entry : facet.getList())
				hostnames.put(entry.getKey(), entry.getValue().count);
	}
}
