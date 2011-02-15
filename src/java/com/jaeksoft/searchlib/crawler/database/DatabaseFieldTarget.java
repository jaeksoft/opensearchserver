/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database;

import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.Target;

public class DatabaseFieldTarget extends Target {

	private boolean removeTag;

	private boolean convertHtmlEntities;

	private boolean filePath;

	private boolean crawlUrl;

	private String filePathPrefix;

	public DatabaseFieldTarget(String targetName, boolean removeTag,
			boolean convertHtmlEntities, boolean filePath,
			String filePathPrefix, boolean crawlUrl) {
		super(targetName);
		this.removeTag = removeTag;
		this.convertHtmlEntities = convertHtmlEntities;
		this.filePathPrefix = filePathPrefix;
		this.filePath = filePath;
		this.crawlUrl = crawlUrl;
	}

	public DatabaseFieldTarget(String targetName, Node targetNode) {
		super(targetName);
		removeTag = false;
		convertHtmlEntities = false;
		List<Node> nodeList = DomUtils.getNodes(targetNode, "filter");
		for (Node node : nodeList) {
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node,
					"removeTag")))
				removeTag = true;
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node,
					"convertHtmlEntities")))
				convertHtmlEntities = true;
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node,
					"filePath")))
				filePath = true;
			filePathPrefix = DomUtils.getAttributeText(node, "filePathPrefix");
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node,
					"crawlUrl")))
				crawlUrl = true;
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "removeTag", removeTag ? "yes" : "no",
				"convertHtmlEntities", convertHtmlEntities ? "yes" : "no",
				"filePath", filePath ? "yes" : "no", "filePathPrefix",
				filePathPrefix, "crawlUrl", crawlUrl ? "yes" : "no");
		xmlWriter.endElement();
	}

	/**
	 * @param removeTag
	 *            the removeTag to set
	 */
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	/**
	 * @return the removeTag
	 */
	public boolean isRemoveTag() {
		return removeTag;
	}

	/**
	 * @param convertHtmlEntities
	 *            the convertHtmlEntities to set
	 */
	public void setConvertHtmlEntities(boolean convertHtmlEntities) {
		this.convertHtmlEntities = convertHtmlEntities;
	}

	/**
	 * @return the convertHtmlEntities
	 */
	public boolean isConvertHtmlEntities() {
		return convertHtmlEntities;
	}

	/**
	 * @return the filePathPrefix
	 */
	public String getFilePathPrefix() {
		return filePathPrefix;
	}

	/**
	 * @param filePathPrefix
	 *            the filePathPrefix to set
	 */
	public void setFilePathPrefix(String filePathPrefix) {
		this.filePathPrefix = filePathPrefix;
	}

	/**
	 * @param filePath
	 *            the FilePath to set
	 */
	public void setFilePath(boolean filePath) {
		this.filePath = filePath;
		if (!filePath)
			setFilePathPrefix(null);
	}

	/**
	 * @return the FilePath
	 */
	public boolean isFilePath() {
		return filePath;
	}

	/**
	 * @return true if the FilePath is not set
	 */
	public boolean isNotFilePath() {
		return !isFilePath();
	}

	/**
	 * @param crawlUrl
	 *            the crawlUrl to set
	 */
	public void setCrawlUrl(boolean crawlUrl) {
		this.crawlUrl = crawlUrl;
	}

	/**
	 * @return the crawlUrl
	 */
	public boolean isCrawlUrl() {
		return crawlUrl;
	}

	public boolean isNotCrawlUrl() {
		return !crawlUrl;
	}
}
