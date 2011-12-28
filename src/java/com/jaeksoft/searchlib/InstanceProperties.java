package com.jaeksoft.searchlib;

/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.FileDirUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.web.StartStopListener;

public class InstanceProperties {

	private long maxDocumentLimit = 0;

	private int minCrawlerDelay = 0;

	private boolean chroot = false;

	private final static String LIMIT_NODEPATH = "/instanceProperties/limit";

	private final static String LIMIT_CHROOT_ATTR = "chroot";

	private final static String LIMIT_MAXDOCUMENTLIMIT_ATTR = "maxDocumentLimit";

	private final static String LIMIT_MINCRAWLERDELAY_ATTR = "minCrawlerDelay";

	public InstanceProperties(File xmlFile)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		if (!xmlFile.exists())
			return;
		XPathParser xpp = new XPathParser(xmlFile);
		Node node = xpp.getNode(LIMIT_NODEPATH);
		if (node == null)
			return;
		maxDocumentLimit = XPathParser.getAttributeLong(node,
				LIMIT_MAXDOCUMENTLIMIT_ATTR);
		chroot = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node,
				LIMIT_CHROOT_ATTR));
		minCrawlerDelay = XPathParser.getAttributeValue(node,
				LIMIT_MINCRAWLERDELAY_ATTR);
	}

	/**
	 * @return the maxDocumentLimit
	 */
	public long getMaxDocumentLimit() {
		return maxDocumentLimit;
	}

	/**
	 * @return the minCrawlerDelay
	 */
	public int getMinCrawlerDelay() {
		return minCrawlerDelay;
	}

	/**
	 * @return the chroot
	 */
	public boolean isChroot() {
		return chroot;
	}

	public final boolean checkChrootQuietly(File file) throws IOException {
		if (!chroot)
			return true;
		return FileDirUtils.isSubDirectory(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, file);
	}

	public final void checkChroot(File file) throws IOException,
			SearchLibException {
		if (!checkChrootQuietly(file))
			throw new SearchLibException(
					"You are not allowed to reach this location in the file system: "
							+ file.getAbsolutePath());
	}

	public final void checkMaxDocumentLimit(long count)
			throws SearchLibException {
		if (maxDocumentLimit == 0)
			return;
		if (count >= maxDocumentLimit)
			throw new SearchLibException(
					"The maximum number of allowable documents has been reached ("
							+ maxDocumentLimit + ")");
	}

}
