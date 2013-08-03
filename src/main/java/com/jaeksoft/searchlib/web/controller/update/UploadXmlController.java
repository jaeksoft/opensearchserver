/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.update;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UploadXmlController extends AbstractUploadController {

	public class UpdateXmlThread extends AbstractUpdateThread {

		private final String xsl;

		private UpdateXmlThread(Client client, StreamSource streamSource,
				String xsl, String mediaName) {
			super(client, streamSource, mediaName);
			this.xsl = xsl;
		}

		@Override
		public int doUpdate() throws SearchLibException, IOException {
			try {
				ProxyHandler proxyHandler = client.getWebPropertyManager()
						.getProxyHandler();
				Node xmlDoc;
				if (xsl != null && xsl.length() > 0) {
					tempResult = File.createTempFile("ossupload", ".xml");
					DomUtils.xslt(streamSource, xsl, tempResult);
					xmlDoc = DomUtils.readXml(new StreamSource(tempResult),
							false);
				} else {
					xmlDoc = DomUtils.readXml(streamSource, false);
				}
				return client.updateXmlDocuments(xmlDoc, 50, null,
						proxyHandler, this);
			} catch (TransformerException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (ParserConfigurationException e) {
				throw new SearchLibException(e);
			} catch (XPathExpressionException e) {
				throw new SearchLibException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (InstantiationException e) {
				throw new SearchLibException(e);
			} catch (IllegalAccessException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			}
		}

	}

	private boolean xslEnabled;

	private String xslContent;

	public UploadXmlController() throws SearchLibException {
		super(ScopeAttribute.UPDATE_XML_MAP);
	}

	@Override
	protected void reset() {
		xslEnabled = false;
		xslContent = null;
	}

	@Override
	protected AbstractUpdateThread newUpdateThread(Client client,
			StreamSource streamSource, String mediaName) {
		return new UpdateXmlThread(client, streamSource, xslContent, mediaName);
	}

	/**
	 * @return the xslEnabled
	 */
	public boolean isXslEnabled() {
		return xslEnabled;
	}

	/**
	 * @param xslEnabled
	 *            the xslEnabled to set
	 * @throws SearchLibException
	 */
	public void setXslEnabled(boolean xslEnabled) throws SearchLibException {
		this.xslEnabled = xslEnabled;
		this.xslContent = null;
		reload();
	}

	/**
	 * @return the xslContent
	 */
	public String getXslContent() {
		return xslContent;
	}

	/**
	 * @param xslContent
	 *            the xslContent to set
	 */
	public void setXslContent(String xslContent) {
		this.xslContent = xslContent;
	}
}
