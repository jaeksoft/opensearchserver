package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by aureliengiudici on 19/04/2016.
 */
public class RestStatusTest extends CommonRestAPI {
	public final static String path = "/services/rest/index/*/crawler/web/run";

	@Test
	public void testA_allStatus() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).accept(MediaType.APPLICATION_JSON).get();
		checkCommonResult(response, CommonResult.class, 200);

	}
}
