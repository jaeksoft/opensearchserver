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
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Mailer;
import com.jaeksoft.searchlib.util.FilesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.web.StartStopListener;

public class InstanceProperties {

	private final long maxDocumentLimit;

	private final long maxStorage;

	private final int maxIndexNumber;

	private final int minCrawlerDelay;

	private final int maxApiRate;

	private final int minApiDelay;

	private final int requestPerMonth;

	private int requestPerMonthCount;

	private long lastTimeRequestPerMonthStore;

	private final File requestperMonthFile;

	private long nextApiTime;

	private int countApiCall;

	private int countApiWait;

	private final boolean chroot;

	private final static String LIMIT_NODEPATH = "/instanceProperties/limit";

	private final static String LIMIT_CHROOT_ATTR = "chroot";

	private final static String LIMIT_MAXDOCUMENTLIMIT_ATTR = "maxDocumentLimit";

	private final static String LIMIT_MAX_STORAGE_ATTR = "maxStorage";

	private final static String LIMIT_MAX_INDEX_NUMBER_ATTR = "maxIndexNumber";

	private final static String LIMIT_MINCRAWLERDELAY_ATTR = "minCrawlerDelay";

	private final static String LIMIT_MAX_API_RATE = "maxApiRate";

	private final static String LIMIT_REQUEST_PER_MONTH = "requestPerMonth";

	private final static String MAILER_URL = "mailerUrl";

	private final static String MAILER_FROM_EMAIL = "mailerFromEmail";

	private final static String MAILER_FROM_NAME = "mailerFromName";

	private final Mailer mailer;

