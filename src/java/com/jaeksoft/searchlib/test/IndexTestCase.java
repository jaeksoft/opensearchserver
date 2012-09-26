package com.jaeksoft.searchlib.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

public class IndexTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public IndexTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void indexDocument() throws ClientProtocolException, IOException {
		File patterns = FileUtils.toFile(this.getClass().getResource(
				"documents.xml"));
		int status = commomTestCase.postFile(patterns, "text/xml",
				CommomTestCase.INDEX_API);
		assertEquals(200, status);
	}

	public static TestSuite suite() {
		TestSuite indexSuit = new TestSuite();
		indexSuit.addTest(new IndexTestCase("indexDocument"));
		return indexSuit;
	}
}
