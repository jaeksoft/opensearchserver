/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.api;

import java.util.List;

public class Api {

	private String apiName;
	private String queryTemplate;
	private List<OpenSearchApi> openSearchApi;

	public Api(String openSearch, String queryTemplate,
			List<OpenSearchApi> openSearchApi) {
		this.apiName = openSearch;
		this.queryTemplate = queryTemplate;
		this.openSearchApi = openSearchApi;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getQueryTemplate() {
		return queryTemplate;
	}

	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

	public List<OpenSearchApi> getOpenSearchApi() {
		return openSearchApi;
	}

	public void setOpenSearchApi(List<OpenSearchApi> openSearchApi) {
		this.openSearchApi = openSearchApi;
	}
}
