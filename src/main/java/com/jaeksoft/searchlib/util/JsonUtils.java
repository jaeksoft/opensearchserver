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

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	private final static ObjectMapper mapper = new ObjectMapper();

	public static <T> T getObject(String json, Class<T> objectClass)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, objectClass);
	}

	public static <T> T getObject(String json, TypeReference<T> typeReference)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, typeReference);
	}

	public static String toJsonString(Object object)
			throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}

	public static void jsonToFile(Object object, File file)
			throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(file, object);
	}

	public final static TypeReference<Map<String, String>> MapStringStringTypeRef = new TypeReference<Map<String, String>>() {
	};

	public final static TypeReference<Map<String, List<String>>> MapStringListStringTypeRef = new TypeReference<Map<String, List<String>>>() {
	};

}
