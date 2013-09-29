/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class AbstractRequest {

	public final static String XML_NODE_REQUEST = "request";
	public final static String XML_ATTR_NAME = "name";
	public final static String XML_ATTR_TYPE = "type";

	protected final ReadWriteLock rwl = new ReadWriteLock();

	public final RequestTypeEnum requestType;

	private String requestName;
	protected Config config;
	private boolean withLogReport;
	private List<String> customLogs;
	private int timerMinTime;
	private int timerMaxDepth;

	protected AbstractRequest(Config config, RequestTypeEnum requestType) {
		this.config = config;
		this.requestType = requestType;
		this.requestName = null;
		setDefaultValues();
	}

	protected void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		this.config = config;
		this.requestName = XPathParser.getAttributeString(requestNode,
				XML_ATTR_NAME);
	}

	/**
	 * Build a new request by reading the XML config file
	 * 
	 * @param config
	 * @param xpp
	 * @param requestNode
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws ParseException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public final void fromXmlConfig(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			fromXmlConfigNoLock(config, xpp, requestNode);
		} finally {
			rwl.w.unlock();
		}
	}

	public void copyFrom(AbstractRequest request) {
		this.config = request.config;
		this.requestName = request.requestName;
		this.withLogReport = request.withLogReport;
		this.customLogs = null;
		if (request.customLogs != null)
			this.customLogs = new ArrayList<String>(request.customLogs);
		this.timerMinTime = request.timerMinTime;
		this.timerMaxDepth = request.timerMaxDepth;
	}

	public AbstractRequest duplicate() throws InstantiationException,
			IllegalAccessException {
		AbstractRequest newRequest = getClass().newInstance();
		newRequest.copyFrom(this);
		return newRequest;
	}

	protected void setDefaultValues() {
		withLogReport = false;
		customLogs = null;
		timerMinTime = 10;
		timerMaxDepth = 3;
	}

	public final RequestTypeEnum getType() {
		return requestType;
	}

	public void init(Config config) {
		rwl.w.lock();
		try {
			this.config = config;
		} finally {
			rwl.w.unlock();
		}
	}

	public Config getConfig() {
		rwl.r.lock();
		try {
			return this.config;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getRequestName() {
		rwl.r.lock();
		try {
			return this.requestName;
		} finally {
			rwl.r.unlock();
		}
	}

	public abstract Query getQuery() throws ParseException, SyntaxError,
			SearchLibException, IOException;

	public void setRequestName(String name) {
		rwl.w.lock();
		try {
			this.requestName = name;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setLogReport(boolean withLogReport) {
		rwl.w.lock();
		try {
			this.withLogReport = withLogReport;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isLogReport() {
		rwl.r.lock();
		try {
			return withLogReport;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addCustomLog(String p) {
		rwl.w.lock();
		try {
			if (customLogs == null)
				customLogs = new ArrayList<String>(0);
			customLogs.add(p);
		} finally {
			rwl.w.unlock();
		}
	}

	public List<String> getCustomLogs() {
		rwl.r.lock();
		try {
			return customLogs;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getNameType() {
		StringBuffer sb = new StringBuffer();
		sb.append(getType().getLabel());
		sb.append(": ");
		sb.append(requestName);
		return sb.toString();
	}

	public abstract String getInfo();

	protected abstract void resetNoLock();

	public final void reset() {
		rwl.w.lock();
		try {
			resetNoLock();
		} finally {
			rwl.w.unlock();
		}
	}

	public abstract void writeXmlConfig(XmlWriter xmlWriter)
			throws SAXException;

	protected abstract void setFromServletNoLock(ServletTransaction transaction)
			throws SyntaxError, SearchLibException;

	public final void setFromServlet(ServletTransaction transaction)
			throws SyntaxError, SearchLibException {
		rwl.w.lock();
		try {
			setFromServletNoLock(transaction);
		} finally {
			rwl.w.unlock();
		}
	}

	public abstract AbstractResult<?> execute(ReaderInterface reader)
			throws SearchLibException;

	/**
	 * @param timerMinTime
	 *            the timerMinTime to set
	 */
	public void setTimerMinTime(int timerMinTime) {
		this.timerMinTime = timerMinTime;
	}

	/**
	 * @return the timerMi,Time
	 */
	public int getTimerMinTime() {
		return timerMinTime;
	}

	/**
	 * @return the timerMaxDepth
	 */
	public int getTimerMaxDepth() {
		return timerMaxDepth;
	}

	/**
	 * @param timerMaxDepth
	 *            the timerMaxDepth to set
	 */
	public void setTimerMaxDepth(int timerMaxDepth) {
		this.timerMaxDepth = timerMaxDepth;
	}

}
