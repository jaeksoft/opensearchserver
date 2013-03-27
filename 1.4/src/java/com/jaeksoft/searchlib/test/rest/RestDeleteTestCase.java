package com.jaeksoft.searchlib.test.rest;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.webservice.CommonResult;

public class RestDeleteTestCase extends TestCase {
	private CommonRestTestCase commonRestTestCase = null;

	public RestDeleteTestCase(String name) {
		super(name);
		commonRestTestCase = new CommonRestTestCase();
	}

	@Test
	public void testBDeleteIndexXML() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/delete/json");
		webClient.query("name", CommonRestTestCase.INDEX_NAME);
		Response response = commonRestTestCase.doDeleteRequest(webClient);
		CommonResult commonResult = response.readEntity(CommonResult.class);
		assertEquals("Index deleted: oss", commonResult.info);

	}

}
