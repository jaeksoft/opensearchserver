package com.jaeksoft.searchlib.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;

public class Full {

	private Client client;

	@Before
	public void getInstance() throws SearchLibException,
			NoSuchAlgorithmException, XPathExpressionException, IOException,
			URISyntaxException, ParserConfigurationException, SAXException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		File configFile = new File("resources/test_config.xml");
		assertTrue(configFile.exists());
		client = Client.getFileInstance(configFile);
		populate();
	}

	public void populate() throws SearchLibException, NoSuchAlgorithmException,
			IOException, URISyntaxException, XPathExpressionException,
			ParserConfigurationException, SAXException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		File contentFile = new File("resources/content_sample.xml");
		assertTrue(contentFile.exists());
		FileInputStream fis = new FileInputStream(contentFile);
		client.updateXmlDocuments(null, new InputSource(fis));
		fis.close();
		client.reload(null);
	}

	@Test
	public void matchAllDocs() throws Exception {
		SearchRequest searchRequest = client.getNewSearchRequest();
		searchRequest.setQueryString("*:*");
		Result result = client.search(searchRequest);
		assertTrue(result.getDocumentCount() > 0);
	}
}
