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

public class FileCrawlerTest extends TestCase {
	private CommonTestCase commomTestCase = null;

	public FileCrawlerTest(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	public void createFileCrawlerInstance() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "create"));
		namedValuePairs.add(commomTestCase.getNameValuePair("type", "file"));
		namedValuePairs.add(commomTestCase.getNameValuePair("path",
				"/home/opensearchserver"));
		namedValuePairs.add(commomTestCase.getNameValuePair("withsubdirectory",
				"true"));
		namedValuePairs.add(commomTestCase.getNameValuePair("delay", "10"));
		namedValuePairs.add(commomTestCase.getNameValuePair("enabled", "true"));
		namedValuePairs.add(commomTestCase.getNameValuePair("ignorehidden",
				"true"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.FILE_CRAWLER_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("A new file crawler instance is created.", response);

	}

	public void startFileCrwler() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "start"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.FILE_CRAWLER_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("STARTED", response);

	}

	public void stopFileCrwler() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "stop"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.FILE_CRAWLER_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("STOPPED", response);

	}

}
