/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2016 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import com.jaeksoft.searchlib.util.*;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.webservice.ApiIdentifier;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import redis.clients.jedis.Jedis;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	private final String redisApiServerHostname;

	private final int redisApiServerPort;

	private final String redisApiAuth;

	private final boolean chroot;

	private final boolean disableScheduler;

	private final boolean disableWebCrawler;

	private final boolean disableFileCrawler;

	private final String silentBackupUrl;

	private final static String REPLICATION_NODEPATH = "/instanceProperties/replication";

	private final static String LIMIT_NODEPATH = "/instanceProperties/limit";

	private final static String LIMIT_CHROOT_ATTR = "chroot";

	private final static String LIMIT_MAXDOCUMENTLIMIT_ATTR = "maxDocumentLimit";

	private final static String LIMIT_MAX_STORAGE_ATTR = "maxStorage";

	private final static String LIMIT_MAX_INDEX_NUMBER_ATTR = "maxIndexNumber";

	private final static String LIMIT_MINCRAWLERDELAY_ATTR = "minCrawlerDelay";

	private final static String LIMIT_MAX_API_RATE = "maxApiRate";

	private final static String LIMIT_REQUEST_PER_MONTH = "requestPerMonth";

	private final static String DISABLE_SCHEDULER = "disableScheduler";

	private final static String DISABLE_WEBCRAWLER = "disableWebCrawler";

	private final static String DISABLE_FILECRAWLER = "disableFileCrawler";

	private final static String SILENT_BACKUP_URL = "silentBackupUrl";

	private final static String REDIS_API_NODE = "/instanceProperties/redisApi";

	private final static String REDIS_API_HOSTNAME_ATTR = "hostname";

	private final static String REDIS_API_PORT_ATTR = "port";

	private final static String REDIS_API_AUTH_ATTR = "auth";

	public InstanceProperties(File xmlFile)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
			URISyntaxException {
		nextApiTime = System.currentTimeMillis();
		countApiCall = 0;
		countApiWait = 0;
		lastTimeRequestPerMonthStore = 0;
		requestPerMonthCount = 0;
		if (xmlFile.exists()) {
			requestperMonthFile = new File(xmlFile.getParent(), "requestPerMonth.txt");
			loadRequestPerMonth();
			XPathParser xpp = new XPathParser(xmlFile);
			Node node = xpp.getNode(LIMIT_NODEPATH);
			if (node != null) {
				maxDocumentLimit = XPathParser.getAttributeLong(node, LIMIT_MAXDOCUMENTLIMIT_ATTR);
				chroot = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node, LIMIT_CHROOT_ATTR));
				minCrawlerDelay = XPathParser.getAttributeValue(node, LIMIT_MINCRAWLERDELAY_ATTR);
				maxIndexNumber = XPathParser.getAttributeValue(node, LIMIT_MAX_INDEX_NUMBER_ATTR);
				maxStorage = XPathParser.getAttributeLong(node, LIMIT_MAX_STORAGE_ATTR);
				maxApiRate = XPathParser.getAttributeValue(node, LIMIT_MAX_API_RATE);
				requestPerMonth = XPathParser.getAttributeValue(node, LIMIT_REQUEST_PER_MONTH);
				minApiDelay = maxApiRate != 0 ? 1000 / maxApiRate : 0;
				disableScheduler = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node, DISABLE_SCHEDULER));
				disableWebCrawler = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node, DISABLE_WEBCRAWLER));
				disableFileCrawler = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node, DISABLE_FILECRAWLER));
			} else {
				maxDocumentLimit = 0;
				chroot = false;
				minCrawlerDelay = 0;
				maxIndexNumber = 0;
				maxStorage = 0;
				maxApiRate = 0;
				requestPerMonth = 0;
				minApiDelay = 0;
				disableScheduler = false;
				disableWebCrawler = false;
				disableFileCrawler = false;
			}

			node = xpp.getNode(REPLICATION_NODEPATH);
			if (node != null) {
				silentBackupUrl = XPathParser.getAttributeString(node, SILENT_BACKUP_URL);
			} else {
				silentBackupUrl = null;
			}

			node = xpp.getNode(REDIS_API_NODE);
			if (node != null) {
				redisApiServerHostname = XPathParser.getAttributeString(node, REDIS_API_HOSTNAME_ATTR);
				redisApiServerPort = XPathParser.getAttributeValue(node, REDIS_API_PORT_ATTR);
				redisApiAuth = XPathParser.getAttributeString(node, REDIS_API_AUTH_ATTR);
			} else {
				redisApiServerHostname = null;
				redisApiServerPort = 0;
				redisApiAuth = null;
			}

		} else {
			requestperMonthFile = null;
			maxDocumentLimit = 0;
			chroot = false;
			minCrawlerDelay = 0;
			maxIndexNumber = 0;
			maxStorage = 0;
			maxApiRate = 0;
			requestPerMonth = 0;
			minApiDelay = 0;
			redisApiServerHostname = null;
			redisApiServerPort = 0;
			redisApiAuth = null;
			disableScheduler = false;
			disableWebCrawler = false;
			disableFileCrawler = false;
			silentBackupUrl = null;
		}
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

	public boolean isDisableScheduler() {
		return disableScheduler;
	}

	public boolean isDisableWebCrawler() {
		return disableWebCrawler;
	}

	public boolean isDisableFileCrawler() {
		return disableFileCrawler;
	}

	public final boolean checkChrootQuietly(File file) throws IOException {
		if (!chroot)
			return true;
		return FileUtils.isSubDirectory(StartStopListener.OPENSEARCHSERVER_DATA_FILE, file);
	}

	public final void checkChroot(File file) throws IOException {
		if (!checkChrootQuietly(file))
			throw new IOException(
					"You are not allowed to reach this location in the file system: " + file.getAbsolutePath());
	}

	public final void checkMaxDocumentLimit() throws SearchLibException, IOException {
		if (maxDocumentLimit == 0)
			return;
		long count = ClientCatalog.countAllDocuments();
		if (count < maxDocumentLimit)
			return;
		throw new SearchLibException(
				"The maximum number of allowable documents has been reached (" + maxDocumentLimit + ")");
	}

	public final void checkMaxIndexNumber() throws SearchLibException, IOException {
		if (maxIndexNumber == 0)
			return;
		long count = ClientCatalog.getClientCatalog(null).size();
		if (count < maxIndexNumber)
			return;
		throw new SearchLibException("The maximum number of allowable index has been reached (" + maxIndexNumber + ")");
	}

	public final void checkMaxStorageLimit() throws SearchLibException {
		if (maxStorage == 0)
			return;
		long size = ClientCatalog.calculateInstanceSize();
		if (size <= maxStorage)
			return;
		throw new SearchLibException(
				"The maximum storage size has been reached (" + StringUtils.humanBytes(maxStorage) + ")");
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
		String s = FileUtils.readFileToString(requestperMonthFile, "UTF-8");
		if (s == null)
			return;
		try {
			requestPerMonthCount = Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			Logging.warn(e);
		}
	}

	private final void storeRequestPerMonthCount(long t) throws IOException {
		synchronized (requestperMonthFile) {
			if (!requestperMonthFile.exists())
				resetRequestPerMonthCount();
			FileUtils.write(requestperMonthFile, Integer.toString(getRequestPerMonthCount()), "UTF-8");
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

	private final static SimpleDateFormat REDIS_STATS_DAYFORMAT = new SimpleDateFormat("yyyyMMdd");

	protected final void checkRedisApi(String apiKey, ApiIdentifier apiId, String remoteIpAddress)
			throws WebApplicationException {
		if (redisApiServerHostname == null)
			return;
		if (apiKey == null || apiKey.length() == 0)
			throw new WebApplicationException(Status.FORBIDDEN);
		Jedis jedis = new Jedis(redisApiServerHostname, redisApiServerPort, 10000);
		try {
			if (redisApiAuth != null && redisApiAuth.length() > 0)
				jedis.auth(redisApiAuth);
			String[] parts = StringUtils.split(apiKey, '_');
			if (parts.length < 2)
				throw new WebApplicationException(Status.FORBIDDEN);
			StringBuilder sbKey = new StringBuilder("apiAccountService.");
			sbKey.append(parts[1]);
			sbKey.append('.');
			sbKey.append(apiId);
			String skey = sbKey.toString();
			String v = jedis.hget(skey, apiKey);
			if (v == null)
				throw new WebApplicationException(Status.FORBIDDEN);
			if (remoteIpAddress.equals("0:0:0:0:0:0:0:1"))
				remoteIpAddress = "127.0.0.1";
			boolean bAllowed = "0".equals(v) || v.length() == 0;
			if (!bAllowed)
				for (SubnetInfo subnetInfo : NetworksUtils.getSubnetArray(v))
					if (subnetInfo.isInRange(remoteIpAddress))
						bAllowed = true;
			if (!bAllowed) {
				Logging.warn("Authentication failure: " + apiKey + " " + remoteIpAddress);
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			String statKey;
			synchronized (REDIS_STATS_DAYFORMAT) {
				statKey = REDIS_STATS_DAYFORMAT.format(new Date());
			}
			jedis.hincrBy(skey + ".stats", statKey, 1);
		} catch (IOException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		} finally {
			if (jedis != null) {
				if (jedis.isConnected())
					jedis.disconnect();
				IOUtils.close(jedis);
			}
		}
	}

	public final void checkApi() throws InterruptedException, IOException {
		checkApiRate();
		checkApiRequestPerMonth();
	}

	public final void checkApi(String apiKey, ApiIdentifier apiId, String remoteAddr)
			throws WebApplicationException, InterruptedException {
		checkApiRate();
		checkRedisApi(apiKey, apiId, remoteAddr);
	}

	public final float getApiWaitRate() {
		return (float) (((float) countApiWait / (float) countApiCall) * 100);
	}

	public final boolean isMaxApiRate() {
		return maxApiRate > 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
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
	 * @return the redisApiServerHostname
	 */
	public String getRedisApiServerHostname() {
		return redisApiServerHostname;
	}

	/**
	 * @return the redisApiServerPort
	 */
	public int getRedisApiServerPort() {
		return redisApiServerPort;
	}

	public String getSilentBackupUrl() {
		return silentBackupUrl;
	}
}
