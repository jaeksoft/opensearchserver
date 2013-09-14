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

package com.jaeksoft.searchlib.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.jaeksoft.searchlib.webservice.CommonResult;

public abstract class CommonRestAPI {

	public WebClient client() {
		return WebClient.create(AllRestAPITests.SERVER_URL,
				Collections.singletonList(new JacksonJsonProvider()));
	}

	public <T extends CommonResult> T checkCommonResult(Response response,
			Class<T> commonResultClass, int httpCode) {
		assertNotNull(response);
		assertEquals((int) httpCode, response.getStatus());
		T commonResult = response.readEntity(commonResultClass);
		assertNotNull(commonResult.successful);
		assertEquals(true, commonResult.successful);
		return commonResult;
	}
}
