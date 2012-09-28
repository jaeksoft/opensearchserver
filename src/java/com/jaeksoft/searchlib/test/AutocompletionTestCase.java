package com.jaeksoft.searchlib.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.xml.sax.SAXException;

public class AutocompletionTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public AutocompletionTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void setAutocompletionField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "set"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field",
				"titleExact"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.AUTOCOMPLETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Field']");
		assertEquals("titleExact", response);
	}

	public void startAutoCompletion() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "build"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.AUTOCOMPLETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Count']");
		assertEquals("300", response);
	}

	public void getAutocompletion() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "a"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.AUTOCOMPLETE_API, true);
		InputStream response = commomTestCase.getResponse(httpPost);
		String suggestions = IOUtils.toString(response);
		String suggests[] = suggestions.split("\n");
		assertEquals(10, suggests.length);
	}

	public static TestSuite suite() throws InterruptedException {
		TestSuite autoCompletionTestCase = new TestSuite();
		autoCompletionTestCase.addTest(new AutocompletionTestCase(
				"setAutocompletionField"));
		autoCompletionTestCase.addTest(new AutocompletionTestCase(
				"startAutoCompletion"));
		autoCompletionTestCase.addTest(new AutocompletionTestCase(
				"getAutocompletion"));
		return autoCompletionTestCase;
	}
}
