package com.jaeksoft.searchlib.test.rest;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.autocompletion.AutoCompletionResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestAutocompletionTestCase extends TestCase {
	private CommonRestTestCase commonRestTestCase = null;

	public RestAutocompletionTestCase(String name) {
		super(name);
		commonRestTestCase = new CommonRestTestCase();
	}

	@Test
	public void testASetAutocompletionField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/autocompletion/set/"
						+ CommonRestTestCase.INDEX_NAME + "/xml");
		webClient.query("field", "title");
		webClient.query("rows", 10);
		Response response = commonRestTestCase.doGetRequest(webClient);
		CommonResult commonResult = response.readEntity(CommonResult.class);
		assertTrue(commonResult.successful);
	}

	@Test
	public void testBStartAutocompletionField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/autocompletion/build/"
						+ CommonRestTestCase.INDEX_NAME + "/xml");
		Response response = commonRestTestCase.doGetRequest(webClient);
		CommonResult commonResult = response.readEntity(CommonResult.class);
		assertTrue(commonResult.successful);
		assertEquals("170 term(s) indexed", commonResult.info);
	}

	@Test
	public void testCQueryAutocompletionField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/autocompletion/query/"
						+ CommonRestTestCase.INDEX_NAME + "/xml");
		webClient.query("prefix", "Open");
		webClient.query("rows", 10);
		commonRestTestCase.doGetRequest(webClient);
		Collection<? extends AutoCompletionResult> autoCompletionResult = webClient
				.getCollection(AutoCompletionResult.class);
		assertEquals(2, autoCompletionResult.size());
	}
}
