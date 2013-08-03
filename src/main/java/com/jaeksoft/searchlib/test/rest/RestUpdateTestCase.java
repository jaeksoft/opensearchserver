package com.jaeksoft.searchlib.test.rest;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class RestUpdateTestCase extends TestCase {
	private CommonRestTestCase commonRestTestCase = null;

	public RestUpdateTestCase(String name) {
		super(name);
		commonRestTestCase = new CommonRestTestCase();
	}

	@Test
	public void testRestAPIUpdateDocument() throws ClientProtocolException,
			IOException {
		File documents = FileUtils.toFile(this.getClass().getResource(
				"documents.xml"));
		int status = commonRestTestCase.restAPIPostFile(documents,
				"application/xml", "/update");
		assertEquals(200, status);
	}
}
