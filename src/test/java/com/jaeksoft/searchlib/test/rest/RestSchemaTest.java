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

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.fields.ResultField;
import com.jaeksoft.searchlib.webservice.fields.ResultFieldList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestSchemaTest extends CommonRestAPI {

	@Test
	public void testA_addFields() throws IOException {
		String json = getResource("schema_fields.json");
		Response response = client()
				.path("/services/rest/index/{index_name}/field",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).put(json);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testB_addField() throws IOException {
		String json = getResource("schema_field.json");
		Response response = client()
				.path("/services/rest/index/{index_name}/field/{field_name}",
						IntegrationTest.INDEX_NAME, "autocomplete")
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).put(json);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testC_addField() throws IOException {
		String json = getResource("schema_field_noname.json");
		Response response = client()
				.path("/services/rest/index/{index_name}/field/{field_name}",
						IntegrationTest.INDEX_NAME, "autocomplete2")
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).put(json);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testD_setDefaulUniqueField() {
		Response response = client()
				.accept(MediaType.APPLICATION_JSON)
				.path("/services/rest/index/{index_name}/field",
						IntegrationTest.INDEX_NAME).query("default", "content")
				.query("unique", "id").post(null);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testE_listFields() {
		Response response = client()
				.accept(MediaType.APPLICATION_JSON)
				.path("/services/rest/index/{index_name}/field",
						IntegrationTest.INDEX_NAME).get();
		ResultFieldList resultFieldList = checkCommonResult(response,
				ResultFieldList.class, 200);
		assertEquals("id", resultFieldList.uniqueField);
		assertEquals("content", resultFieldList.defaultField);
		assertEquals((int) 4, resultFieldList.fields.size());
	}

	@Test
	public void testF_getField() {
		Response response = client()
				.accept(MediaType.APPLICATION_JSON)
				.path("/services/rest/index/{index_name}/field/{field_name}",
						IntegrationTest.INDEX_NAME, "autocomplete2").get();
		ResultField resultField = checkCommonResult(response,
				ResultField.class, 200);
		assertEquals("autocomplete2", resultField.field.name);
	}

	@Test
	public void testG_deleteField() {
		Response response = client()
				.accept(MediaType.APPLICATION_JSON)
				.path("/services/rest/index/{index_name}/field/{field_name}",
						IntegrationTest.INDEX_NAME, "autocomplete2").delete();
		checkCommonResult(response, CommonResult.class, 200);
	}
}
