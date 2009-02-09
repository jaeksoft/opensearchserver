package com.jaeksoft.searchlib.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;

public class Full {

	protected File configFile = null;

	protected Client client = null;

	@Before
	public void before() {
		configFile = new File("resources/test_config.xml");
	}

	public Client getClient(boolean create) throws SearchLibException {
		return new Client(configFile, create);
	}

	public void open() throws SearchLibException {
		assertTrue(configFile.exists());
		client = getClient(false);
	}

	@Test
	public void create() throws SearchLibException {
		assertTrue(configFile.exists());
		client = getClient(true);
	}

	@Test
	public void populate() throws SearchLibException, NoSuchAlgorithmException,
			IOException, URISyntaxException {
		open();
		int lastFacet2 = 0;
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < i; j++) {
				int ref = i * 10 + j;
				int facet1 = 10 + i;
				int facet2 = 20 + i;
				IndexDocument document = new IndexDocument();
				document.add("id", ref);
				document.add("text1", "text " + ref + " " + facet1);
				document.add("text2", "text " + ref + " " + facet2);
				document.add("facet1", facet1);
				document.add("facet2", facet2);
				if (lastFacet2 != 0)
					document.add("facet2", lastFacet2);
				lastFacet2 = facet2;
				System.out.println(ref + " " + facet1 + " " + facet2);
				client.updateDocument(document);
			}
	}

	@Test
	public void query() throws Exception {
		open();
		Request request = client.getNewRequest("search");
		request.setQueryString("*:*");
		Result result = client.getIndex().search(request);
		RenderXml render = new RenderXml(result);
		PrintWriter pw = new PrintWriter(System.out);
		render.render(pw);
		pw.flush();
		pw.close();
	}

}
