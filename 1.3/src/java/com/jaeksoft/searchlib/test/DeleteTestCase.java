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

public class DeleteTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public DeleteTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void getDeletedDocument() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("q", "*:*"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.DELETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Deleted']");
		assertEquals("174", response);
	}

	public static TestSuite suite() throws InterruptedException {
		TestSuite deleteTestCase = new TestSuite();
		deleteTestCase.addTest(new DeleteTestCase("getDeletedDocument"));
		return deleteTestCase;
	}

}
