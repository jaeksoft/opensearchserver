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

public class DeleteIndexTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public DeleteIndexTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void deleteIndex() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd",
				"deleteindex"));
		namedValuePairs.add(commomTestCase.getNameValuePair("index.name",
				CommomTestCase.INDEX_NAME));
		namedValuePairs.add(commomTestCase.getNameValuePair(
				"index.delete.name", CommomTestCase.INDEX_NAME));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, false);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("Index deleted: oss_1.3", response);
	}

	public static TestSuite suite() throws InterruptedException {
		TestSuite deleteTestCase = new TestSuite();
		deleteTestCase.addTest(new DeleteIndexTestCase("deleteIndex"));
		return deleteTestCase;
	}
}
