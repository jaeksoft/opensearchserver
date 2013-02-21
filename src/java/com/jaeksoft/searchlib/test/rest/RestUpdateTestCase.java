package com.jaeksoft.searchlib.test.rest;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.jaeksoft.searchlib.test.CommonTestCase;

public class RestUpdateTestCase extends TestCase {
	private CommonTestCase commomTestCase = null;

	public RestUpdateTestCase(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	@Test
	public void testRestAPIUpdateDocument() throws ClientProtocolException,
			IOException {
		File documents = FileUtils.toFile(this.getClass().getResource(
				"documents.xml"));
		int status = commomTestCase.restAPIPostFile(documents,
				"application/xml", CommonTestCase.INDEX_API);
		assertEquals(200, status);
	}
}
