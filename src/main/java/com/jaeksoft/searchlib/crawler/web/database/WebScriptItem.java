/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.UnsupportedEncodingException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class WebScriptItem extends AbstractPatternNameValueItem {

	private Script script;

	public WebScriptItem() {
		script = null;
	}

	public WebScriptItem(Node node) {
		script = null;
		String s = DomUtils.getText(node);
		if (!StringUtils.isEmpty(s))
			setValue(StringUtils.base64decode(s));
		setName(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "name")));
		setPattern(StringUtils.base64decode(DomUtils.getAttributeText(node,
				"pattern")));
	}

	@Override
	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		xmlWriter.startElement(
				WebScriptManager.ITEM_NODE_NAME,
				"name",
				new String(StringUtils.base64encode(name)),
				"pattern",
				pattern == null ? null : new String(StringUtils
						.base64encode(pattern)));
		if (value != null)
			xmlWriter.textNode(StringUtils.base64encode(value));
		xmlWriter.endElement();
	}

	@Override
	protected void changeEvent() {
		GroovyShell shell = new GroovyShell();
		script = shell.parse(value);
	}

	public void exec(HttpDownloader httpDownloader) {
		if (script == null) {
			GroovyShell shell = new GroovyShell();
			script = shell.parse(getValue());
		}
		Binding binding = new Binding();
		binding.setVariable("downloader", httpDownloader);
		script.setBinding(binding);
		script.run();
	}
}
