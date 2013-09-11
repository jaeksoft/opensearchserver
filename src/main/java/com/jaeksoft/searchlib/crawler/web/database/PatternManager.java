/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class PatternManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	// For better performances, pattern are grouped by hostname in a map
	private Map<String, List<PatternItem>> patternMap = null;

	private File patternFile;

	public PatternManager(File indexDir, String filename)
			throws SearchLibException {
		patternFile = new File(indexDir, filename);
		patternMap = new TreeMap<String, List<PatternItem>>();
		try {
			load();
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	private void load() throws ParserConfigurationException, SAXException,
			IOException, XPathExpressionException, SearchLibException,
			URISyntaxException {
		if (!patternFile.exists())
			return;
		XPathParser xpp = new XPathParser(patternFile);
		NodeList nodeList = xpp.getNodeList("/patterns/pattern");
		int l = nodeList.getLength();
		List<PatternItem> patternList = new ArrayList<PatternItem>(l);
		for (int i = 0; i < l; i++)
			patternList
					.add(new PatternItem(DomUtils.getText(nodeList.item(i))));
		addListWithoutStoreAndLock(patternList, true);
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!patternFile.exists())
			patternFile.createNewFile();
		PrintWriter pw = new PrintWriter(patternFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("patterns");
			Iterator<List<PatternItem>> it = patternMap.values().iterator();
			while (it.hasNext()) {
				for (PatternItem item : it.next()) {
					xmlWriter.startElement("pattern");
					xmlWriter.textNode(item.getPattern());
					xmlWriter.endElement();
				}
			}
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	private void addListWithoutStoreAndLock(List<PatternItem> patternList,
			boolean bDeleteAll) throws SearchLibException,
			MalformedURLException, URISyntaxException {
		if (bDeleteAll)
			patternMap.clear();
		if (patternList == null)
			return;
		for (PatternItem item : patternList) {
			if (!bDeleteAll && findPattern(item) != null)
				item.setStatus(PatternItem.Status.ALREADY);
			else {
				addPatternWithoutLock(item);
				item.setStatus(PatternItem.Status.INJECTED);
			}
		}
	}

	public void addList(List<PatternItem> patternList, boolean bDeleteAll)
			throws SearchLibException {
		rwl.w.lock();
		try {
			addListWithoutStoreAndLock(patternList, bDeleteAll);
			store();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	private int delPatternWithoutLock(String sPattern)
			throws MalformedURLException, URISyntaxException {
		String host = new PatternItem(sPattern).getHost();
		List<PatternItem> itemList = patternMap.get(host);
		if (itemList == null)
			return 0;
		int count = 0;
		Iterator<PatternItem> it = itemList.iterator();
		while (it.hasNext()) {
			if (it.next().sPattern.equals(sPattern)) {
				it.remove();
				count++;
			}
		}
		return count;
	}

	public int delPattern(Collection<String> patterns)
			throws SearchLibException {
		rwl.w.lock();
		try {
			int count = 0;
			for (String pattern : patterns)
				count += delPatternWithoutLock(pattern);
			store();
			return count;
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void delPatternItem(Collection<PatternItem> patterns)
			throws SearchLibException {
		rwl.w.lock();
		try {
			for (PatternItem pattern : patterns)
				delPatternWithoutLock(pattern.sPattern);
			store();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}

	}

	private void addPatternWithoutLock(PatternItem patternItem)
			throws MalformedURLException, URISyntaxException {
		String host = patternItem.getHost();
		List<PatternItem> itemList = patternMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<PatternItem>();
			patternMap.put(host, itemList);
		}
		itemList.add(patternItem);
	}

	public void addPattern(PatternItem patternItem) throws SearchLibException {
		rwl.w.lock();
		try {
			addPatternWithoutLock(patternItem);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public int getPatterns(String startsWith, long start, long rows,
			List<PatternItem> list) throws SearchLibException {
		rwl.r.lock();
		try {
			if (StringUtils.isEmpty(startsWith))
				startsWith = null;
			Iterator<List<PatternItem>> it = patternMap.values().iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (PatternItem item : it.next()) {
					if (startsWith != null) {
						if (!item.getPattern().startsWith(startsWith)) {
							pos++;
							continue;
						}
					}
					if (rows == 0 || pos < end) {
						if (pos >= start)
							list.add(item);
					}
					total++;
					pos++;
				}
			return total;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getPatterns(String startsWith, List<String> list)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (StringUtils.isEmpty(startsWith))
				startsWith = null;
			Iterator<List<PatternItem>> it = patternMap.values().iterator();
			int total = 0;
			while (it.hasNext())
				for (PatternItem item : it.next()) {
					if (startsWith != null)
						if (!item.getPattern().startsWith(startsWith))
							continue;
					list.add(item.sPattern);
					total++;
				}
			return total;
		} finally {
			rwl.r.unlock();
		}
	}

	private PatternItem findPattern(PatternItem pattern)
			throws MalformedURLException, URISyntaxException {
		rwl.r.lock();
		try {
			List<PatternItem> patternList = patternMap.get(pattern.getHost());
			if (patternList == null)
				return null;
			String sPattern = pattern.getPattern();
			for (PatternItem patternItem : patternList)
				if (patternItem.getPattern().equals(sPattern))
					return patternItem;
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	final public boolean matchPattern(URL url) {
		rwl.r.lock();
		try {
			List<PatternItem> patternList = patternMap.get(url.getHost());
			if (patternList == null)
				return false;
			String sUrl = url.toExternalForm();
			for (PatternItem patternItem : patternList)
				if (patternItem.match(sUrl))
					return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	public void injectUrl(List<InjectUrlItem> urlListItems)
			throws SearchLibException {
		Iterator<InjectUrlItem> it = urlListItems.iterator();
		List<PatternItem> patternList = new ArrayList<PatternItem>();
		while (it.hasNext()) {
			InjectUrlItem item = it.next();
			if (matchPattern(item.getURL()))
				continue;
			patternList.add(new PatternItem(item.getURL()));
		}
		addList(patternList, false);
	}

	final private static void addLine(List<PatternItem> list, String pattern) {
		pattern = pattern.trim();
		if (pattern.length() == 0)
			return;
		if (pattern.indexOf(':') == -1)
			pattern = "http://" + pattern;
		PatternItem item = new PatternItem();
		item.setPattern(pattern);
		list.add(item);
	}

	final private static void addLines(List<PatternItem> list, String lines)
			throws IOException {
		if (lines == null)
			return;
		StringReader sr = null;
		BufferedReader br = null;
		try {
			sr = new StringReader(lines);
			br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null)
				addLine(list, line);
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
			if (sr != null)
				IOUtils.closeQuietly(sr);
		}
	}

	public static List<PatternItem> getPatternList(String pattern)
			throws IOException {
		List<PatternItem> patternList = new ArrayList<PatternItem>(0);
		addLines(patternList, pattern);
		return patternList;
	}

	public static List<PatternItem> getPatternList(List<String> patterns)
			throws IOException {
		List<PatternItem> patternList = new ArrayList<PatternItem>(0);
		if (patterns != null)
			for (String sPattern : patterns)
				addLines(patternList, sPattern);
		return patternList;
	}

	public static List<PatternItem> getPatternList(BufferedReader reader)
			throws IOException {
		List<PatternItem> patternList = new ArrayList<PatternItem>();
		String line;
		while ((line = reader.readLine()) != null)
			addLine(patternList, line);
		return patternList;
	}

	public static String getStringPatternList(List<PatternItem> patternList) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (PatternItem item : patternList)
				pw.println(item.getPattern());
			return sw.toString();
		} finally {
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}

	public static final int countStatus(List<PatternItem> patternList,
			PatternItem.Status status) {
		if (patternList == null)
			return 0;
		int count = 0;
		for (PatternItem patternItem : patternList)
			if (patternItem.getStatus() == status)
				count++;
		return count;
	}

}
