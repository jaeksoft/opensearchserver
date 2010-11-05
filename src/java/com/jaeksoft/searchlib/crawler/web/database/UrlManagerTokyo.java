/**   

 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import tokyocabinet.TDB;
import tokyocabinet.TDBQRY;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.InjectUrlItem.Status;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.Field;
import com.jaeksoft.searchlib.database.tokyo.TokyoTDB;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class UrlManagerTokyo extends UrlManagerAbstract {

	public TokyoTDB dbUrl;

	public final ReadWriteLock rwl = new ReadWriteLock();

	public UrlManagerTokyo() {
		dbUrl = new TokyoTDB();
	}

	@Override
	public void init(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		targetClient = client;
		File file = new File(dataDir, "url");
		try {
			rwl.w.lock();
			try {
				dbUrl.init(file);
				dbUrl.openForWrite();
				dbUrl.db.setindex(UrlItemFieldEnum.url.name(), TDB.ITLEXICAL
						| TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.when.name(), TDB.ITLEXICAL
						| TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.host.name(), TDB.ITLEXICAL
						| TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.contentBaseType.name(),
						TDB.ITLEXICAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.contentEncoding.name(),
						TDB.ITLEXICAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.lang.name(), TDB.ITLEXICAL
						| TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.langMethod.name(),
						TDB.ITLEXICAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.responseCode.name(),
						TDB.ITDECIMAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.fetchStatus.name(),
						TDB.ITDECIMAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.parserStatus.name(),
						TDB.ITDECIMAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.indexStatus.name(),
						TDB.ITDECIMAL | TDB.ITKEEP);
				dbUrl.db.setindex(UrlItemFieldEnum.robotsTxtStatus.name(),
						TDB.ITDECIMAL | TDB.ITKEEP);
				dbUrl.sync();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void free() throws SearchLibException {
		try {
			rwl.w.lock();
			dbUrl.close();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public Client getUrlDbClient() {
		return null;
	}

	public void deleteUrl(String sUrl) throws SearchLibException {
		try {
			if (sUrl == null)
				return;
			targetClient.deleteDocument(sUrl);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (HttpException e) {
			throw new SearchLibException(e);
		}
		try {
			rwl.w.lock();
			try {
				if (!dbUrl.isWrite()) {
					dbUrl.close();
					dbUrl.openForWrite();
				}
				String pk = getPrimaryKey(sUrl);
				if (pk != null)
					if (!dbUrl.db.out(pk))
						if (dbUrl.db.ecode() != TDB.ENOREC)
							dbUrl.throwError("Delete failure");
				dbUrl.sync();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void deleteUrls(Collection<String> workDeleteUrlList)
			throws SearchLibException {
		try {
			targetClient.deleteDocuments(workDeleteUrlList);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
		try {
			rwl.w.lock();
			try {
				if (!dbUrl.isWrite()) {
					dbUrl.close();
					dbUrl.openForWrite();
				}
				for (String sUrl : workDeleteUrlList) {
					String pk = getPrimaryKey(sUrl);
					if (pk != null)
						if (!dbUrl.db.out(pk))
							if (dbUrl.db.ecode() != TDB.ENOREC)
								dbUrl.throwError("Delete failure");
				}
				dbUrl.sync();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void removeExisting(List<String> urlList) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				Iterator<String> it = urlList.iterator();
				while (it.hasNext())
					if (getPrimaryKey((String) it.next()) != null)
						it.remove();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private String getPrimaryKey(String sUrl) throws SearchLibException {
		TDBQRY qry = new TDBQRY(dbUrl.db);
		qry.addcond(UrlItemFieldEnum.url.name(), TDBQRY.QCSTREQ, sUrl);
		List<?> res = qry.search();
		if (res == null)
			dbUrl.throwError("GetPrimaryKey failed");
		if (res.size() == 0)
			return null;
		return new String((byte[]) res.get(0));
	}

	private void tokyoMap2hostList(List<?> res, List<NamedItem> hostList,
			int limit) {
		Map<String, NamedItem> hostSet = new HashMap<String, NamedItem>();
		for (Object pk : res) {
			Map<?, ?> map = dbUrl.db.get(new String((byte[]) pk));
			String host = (String) map.get(UrlItemFieldEnum.host.name());
			NamedItem namedItem = hostSet.get(host);
			if (namedItem == null) {
				namedItem = new NamedItem(host, 1);
				hostSet.put(host, namedItem);
				hostList.add(namedItem);
				if (limit-- < 0)
					return;
			} else
				namedItem.count++;
		}
	}

	@Override
	public void getOldHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMLT,
						Long.toString(fetchIntervalDate.getTime()));
				List<?> res = qry.search();
				tokyoMap2hostList(res, hostList, limit);
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void getNewHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				qry.addcond(UrlItemFieldEnum.fetchStatus.name(),
						TDBQRY.QCNUMEQ, FetchStatus.UN_FETCHED.getValue());
				qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMGE,
						Long.toString(fetchIntervalDate.getTime()));
				List<?> res = qry.search();
				tokyoMap2hostList(res, hostList, limit);
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				qry.addcond(UrlItemFieldEnum.host.name(), TDBQRY.QCNUMEQ,
						host.getName());
				qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMLT,
						Long.toString(fetchIntervalDate.getTime()));
				qry.setlimit((int) limit, 0);
				List<?> res = qry.search();
				for (Object o : res)
					urlList.add(new UrlItem(dbUrl.db
							.get(new String((byte[]) o))));
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				qry.addcond(UrlItemFieldEnum.url.name(), TDBQRY.QCSTREQ,
						url.toExternalForm());
				List<?> res = qry.search();
				if (res == null || res.size() == 0)
					return null;
				return new UrlItem(
						dbUrl.db.get(new String((byte[]) res.get(0))));
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void getNewUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				qry.addcond(UrlItemFieldEnum.host.name(), TDBQRY.QCSTREQ,
						host.getName());
				qry.addcond(UrlItemFieldEnum.fetchStatus.name(),
						TDBQRY.QCSTREQ, FetchStatus.UN_FETCHED.getValue());
				qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMGE,
						Long.toString(fetchIntervalDate.getTime()));
				qry.setlimit((int) limit, 0);
				List<?> res = qry.search();
				for (Object o : res)
					urlList.add(new UrlItem(dbUrl.db
							.get(new String((byte[]) o))));
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void reload(boolean optimize) throws SearchLibException {
		// TODO Implementation
		targetClient.reload();
	}

	@Override
	public void updateUrlItems(List<UrlItem> urlItems)
			throws SearchLibException {
		if (urlItems == null)
			return;
		try {
			rwl.w.lock();
			try {
				if (!dbUrl.isWrite()) {
					dbUrl.close();
					dbUrl.openForWrite();
				}

				for (UrlItem urlItem : urlItems) {
					if (urlItem == null)
						continue;

					String sUrl = urlItem.getUrl();

					String primaryKey = getPrimaryKey(sUrl);
					if (primaryKey == null)
						primaryKey = Long.toString(dbUrl.db.genuid());

					Map<String, String> cols = new HashMap<String, String>();
					cols.put(UrlItemFieldEnum.url.name(), sUrl);
					if (urlItem.getLang() != null)
						cols.put(UrlItemFieldEnum.lang.name(),
								urlItem.getLang());
					if (urlItem.getLangMethod() != null)
						cols.put(UrlItemFieldEnum.langMethod.name(),
								urlItem.getLangMethod());
					if (urlItem.getContentBaseType() != null)
						cols.put(UrlItemFieldEnum.contentBaseType.name(),
								urlItem.getContentBaseType());
					if (urlItem.getContentTypeCharset() != null)
						cols.put(UrlItemFieldEnum.contentTypeCharset.name(),
								urlItem.getContentTypeCharset());
					if (urlItem.getContentEncoding() != null)
						cols.put(UrlItemFieldEnum.contentEncoding.name(),
								urlItem.getContentEncoding());
					String hostname = urlItem.getURL().getHost();
					cols.put(UrlItemFieldEnum.host.name(), hostname);
					if (urlItem.getWhen() != null)
						cols.put(UrlItemFieldEnum.when.name(),
								Long.toString(urlItem.getWhen().getTime()));
					if (urlItem.getResponseCode() != null)
						cols.put(UrlItemFieldEnum.responseCode.name(), urlItem
								.getResponseCode().toString());
					if (urlItem.getRobotsTxtStatus() != null)
						cols.put(UrlItemFieldEnum.robotsTxtStatus.name(),
								urlItem.getRobotsTxtStatus().getValue());
					if (urlItem.getParserStatus() != null)
						cols.put(UrlItemFieldEnum.parserStatus.name(), urlItem
								.getParserStatus().getValue());
					if (urlItem.getIndexStatus() != null)
						cols.put(UrlItemFieldEnum.indexStatus.name(), urlItem
								.getIndexStatus().getValue());
					if (urlItem.getFetchStatus() != null)
						cols.put(UrlItemFieldEnum.fetchStatus.name(), urlItem
								.getFetchStatus().getValue());
					if (!dbUrl.db.put(primaryKey, cols))
						dbUrl.throwError("Put error");
				}
				dbUrl.sync();
			} catch (MalformedURLException e) {
				throw new SearchLibException(e);
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void inject(List<InjectUrlItem> list) throws SearchLibException {
		try {
			rwl.w.lock();
			try {
				if (!dbUrl.isWrite()) {
					dbUrl.close();
					dbUrl.openForWrite();
				}
				for (InjectUrlItem item : list) {
					if (getPrimaryKey(item.getUrl()) != null)
						item.setStatus(InjectUrlItem.Status.ALREADY);
					else {
						String uid = Long.toString(dbUrl.db.genuid());
						Map<String, String> cols = new HashMap<String, String>();
						cols.put(UrlItemFieldEnum.url.name(), item.getUrl());
						cols.put(UrlItemFieldEnum.when.name(),
								Long.toString(new Date().getTime()));
						String hostname = item.getURL().getHost();
						cols.put(UrlItemFieldEnum.host.name(), hostname);
						cols.put(UrlItemFieldEnum.fetchStatus.name(),
								FetchStatus.UN_FETCHED.getValue());
						cols.put(UrlItemFieldEnum.parserStatus.name(),
								ParserStatus.NOT_PARSED.getValue());
						cols.put(UrlItemFieldEnum.indexStatus.name(),
								IndexStatus.NOT_INDEXED.getValue());
						cols.put(UrlItemFieldEnum.robotsTxtStatus.name(),
								RobotsTxtStatus.UNKNOWN.getValue());
						dbUrl.db.put(uid, cols);
						item.setStatus(Status.INJECTED);
					}
				}
				dbUrl.sync();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public long getUrls(SearchTemplate urlSearchTemplate, String like,
			String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, boolean orderAsc, long start, long rows,
			List<UrlItem> list) throws SearchLibException {
		try {
			rwl.r.lock();
			try {
				if (!dbUrl.isOpen())
					dbUrl.openForRead();
				TDBQRY qry = new TDBQRY(dbUrl.db);
				if (like != null) {
					like = like.trim();
					if (like.length() > 0) {
						qry.addcond(UrlItemFieldEnum.url.name(),
								TDBQRY.QCSTREW, like);
					}
				}
				if (host != null) {
					host = host.trim();
					if (host.length() > 0) {
						qry.addcond(UrlItemFieldEnum.host.name(),
								TDBQRY.QCSTREQ, host);
					}
				}
				if (lang != null) {
					lang = lang.trim();
					if (lang.length() > 0) {
						qry.addcond(UrlItemFieldEnum.lang.name(),
								TDBQRY.QCSTREQ, lang);
					}
				}
				if (langMethod != null) {
					langMethod = langMethod.trim();
					if (langMethod.length() > 0) {
						qry.addcond(UrlItemFieldEnum.langMethod.name(),
								TDBQRY.QCSTREQ, langMethod);
					}
				}
				if (contentBaseType != null) {
					contentBaseType = contentBaseType.trim();
					if (contentBaseType.length() > 0) {
						qry.addcond(UrlItemFieldEnum.contentBaseType.name(),
								TDBQRY.QCSTREQ, contentBaseType);
					}
				}
				if (contentTypeCharset != null) {
					contentTypeCharset = contentTypeCharset.trim();
					if (contentTypeCharset.length() > 0) {
						qry.addcond(UrlItemFieldEnum.contentTypeCharset.name(),
								TDBQRY.QCSTREQ, contentTypeCharset);
					}
				}
				if (contentEncoding != null) {
					contentEncoding = contentEncoding.trim();
					if (contentEncoding.length() > 0) {
						qry.addcond(UrlItemFieldEnum.contentEncoding.name(),
								TDBQRY.QCSTREQ, contentEncoding);
					}
				}
				if (robotsTxtStatus != null
						&& robotsTxtStatus != RobotsTxtStatus.ALL) {
					qry.addcond(UrlItemFieldEnum.robotsTxtStatus.name(),
							TDBQRY.QCNUMEQ, robotsTxtStatus.getValue());
				}
				if (fetchStatus != null && fetchStatus != FetchStatus.ALL) {
					qry.addcond(UrlItemFieldEnum.fetchStatus.name(),
							TDBQRY.QCNUMEQ, fetchStatus.getValue());
				}
				if (parserStatus != null && parserStatus != ParserStatus.ALL) {
					qry.addcond(UrlItemFieldEnum.parserStatus.name(),
							TDBQRY.QCNUMEQ, parserStatus.getValue());
				}
				if (indexStatus != null && indexStatus != IndexStatus.ALL) {
					qry.addcond(UrlItemFieldEnum.indexStatus.name(),
							TDBQRY.QCNUMEQ, indexStatus.getValue());
				}
				if (startDate != null)
					qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMGE,
							Long.toString(startDate.getTime()));

				if (endDate != null)
					qry.addcond(UrlItemFieldEnum.when.name(), TDBQRY.QCNUMLT,
							Long.toString(endDate.getTime()));

				List<?> res = qry.search();
				long end = start + rows;
				if (end > res.size())
					end = res.size();
				for (int i = (int) start; i < end; i++) {
					Map<?, ?> rcols = dbUrl.db.get(new String((byte[]) res
							.get(i)));
					list.add(new UrlItem(rcols));
				}
				return res.size();
			} catch (SearchLibException e) {
				dbUrl.close();
				throw e;
			}
		} finally {
			rwl.r.unlock();
		}
	}
}