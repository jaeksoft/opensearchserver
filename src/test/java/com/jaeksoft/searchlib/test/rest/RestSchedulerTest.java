package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.apple.eio.FileManager.getResource;

/**
 * Created by aureliengiudici on 09/05/2016.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestSchedulerTest extends CommonRestAPI {
	public final static String path = "/services/rest/index/{index_name}/scheduler/create";

	@Test
	public void test1_addTaskItem() throws IOException {
		String json = getResource("Scheduler_test.json");
		Response response = client().path("/services/rest/index/{index_name}/scheduler/create", IntegrationTest.INDEX_NAME)
				.query("login", "Tetraa", "key", "85dd8ea46c51d14370352c37dda79e4a")
				.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).put(json);
		checkCommonResult(response, CommonResult.class, 200);
	}
}
