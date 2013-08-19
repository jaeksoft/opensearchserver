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

package com.jaeksoft.searchlib.webservice;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.webservice.CommonServices.CommonServiceException;

public class JSONParam {

	private String json;

	public JSONParam(String json) throws WebApplicationException {
		this.json = json;
	}

	public <T> T getObject(Class<T> objectClass) {
		try {
			return JsonUtils.getObject(json, objectClass);
		} catch (JsonParseException e) {
			throw new CommonServiceException(e);
		} catch (JsonMappingException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	public <T> T getObject(TypeReference<T> typeReference) {
		try {
			return JsonUtils.getObject(json, typeReference);
		} catch (JsonParseException e) {
			throw new CommonServiceException(e);
		} catch (JsonMappingException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	public Map<String, String> getVariables() {
		return getObject(new TypeReference<Map<String, String>>() {
		});
	}

}
