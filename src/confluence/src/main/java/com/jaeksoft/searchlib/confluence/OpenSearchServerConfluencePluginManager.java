package com.jaeksoft.searchlib.confluence;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

public class OpenSearchServerConfluencePluginManager {

	private PageManager pageManager = null;
	private SpaceManager spaceManager = null;
	private SettingsManager settingsManager = null;
	private File tempFile = null;

	public OpenSearchServerConfluencePluginManager(

	SpaceManager spaceManager, PageManager pageManager,
			SettingsManager settingsManager) throws IOException {
		this.spaceManager = spaceManager;
		this.pageManager = pageManager;
		this.settingsManager = settingsManager;

	}

	public void createIndexFile() throws ParserConfigurationException,
			TransformerException {
		try {
			tempFile = File.createTempFile("oss", ".xml");
			String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element index = doc.createElement("index");
			List<Space> spaces = spaceManager.getAllSpaces();
			for (Space space : spaces) {
				@SuppressWarnings("unchecked")
				List<Page> pages = pageManager.getPages(space, true);
				doc.appendChild(index);
				for (Page page : pages) {
					Element document = doc.createElement("document");
					index.appendChild(document);
					Attr lang = doc.createAttribute("lang");
					lang.setValue("en");
					document.setAttributeNode(lang);
					getFieldDocument("title", page.getTitle().trim(), doc,
							document, true);
					getFieldDocument("content", page
							.getBodyAsStringWithoutMarkup().trim(), doc,
							document, true);
					getFieldDocument("url", baseUrl + page.getUrlPath().trim(),
							doc, document, false);
					getFieldDocument("id", baseUrl + page.getUrlPath().trim(),
							doc, document, false);
					getFieldDocument("type", "documentation", doc, document,
							false);
					getFieldDocument("lang", "en", doc, document, false);
					if (page.getCurrentDate() != null) {
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyyMMddHHmmss");
						String dateNow = formatter
								.format(page.getCurrentDate());
						getFieldDocument("content_timestamp", dateNow, doc,
								document, false);
					}
				}
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer;
				transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult resultXml = new StreamResult(tempFile);
				transformer.transform(source, resultXml);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(OpenSearchServerSettings settings) {
		try {
			String url = settings.getServerurl() + "/update?use="
					+ settings.getIndexname() + "&login="
					+ settings.getUsername() + "&key=" + settings.getKey();
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut put = new HttpPut(url);
			put.setEntity(new FileEntity(tempFile.getAbsoluteFile(), "text/xml"));
			httpClient.execute(put);
			// tempFile.deleteOnExit();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public void getFieldDocument(String nameString, String valueString,
			Document doc, Element index, boolean withCDATA) {
		Element field = doc.createElement("field");
		index.appendChild(field);
		Attr name = doc.createAttribute("name");
		name.setValue(nameString);
		field.setAttributeNode(name);

		Element value = doc.createElement("value");
		if (withCDATA)
			value.appendChild(doc.createCDATASection(valueString));
		else
			value.appendChild(doc.createTextNode(valueString));
		field.appendChild(value);
	}
}
