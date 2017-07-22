/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2016-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.scheduler.SchedulerResult;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by aureliengiudici on 09/05/2016.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestSchedulerTest extends CommonRestAPI {
	public final static String path = "/services/rest/index/{index_name}/scheduler/create";

	@Test
	public void test1_addTaskItem() throws IOException {
		String json = getResource("Scheduler_test.json");
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/scheduler/create")
				.queryParam("login", "Tetraa", "key", "85dd8ea46c51d14370352c37dda79e4a")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(json, MediaType.APPLICATION_JSON));
		checkCommonResult(response, SchedulerResult.class, 200);
	}
}
