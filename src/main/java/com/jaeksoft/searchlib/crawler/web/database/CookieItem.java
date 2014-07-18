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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.net.InternetDomainName;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CookieItem extends AbstractPatternNameValueItem {

	private BasicClientCookie basicClientCookie = null;

	public CookieItem() {
	}

	public CookieItem(BasicClientCookie basicClientCookie) {
		this.pattern = null;
		this.name = basicClientCookie.getName();
		this.value = basicClientCookie.getValue();
		this.basicClientCookie = basicClientCookie;
	}

	public CookieItem(Node node) {
		super(node);
		basicClientCookie = null;
	}

	@Override
	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		super.writeXml(CookieManager.ITEM_NODE_NAME, xmlWriter);
	}

	public BasicClientCookie getCookie() throws MalformedURLException,
			URISyntaxException {
		if (basicClientCookie != null)
			return basicClientCookie;
		basicClientCookie = new BasicClientCookie(name, value);
		String domain_attr = StringUtils.fastConcat(".",
				InternetDomainName.from(extractUrl().getHost()));
		basicClientCookie.setDomain(domain_attr);
		return basicClientCookie;
	}

	@Override
	protected void changeEvent() {
		basicClientCookie = null;
	}

}
