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

public class SearchTemplateTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public SearchTemplateTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void creatSpellCheckQuery() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "create"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.type",
				"SpellCheckRequest"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.name",
				"spellcheck"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.suggestions",
				"1"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.field",
				"titleExact"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.lang",
				"ENGLISH"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt.algorithm",
				"JaroWinklerDistance"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_TEMPLATE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Status']");
		assertEquals("OK", response);
	}

	public static TestSuite suite() {
		TestSuite searchTemplateTestCase = new TestSuite();
		searchTemplateTestCase.addTest(new SearchTemplateTestCase(
				"creatSpellCheckQuery"));
		return searchTemplateTestCase;
	}
}
