/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.mailbox;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class MailboxCrawlItem
		extends
		FieldMapCrawlItem<MailboxCrawlItem, MailboxCrawlThread, MailboxCrawlMaster> {

	private String serverName;

	private int serverPort;

	private String serverProtocol;

	private String user;

	private String password;

	private LanguageEnum lang;

	public MailboxCrawlItem(MailboxCrawlMaster crawlMaster) {
		super(crawlMaster, new MailboxFieldMap());
		serverName = null;
		serverPort = 110;
		serverProtocol = "pop";
		user = null;
		password = null;
		lang = LanguageEnum.UNDEFINED;
	}

	public MailboxCrawlItem(MailboxCrawlItem crawl) {
		this((MailboxCrawlMaster) crawl.threadMaster);
		crawl.copyTo(this);
	}

	@Override
	public void copyTo(MailboxCrawlItem item) {
		super.copyTo(item);
		item.serverName = this.serverName;
		item.serverPort = this.serverPort;
		item.serverProtocol = this.serverProtocol;
		item.user = this.user;
		item.password = this.password;
		item.lang = this.lang;
	}

	public String getInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append(user);
		sb.append(" - ");
		sb.append(serverName);
		return sb.toString();
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	protected final static String MBCRAWL_ATTR_SERVER_NAME = "serverName";
	protected final static String MBCRAWL_ATTR_SERVER_PROTOCOL = "serverProtocol";
	protected final static String MBCRAWL_ATTR_SERVER_PORT = "serverPort";
	protected final static String MBCRAWL_ATTR_USER = "user";
	protected final static String MBCRAWL_ATTR_PASSWORD = "password";
	protected final static String MBCRAWL_ATTR_LANG = "lang";
	protected final static String MBCRAWL_NODE_NAME_MAP = "map";

	public static MailboxCrawlItem fromXml(MailboxCrawlMaster crawlMaster,
			XPathParser xpp, Node item) throws XPathExpressionException {
		MailboxCrawlItem crawl = new MailboxCrawlItem(crawlMaster);
		crawl.setServerName(XPathParser.getAttributeString(item,
				MBCRAWL_ATTR_SERVER_NAME));
		crawl.setServerProtocol(XPathParser.getAttributeString(item,
				MBCRAWL_ATTR_SERVER_PROTOCOL));
		crawl.setServerPort(XPathParser.getAttributeValue(item,
				MBCRAWL_ATTR_SERVER_PORT));
		crawl.setUser(XPathParser.getAttributeString(item, MBCRAWL_ATTR_USER));
		crawl.setPassword(XPathParser.getAttributeString(item,
				MBCRAWL_ATTR_PASSWORD));
		crawl.setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				item, MBCRAWL_ATTR_LANG)));
		Node mapNode = xpp.getNode(item, MBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			crawl.getFieldMap().load(mapNode);
		return crawl;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(MBCRAWL_ATTR_SERVER_NAME, getServerName(),
				MBCRAWL_ATTR_SERVER_PROTOCOL, getServerProtocol(),
				MBCRAWL_ATTR_SERVER_PORT, Integer.toString(getServerPort()),
				MBCRAWL_ATTR_USER, getUser(), MBCRAWL_ATTR_PASSWORD,
				getPassword(), MBCRAWL_ATTR_LANG, getLang().getCode());
		xmlWriter.startElement(MBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(MailboxCrawlItem o) {
		return getInfo().compareTo(o.getInfo());
	}

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName
	 *            the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return the serverProtocol
	 */
	public String getServerProtocol() {
		return serverProtocol;
	}

	/**
	 * @param serverProtocol
	 *            the serverProtocol to set
	 */
	public void setServerProtocol(String serverProtocol) {
		this.serverProtocol = serverProtocol;
	}

}