	public InstanceProperties(File xmlFile)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, URISyntaxException {
		nextApiTime = System.currentTimeMillis();
		countApiCall = 0;
		countApiWait = 0;
		lastTimeRequestPerMonthStore = 0;
		requestPerMonthCount = 0;
		if (xmlFile.exists()) {
			requestperMonthFile = new File(xmlFile.getParent(),
					"requestPerMonth.txt");
			loadRequestPerMonth();
			XPathParser xpp = new XPathParser(xmlFile);
			Node node = xpp.getNode(LIMIT_NODEPATH);
			if (node != null) {
				maxDocumentLimit = XPathParser.getAttributeLong(node,
						LIMIT_MAXDOCUMENTLIMIT_ATTR);
				chroot = "yes".equalsIgnoreCase(XPathParser.getAttributeString(
						node, LIMIT_CHROOT_ATTR));
				minCrawlerDelay = XPathParser.getAttributeValue(node,
						LIMIT_MINCRAWLERDELAY_ATTR);
				maxIndexNumber = XPathParser.getAttributeValue(node,
						LIMIT_MAX_INDEX_NUMBER_ATTR);
				maxStorage = XPathParser.getAttributeLong(node,
						LIMIT_MAX_STORAGE_ATTR);
				maxApiRate = XPathParser.getAttributeValue(node,
						LIMIT_MAX_API_RATE);
				requestPerMonth = XPathParser.getAttributeValue(node,
						LIMIT_REQUEST_PER_MONTH);
				minApiDelay = maxApiRate != 0 ? 1000 / maxApiRate : 0;
				mailer = new Mailer(XPathParser.getAttributeString(node,
						MAILER_URL), XPathParser.getAttributeString(node,
						MAILER_FROM_EMAIL), XPathParser.getAttributeString(
						node, MAILER_FROM_NAME));
				return;
			}
		} else
			requestperMonthFile = null;
		maxDocumentLimit = 0;
		chroot = false;
		minCrawlerDelay = 0;
		maxIndexNumber = 0;
		maxStorage = 0;
		maxApiRate = 0;
		minApiDelay = 0;
		requestPerMonth = 0;
		mailer = null;
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
	 * @return the maxStorage
	 */
	public long getMaxStorage() {
		return maxStorage;
	}

	/**
	 * @return the maxIndexNumber
	 */
	public int getMaxIndexNumber() {
		return maxIndexNumber;
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
		return FilesUtils.isSubDirectory(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, file);
	}

	public final void checkChroot(File file) throws IOException,
			SearchLibException {
		if (!checkChrootQuietly(file))
			throw new SearchLibException(
					"You are not allowed to reach this location in the file system: "
							+ file.getAbsolutePath());
	}

	public final void checkMaxDocumentLimit() throws SearchLibException,
			IOException {
		if (maxDocumentLimit == 0)
			return;
		long count = ClientCatalog.countAllDocuments();
		if (count < maxDocumentLimit)
			return;
		throw new SearchLibException(
				"The maximum number of allowable documents has been reached ("
						+ maxDocumentLimit + ")");
	}

	public final void checkMaxIndexNumber() throws SearchLibException,
			IOException {
		if (maxIndexNumber == 0)
			return;
		long count = ClientCatalog.getClientCatalog(null).size();
		if (count < maxIndexNumber)
			return;
		throw new SearchLibException(
				"The maximum number of allowable index has been reached ("
						+ maxIndexNumber + ")");
	}

	public final void checkMaxStorageLimit() throws SearchLibException {
		if (maxStorage == 0)
			return;
		long size = ClientCatalog.calculateInstanceSize();
		if (size <= maxStorage)
			return;
		throw new SearchLibException(
				"The maximum storage size has been reached ("
						+ StringUtils.humanBytes(maxStorage) + ")");
	}

	protected final void checkApiRate() throws InterruptedException {
		if (minApiDelay == 0)
			return;
		countApiCall++;
		if (countApiCall == 1000) {
			countApiCall = 1;
			countApiWait = 0;
		}
		long newTime = System.currentTimeMillis();
		long sleep = nextApiTime - newTime;
		nextApiTime = newTime + minApiDelay;
		if (sleep > 0) {
			Thread.sleep(sleep);
			countApiWait++;
		}
	}

	private final void loadRequestPerMonth() throws IOException {
		if (!requestperMonthFile.exists())
			return;
		String s = FileUtils.readFileToString(requestperMonthFile);
		if (s != null)
			requestPerMonthCount = Integer.parseInt(s);
	}

	private final void storeRequestPerMonthCount(long t) throws IOException {
		synchronized (requestperMonthFile) {
			if (!requestperMonthFile.exists())
				resetRequestPerMonthCount();
			FileUtils.write(requestperMonthFile,
					Integer.toString(getRequestPerMonthCount()));
			lastTimeRequestPerMonthStore = t + 10000;
		}
	}

	private ReadWriteLock requestPerMonthLock = new ReadWriteLock();

	private final void incRequestPerMonthCount() {
		requestPerMonthLock.w.lock();
		try {
			requestPerMonthCount++;
		} finally {
			requestPerMonthLock.w.unlock();
		}
	}

	private final void resetRequestPerMonthCount() {
		requestPerMonthLock.w.lock();
		try {
			requestPerMonthCount = 0;
		} finally {
			requestPerMonthLock.w.unlock();
		}
	}

	/**
	 * @return the requestPerMonthCount
	 */
	public int getRequestPerMonthCount() {
		requestPerMonthLock.r.lock();
		try {
			return requestPerMonthCount;
		} finally {
			requestPerMonthLock.r.unlock();
		}
	}

	public final int getRequestPerMonth() {
		return requestPerMonth;
	}

	public final boolean isRequestPerMonth() {
		return requestPerMonth > 0;
	}

	protected final void checkApiRequestPerMonth() throws IOException {
		if (requestPerMonth == 0)
			return;
		long t = System.currentTimeMillis();
		incRequestPerMonthCount();
		if (t < lastTimeRequestPerMonthStore)
			return;
		storeRequestPerMonthCount(t);
	}

	public final void checkApi() throws InterruptedException, IOException {
		checkApiRate();
		checkApiRequestPerMonth();
	}

	public final float getApiWaitRate() {
		return (float) (((float) countApiWait / (float) countApiCall) * 100);
	}

	public final boolean isMaxApiRate() {
		return maxApiRate > 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(" - maxDocumentLimit: ");
		sb.append(maxDocumentLimit);
		sb.append(" - maxStorage: ");
		sb.append(StringUtils.humanBytes(maxStorage));
		sb.append(" - maxIndexNumber: ");
		sb.append(maxIndexNumber);
		sb.append(" - minCrawlerDelay: ");
		sb.append(minCrawlerDelay);
		sb.append(" - maxApiRate: ");
		sb.append(maxApiRate);
		sb.append(" - chroot: ");
		sb.append(chroot);
		return sb.toString();
	}

	/**
	 * @return the mailer
	 */
	public Mailer getMailer() {
		return mailer;
	}

}
