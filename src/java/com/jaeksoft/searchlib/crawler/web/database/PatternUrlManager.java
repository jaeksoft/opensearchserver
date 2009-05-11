/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PatternUrlItem.Status;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;

public class PatternUrlManager {

	protected Map<String, List<PatternUrlItem>> patternUrlMap = null;

	private Client client;

	public PatternUrlManager(File dataDir) throws SearchLibException,
			URISyntaxException {
		client = new Client(new File(dataDir, "pattern"),
				"/pattern_config.xml", true);
		updateCache();
	}

	public void addList(List<PatternUrlItem> patternList, boolean bDeleteAll)
			throws SearchLibException {
		synchronized (client) {
			List<IndexDocument> injectList = new ArrayList<IndexDocument>();
			try {
				if (bDeleteAll) {
					SearchRequest searchRequest = client.getNewSearchRequest();
					searchRequest.setDelete(true);
					searchRequest.setQueryString("*:*");
					client.search(searchRequest);
				}
				synchronized (patternList) {
					// First pass: Identify already
					for (PatternUrlItem item : patternList) {
						String pattern = item.getPattern();
						if (!bDeleteAll
								&& findPatternUrl(new URL(pattern)) != null)
							item.setStatus(PatternUrlItem.Status.ALREADY);
						else
							injectList.add(item.getIndexDocument());
					}
					client.updateDocuments(injectList);
					for (PatternUrlItem item : patternList)
						if (item.getStatus() == Status.UNDEFINED)
							item.setStatus(Status.INJECTED);
				}
				if (injectList.size() > 0) {
					client.reload(null);
					updateCache();
				}
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new SearchLibException(e);
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (SyntaxError e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			} catch (InterruptedException e) {
				throw new SearchLibException(e);
			}
		}
	}

	public void delPattern(Collection<String> patterns)
			throws SearchLibException {
		synchronized (client) {
			try {
				client.deleteDocuments(patterns);
				client.reload(null);
				updateCache();
			} catch (CorruptIndexException e) {
				throw new SearchLibException(e);
			} catch (LockObtainFailedException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			}
		}
	}

	protected void updateCache() throws SearchLibException {
		Map<String, List<PatternUrlItem>> newPatternUrlMap = new TreeMap<String, List<PatternUrlItem>>();
		synchronized (client) {
			try {
				int start = 0;
				boolean found = true;
				while (found == true) {
					found = false;
					SearchRequest searchRequest = client
							.getNewSearchRequest("search");
					searchRequest.setStart(start);
					searchRequest.setRows(100);
					searchRequest.setQueryString("*:*");
					Result result = client.search(searchRequest);
					if (result == null)
						break;
					for (ResultDocument doc : result) {
						found = true;
						PatternUrlItem item = new PatternUrlItem(doc.getValue(
								"pattern", 0));
						String host = item.extractUrl(true).getHost();
						List<PatternUrlItem> itemList = newPatternUrlMap
								.get(host);
						if (itemList == null) {
							itemList = new ArrayList<PatternUrlItem>();
							newPatternUrlMap.put(host, itemList);
						}
						itemList.add(item);
					}
					start += 100;
				}
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (SyntaxError e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			} catch (InterruptedException e) {
				throw new SearchLibException(e);
			}
		}
		synchronized (this) {
			patternUrlMap = newPatternUrlMap;
		}
	}

	public int getPatterns(String like, long start, long rows,
			List<PatternUrlItem> list) throws SearchLibException {
		synchronized (client) {
			SearchRequest searchRequest = client.getNewSearchRequest("search");
			searchRequest.setStart((int) start);
			searchRequest.setRows((int) rows);
			try {
				StringBuffer query = new StringBuffer();
				if (like != null) {
					like = like.trim();
					if (like.length() > 0) {
						query.append("pattern:");
						query.append(SearchRequest.escapeQuery(like));
						query.append("*");
					}
				}
				if (query.length() == 0)
					query.append("*:*");
				searchRequest.setQueryString(query.toString().trim());
				Result result = client.search(searchRequest);
				for (ResultDocument doc : result)
					list.add(new PatternUrlItem(doc.getValue("pattern", 0)));
				return result.getNumFound();
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (SyntaxError e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			} catch (InterruptedException e) {
				throw new SearchLibException(e);
			}
		}
	}

	public PatternUrlItem findPatternUrl(URL url) {
		List<PatternUrlItem> patternList = null;
		synchronized (this) {
			patternList = patternUrlMap.get(url.getHost());
		}
		if (patternList == null)
			return null;
		synchronized (patternList) {
			String sUrl = url.toExternalForm();
			for (PatternUrlItem patternItem : patternList)
				if (patternItem.match(sUrl))
					return patternItem;
			return null;
		}
	}

	public void injectUrl(List<InjectUrlItem> urlListItems)
			throws SearchLibException {
		Iterator<InjectUrlItem> it = urlListItems.iterator();
		List<PatternUrlItem> patternList = new ArrayList<PatternUrlItem>();
		while (it.hasNext()) {
			InjectUrlItem item = it.next();
			if (findPatternUrl(item.getURL()) != null)
				continue;
			patternList.add(new PatternUrlItem(item.getURL()));
		}
		addList(patternList, false);
	}

	private static void addLine(List<PatternUrlItem> list, String pattern) {
		pattern = pattern.trim();
		if (pattern.length() == 0)
			return;
		if (pattern.indexOf(':') == -1)
			pattern = "http://" + pattern;
		PatternUrlItem item = new PatternUrlItem();
		item.setPattern(pattern);
		list.add(item);
	}

	public static List<PatternUrlItem> getPatternUrlList(String pattern) {
		List<PatternUrlItem> patternList = new ArrayList<PatternUrlItem>();
		for (String sPattern : pattern.split("\n"))
			addLine(patternList, sPattern);
		return patternList;
	}

	public static List<PatternUrlItem> getPatternUrlList(BufferedReader reader)
			throws IOException {
		List<PatternUrlItem> patternList = new ArrayList<PatternUrlItem>();
		String line;
		while ((line = reader.readLine()) != null)
			addLine(patternList, line);
		return patternList;
	}

	public static String getStringPatternUrlList(
			List<PatternUrlItem> patternList) {
		StringBuffer sPattern = new StringBuffer();
		for (PatternUrlItem item : patternList) {
			sPattern.append(item.getPattern());
			sPattern.append("\n");
		}
		return sPattern.toString();
	}

}
