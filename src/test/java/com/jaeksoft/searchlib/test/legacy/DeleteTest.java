package com.jaeksoft.searchlib.test.legacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.xml.sax.SAXException;

public class DeleteTest extends TestCase {
	private CommonTestCase commomTestCase = null;

	public DeleteTest(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	public void getDeletedDocument() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("q", "*:*"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.DELETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Deleted']");
		assertEquals("174", response);
	}

}
