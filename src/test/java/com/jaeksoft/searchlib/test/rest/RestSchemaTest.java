/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.fields.ResultField;
import com.jaeksoft.searchlib.webservice.fields.ResultFieldList;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestSchemaTest extends CommonRestAPI {

	@Test
	public void testA_addFields() throws IOException {
		String json = getResource("schema_fields.json");
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field/").request(
				MediaType.APPLICATION_JSON).put(Entity.entity(json, MediaType.APPLICATION_JSON));
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testB_addField() throws IOException {
		String json = getResource("schema_field.json");
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field/autocomplete")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(json, MediaType.APPLICATION_JSON));
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testC_addField() throws IOException {
		String json = getResource("schema_field_noname.json");
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field/autocomplete2")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(json, MediaType.APPLICATION_JSON));
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testD_setDefaulUniqueField() {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field").queryParam(
				"default", "content").queryParam("unique", "id").request(MediaType.APPLICATION_JSON).post(null);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testE_listFields() {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field").request(
				MediaType.APPLICATION_JSON).get();
		ResultFieldList resultFieldList = checkCommonResult(response, ResultFieldList.class, 200);
		assertEquals("id", resultFieldList.uniqueField);
		assertEquals("content", resultFieldList.defaultField);
		assertEquals((int) 6, resultFieldList.fields.size());
	}

	@Test
	public void testF_getField() {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field/autocomplete2")
				.request(MediaType.APPLICATION_JSON)
				.get();
		ResultField resultField = checkCommonResult(response, ResultField.class, 200);
		assertEquals("autocomplete2", resultField.field.name);
	}

	@Test
	public void testG_deleteField() {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/field/autocomplete2")
				.request(MediaType.APPLICATION_JSON)
				.delete();
		checkCommonResult(response, CommonResult.class, 200);
	}
}
