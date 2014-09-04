/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.crawler.MailboxAbstractCrawler;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class MailboxCrawlItem
		extends
		FieldMapCrawlItem<MailboxCrawlItem, MailboxCrawlThread, MailboxCrawlMaster> {

	private String name;

	private String serverName;

	private int serverPort;

	private String serverProtocol;

	private String user;

	private String password;

	private int bufferSize;

	private LanguageEnum lang;

	public MailboxCrawlItem(MailboxCrawlMaster crawlMaster) {
		super(crawlMaster, new MailboxFieldMap());
		name = null;
		bufferSize = 50;
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

	public MailboxCrawlItem duplicate() {
		return new MailboxCrawlItem(this);
	}

	@Override
	public void copyTo(MailboxCrawlItem item) {
		super.copyTo(item);
		item.name = this.name;
		item.serverName = this.serverName;
		item.serverPort = this.serverPort;
		item.serverProtocol = this.serverProtocol;
		item.user = this.user;
		item.password = this.password;
		item.lang = this.lang;
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
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

	protected final static String MBCRAWL_ATTR_NAME = "name";
	protected final static String MBCRAWL_ATTR_SERVER_NAME = "serverName";
	protected final static String MBCRAWL_ATTR_SERVER_PROTOCOL = "serverProtocol";
	protected final static String MBCRAWL_ATTR_SERVER_PORT = "serverPort";
	protected final static String MBCRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String MBCRAWL_ATTR_USER = "user";
	protected final static String MBCRAWL_ATTR_PASSWORD = "password";
	protected final static String MBCRAWL_ATTR_LANG = "lang";
	protected final static String MBCRAWL_NODE_NAME_MAP = "map";

	public static MailboxCrawlItem fromXml(MailboxCrawlMaster crawlMaster,
			XPathParser xpp, Node item) throws XPathExpressionException {
		MailboxCrawlItem crawl = new MailboxCrawlItem(crawlMaster);
		crawl.setName(XPathParser.getAttributeString(item, MBCRAWL_ATTR_NAME));
		crawl.setServerName(XPathParser.getAttributeString(item,
				MBCRAWL_ATTR_SERVER_NAME));
		crawl.setServerProtocol(XPathParser.getAttributeString(item,
				MBCRAWL_ATTR_SERVER_PROTOCOL));
		crawl.setServerPort(XPathParser.getAttributeValue(item,
				MBCRAWL_ATTR_SERVER_PORT));
		crawl.setBufferSize(XPathParser.getAttributeValue(item,
				MBCRAWL_ATTR_BUFFER_SIZE));
		crawl.setUser(XPathParser.getAttributeString(item, MBCRAWL_ATTR_USER));
		crawl.setPassword(StringUtils.base64decode(XPathParser
				.getAttributeString(item, MBCRAWL_ATTR_PASSWORD)));
		crawl.setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				item, MBCRAWL_ATTR_LANG)));
		Node mapNode = xpp.getNode(item, MBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			crawl.getFieldMap().load(mapNode);
		return crawl;
	}

	public void writeXml(String nodeName, XmlWriter xmlWriter)
			throws SAXException, UnsupportedEncodingException {
		xmlWriter.startElement(nodeName, MBCRAWL_ATTR_NAME, getName(),
				MBCRAWL_ATTR_SERVER_NAME, getServerName(),
				MBCRAWL_ATTR_SERVER_PROTOCOL, getServerProtocol(),
				MBCRAWL_ATTR_SERVER_PORT, Integer.toString(getServerPort()),
				MBCRAWL_ATTR_BUFFER_SIZE, Integer.toString(getBufferSize()),
				MBCRAWL_ATTR_USER, getUser(), MBCRAWL_ATTR_PASSWORD,
				StringUtils.base64encode(getPassword()), MBCRAWL_ATTR_LANG,
				getLang().getCode());
		xmlWriter.startElement(MBCRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(MailboxCrawlItem o) {
		return getName().compareTo(o.getName());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param itemName
	 *            the itemName to set
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public String toString() {
		return name;
	}

	public String check() throws InstantiationException,
			IllegalAccessException, MessagingException, IOException,
			SearchLibException {
		MailboxAbstractCrawler crawler = MailboxProtocolEnum.getNewCrawler(
				null, this);
		return crawler.check();
	}

}
