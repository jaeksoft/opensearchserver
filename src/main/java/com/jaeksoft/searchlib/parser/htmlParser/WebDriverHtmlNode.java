/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.htmlParser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.util.List;

public class WebDriverHtmlNode extends HtmlNodeAbstract<WebElement> {

	public WebDriverHtmlNode(WebElement node) {
		super(node);
	}

	@Override
	final public int countElements() {
		List<WebElement> webElements = node.findElements(By.xpath(".//"));
		if (webElements == null)
			return 0;
		return webElements.size();
	}

	private By.ByTagName[] getByTagNameArray(String... tagnames) {
		if (tagnames == null)
			return null;
		if (tagnames.length == 0)
			return null;
		By.ByTagName[] byTagNames = new By.ByTagName[tagnames.length];
		int i = 0;
		for (String tagname : tagnames)
			byTagNames[i++] = new By.ByTagName(tagname);
		return byTagNames;
	}

	private List<WebElement> getChainedWebElements(String... path) {
		By.ByTagName[] byTagNames = getByTagNameArray(path);
		if (byTagNames == null)
			return null;
		return node.findElements(new ByChained(byTagNames));
	}

	private List<WebElement> getAllWebElements(String... tags) {
		By.ByTagName[] byTagNames = getByTagNameArray(tags);
		if (byTagNames == null)
			return null;
		return node.findElements(new ByAll(byTagNames));
	}

	@Override
	public String getFirstTextNode(String... path) {
		final List<WebElement> webElements = getChainedWebElements(path);
		if (webElements == null)
			return null;
		return webElements.get(0).getText();
	}

	@Override
	public String getText() {
		return node.getText();
	}

	@Override
	public String getAttributeText(String name) {
		return node.getAttribute(name);
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<WebElement>> nodes, String... tagNames) {
		final List<WebElement> webElements = getChainedWebElements(tagNames);
		if (webElements == null)
			return;
		for (WebElement webElement : webElements)
			nodes.add(new WebDriverHtmlNode(webElement));
	}

	@Override
	public List<HtmlNodeAbstract<WebElement>> getAllNodes(String... tags) {
		final List<HtmlNodeAbstract<WebElement>> nodes = getNewNodeList();
		final List<WebElement> webElements = getAllWebElements(tags);
		if (webElements != null)
			for (WebElement webElement : webElements)
				nodes.add(new WebDriverHtmlNode(webElement));
		return nodes;
	}

	@Override
	protected List<HtmlNodeAbstract<WebElement>> getNewChildNodes() {
		final List<HtmlNodeAbstract<WebElement>> nodes = getNewNodeList();
		final List<WebElement> webElements = node.findElements(By.xpath(".//"));
		if (webElements != null)
			for (WebElement webElement : webElements)
				nodes.add(new WebDriverHtmlNode(webElement));
		return nodes;
	}

	@Override
	public boolean isComment() {
		return false;
	}

	@Override
	public boolean isTextNode() {
		String text = getText();
		if (text == null)
			return false;
		return text.length() > 0;
	}

	@Override
	public String getNodeName() {
		return node.getTagName();
	}

	@Override
	public String getAttribute(String name) {
		return node.getAttribute(name);
	}

	@Override
	public String generatedSource() {
		return node.toString();
	}
}
