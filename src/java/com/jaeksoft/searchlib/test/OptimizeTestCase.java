package com.jaeksoft.searchlib.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.xml.sax.SAXException;

public class OptimizeTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public OptimizeTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void optimizeIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.OPTIMIZE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Status']");
		assertEquals("OK", response);
	}

	public static TestSuite suite() throws InterruptedException {
		TestSuite optimizeTestSuit = new TestSuite();
		optimizeTestSuit.addTest(new OptimizeTestCase("optimizeIndex"));
		return optimizeTestSuit;
	}

}
