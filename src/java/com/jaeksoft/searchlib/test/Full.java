package com.jaeksoft.searchlib.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.DomUtils;

public class Full {

	private Client client;

	@Before
	public void getInstance() throws SearchLibException,
			NoSuchAlgorithmException, XPathExpressionException, IOException,
			URISyntaxException, ParserConfigurationException, SAXException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, NamingException, HttpException,
			DOMException {
		File configFile = new File("resources/test_config.xml");
		assertTrue(configFile.exists());
		client = ClientCatalog.getClient("web_crawler");
		populate();
	}

	public void populate() throws SearchLibException, NoSuchAlgorithmException,
			IOException, URISyntaxException, XPathExpressionException,
			ParserConfigurationException, SAXException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		File contentFile = new File("resources/content_sample.xml");
		assertTrue(contentFile.exists());
		Node xmlDoc = DomUtils.readXml(new StreamSource(contentFile), false);
		client.updateXmlDocuments(xmlDoc, 10, null, null, null);
		client.reload();
	}

	@Test
	public void matchAllDocs() throws Exception {
		SearchRequest searchRequest = new SearchRequest(client);
		searchRequest.setQueryString("*:*");
		AbstractResultSearch result = (AbstractResultSearch) client
				.request(searchRequest);
		assertTrue(result.getDocumentCount() > 0);
	}
}
