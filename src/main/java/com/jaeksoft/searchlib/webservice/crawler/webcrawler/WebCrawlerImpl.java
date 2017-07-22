/*
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.pattern.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.pattern.PatternManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.RestApplication;
import com.jaeksoft.searchlib.webservice.crawler.CrawlerUtils;
import com.jaeksoft.searchlib.webservice.query.document.FieldValueList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebCrawlerImpl extends CommonServices implements RestWebCrawler {

	@Override
	public CommonResult run(String use, String login, String key, Boolean once) {
		try {
			Client client = getLoggedClient(use, login, key, Role.WEB_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
			if (once != null && once)
				return CrawlerUtils.runOnce(client.getWebCrawlMaster());
			else {
				client.getWebPropertyManager().getCrawlEnabled().setValue(true);
				return CrawlerUtils.runForever(client.getWebCrawlMaster());
			}
		} catch (IOException | SearchLibException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult stop(String use, String login, String key) {
		try {
			Client client = getLoggedClient(use, login, key, Role.WEB_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
			client.getWebPropertyManager().getCrawlEnabled().setValue(false);
			return CrawlerUtils.stop(client.getWebCrawlMaster());
		} catch (IOException | SearchLibException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult status(String use, String login, String key) {
		try {
			if (use.equals("*"))
				return allStatus(login, key);
			Client client = getLoggedClientAnyRole(use, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			return CrawlerUtils.status(client.getWebCrawlMaster());
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult allStatus(String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Set<ClientCatalogItem> catalogItem = ClientCatalog.getClientCatalog(getLoggedUser(login, key));
			CommonResult status;
			if (catalogItem != null && !catalogItem.isEmpty()) {
				status = new CommonResult(true, "All the client's status");
				for (final ClientCatalogItem item : catalogItem) {
					if (item == null)
						continue;
					status.addDetail(item.getIndexName(),
							CrawlerUtils.infoStatus(item.getClient().getWebCrawlMaster()));
				}
			} else
				status = new CommonResult(false, "Don't have any index");
			return status;
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	private AbstractSearchRequest getRequest(UrlManager urlManager, String host)
			throws SearchLibException, ParseException {
		AbstractSearchRequest searchRequest = urlManager.getSearchRequest(SearchTemplate.urlExport);
		searchRequest.setQueryString("*:*");
		if (host != null && host.length() > 0)
			searchRequest.addFilter("host:\"" + host + '"', false);
		return searchRequest;
	}

	public byte[] exportURLs(String use, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			File file = client.getUrlManager().exportURLs(getRequest(client.getUrlManager(), null));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException | IOException | ParseException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult truncateUrls(String use, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			client.getUrlManager().deleteAll(null);
			return new CommonResult(true, "delete all");
		} catch (InterruptedException | IOException | SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	public byte[] exportSiteMap(String use, String host, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			File file = client.getUrlManager().exportSiteMap(getRequest(client.getUrlManager(), host));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException | IOException | ParseException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult injectSiteMap(String index, String login, String key, List<String> addListSiteMap) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = client.getSiteMapList().getSize();
			for (final String SiteMapUrl : addListSiteMap) {
				client.getSiteMapList().add(new SiteMapItem(SiteMapUrl));
			}
			return new CommonResult(true, (client.getSiteMapList().getSize() - count) + " SiteMap injected");
		} catch (SearchLibException | InterruptedException | IOException | URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult deleteSiteMap(String index, String login, String key, List<String> deleteList) {
		try {
			SiteMapItem item = new SiteMapItem();
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = client.getSiteMapList().getSize();
			for (final String del : deleteList) {
				item.setUri(del);
				client.getSiteMapList().remove(item);
			}
			return new CommonResult(true, (count - client.getSiteMapList().getSize()) + " SiteMap deleted");
		} catch (SearchLibException | URISyntaxException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonListResult<String> getSiteMap(String index, String login, String key) {
		try {
			List<String> SiteMapStr = new ArrayList<String>();
			Client client = getLoggedClientAnyRole(index, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			SiteMapList Maplist = client.getSiteMapList();
			for (final SiteMapItem item : Maplist.getArray()) {
				SiteMapStr.add(item.getUri());
			}
			return new CommonListResult<String>(SiteMapStr);
		} catch (SearchLibException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	private CommonResult injectPatterns(String index, String login, String key, Boolean replaceAll, Boolean injectUrls,
			List<String> patterns, boolean inclusion) {
		try {
			if (injectUrls == null)
				injectUrls = false;
			if (replaceAll == null)
				replaceAll = false;
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			List<PatternItem> patternList = PatternManager.getPatternList(patterns);
			PatternManager patternManager =
					inclusion ? client.getInclusionPatternManager() : client.getExclusionPatternManager();
			patternManager.addList(patternList, replaceAll);
			int count = PatternManager.countStatus(patternList, PatternItem.Status.INJECTED);
			if (injectUrls && inclusion)
				client.getUrlManager().injectPrefix(patternList);
			return new CommonResult(true, count + " patterns injected");
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult injectPatternsInclusion(String index, String login, String key, Boolean replaceAll,
			Boolean injectURLS, List<String> patterns) {
		return injectPatterns(index, login, key, replaceAll, injectURLS, patterns, true);
	}

	@Override
	public CommonResult injectPatternsExclusion(String index, String login, String key, Boolean replaceAll,
			List<String> patterns) {
		return injectPatterns(index, login, key, replaceAll, false, patterns, false);
	}

	private CommonResult getPatternStatusResult(WebPropertyManager webPropertyManager) {
		CommonResult commonResult = new CommonResult(true, null);
		commonResult.addDetail("inclusion_enabled", webPropertyManager.getInclusionEnabled().getValue());
		commonResult.addDetail("exclusion_enabled", webPropertyManager.getExclusionEnabled().getValue());
		return commonResult;
	}

	@Override
	public CommonResult getPatternStatus(String index, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			WebPropertyManager webPropertyManager = client.getWebPropertyManager();
			return getPatternStatusResult(webPropertyManager);
		} catch (InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult setPatternStatus(String index, String login, String key, Boolean inclusion, Boolean exclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			WebPropertyManager webPropertyManager = client.getWebPropertyManager();
			if (inclusion != null)
				webPropertyManager.getInclusionEnabled().setValue(inclusion);
			if (exclusion != null)
				webPropertyManager.getExclusionEnabled().setValue(exclusion);
			return getPatternStatusResult(webPropertyManager);
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}

	}

	private CommonResult deletePatterns(String index, String login, String key, List<String> patterns,
			boolean inclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			PatternManager patternManager =
					inclusion ? client.getInclusionPatternManager() : client.getExclusionPatternManager();
			int count = patternManager.delPattern(patterns);
			return new CommonResult(true, count + " patterns deleted");
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deletePatternsInclusion(String index, String login, String key, List<String> deleteList) {
		return deletePatterns(index, login, key, deleteList, true);
	}

	@Override
	public CommonResult deletePatternsExclusion(String index, String login, String key, List<String> deleteList) {
		return deletePatterns(index, login, key, deleteList, false);
	}

	public CommonListResult<String> extractPatterns(String index, String login, String key, String startsWith,
			boolean inclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			PatternManager patternManager =
					inclusion ? client.getInclusionPatternManager() : client.getExclusionPatternManager();
			List<String> patterns = new ArrayList<String>();
			patternManager.getPatterns(startsWith, patterns);
			return new CommonListResult<String>(patterns);
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonListResult<String> extractPatternsInclusion(String index, String login, String key,
			String startsWith) {
		return extractPatterns(index, login, key, startsWith, true);
	}

	@Override
	public CommonListResult<String> extractPatternsExclusion(String index, String login, String key,
			String startsWith) {
		return extractPatterns(index, login, key, startsWith, false);
	}

	@Override
	public CommonResult crawl(String use, String login, String key, String url, Boolean returnData) {
		try {
			Client client = getLoggedClient(use, login, key, Role.WEB_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
			WebCrawlThread webCrawlThread = client.getWebCrawlMaster().manualCrawl(LinkUtils.newEncodedURL(url),
					HostUrlList.ListType.MANUAL);
			if (!webCrawlThread.waitForStart(120))
				throw new WebServiceException("Time out reached (120 seconds)");
			if (!webCrawlThread.waitForEnd(3600))
				throw new WebServiceException("Time out reached (3600 seconds)");
			UrlItem urlItem = webCrawlThread.getCurrentUrlItem();
			CommonResult cr = null;
			if (BooleanUtils.isTrue(returnData)) {
				Crawl crawl = webCrawlThread.getCurrentCrawl();
				if (crawl != null) {
					List<IndexDocument> indexDocuments = crawl.getTargetIndexDocuments();
					if (CollectionUtils.isNotEmpty(indexDocuments)) {
						CommonListResult<ArrayList<FieldValueList>> clr =
								new CommonListResult<ArrayList<FieldValueList>>(indexDocuments.size());
						for (IndexDocument indexDocument : indexDocuments) {
							ArrayList<FieldValueList> list = FieldValueList.getNewList(indexDocument);
							if (list != null)
								clr.items.add(list);
						}
						cr = clr;
					}
				}
			}

			String message = urlItem != null ?
					"Result: " + urlItem.getFetchStatus() + " - " + urlItem.getParserStatus() + " - " +
							urlItem.getIndexStatus() :
					null;
			if (cr == null)
				cr = new CommonResult(true, message);
			if (urlItem != null) {
				cr.addDetail("URL", urlItem.getUrl());
				cr.addDetail("HttpResponseCode", urlItem.getResponseCode());
				cr.addDetail("RobotsTxtStatus", urlItem.getRobotsTxtStatus());
				cr.addDetail("FetchStatus", urlItem.getFetchStatus());
				cr.addDetail("ParserStatus", urlItem.getParserStatus());
				cr.addDetail("IndexStatus", urlItem.getIndexStatus());
				cr.addDetail("RedirectionURL", urlItem.getRedirectionUrl());
				cr.addDetail("ContentBaseType", urlItem.getContentBaseType());
				cr.addDetail("ContentTypeCharset", urlItem.getContentTypeCharset());
				cr.addDetail("ContentLength", urlItem.getContentLength());
			}
			return cr;
		} catch (SearchLibException | ParseException | IOException | SyntaxError | URISyntaxException | ClassNotFoundException | InterruptedException | InstantiationException | IllegalAccessException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult crawlPost(String use, String login, String key, String url, Boolean returnData) {
		return crawl(use, login, key, url, returnData);
	}

	public CommonResult captureScreenshot(String use, String login, String key, String url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			CredentialManager credentialManager = client.getWebCredentialManager();
			ScreenshotServlet.doCapture(null, screenshotManager, credentialManager, LinkUtils.newEncodedURL(url));
			String message = "Captured URL " + url;
			return new CommonResult(true, message);
		} catch (SearchLibException | URISyntaxException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult checkScreenshot(String use, String login, String key, String url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			String message = ScreenshotServlet.doCheck(screenshotManager, LinkUtils.newEncodedURL(url));
			return new CommonResult(true, message);
		} catch (SearchLibException | InterruptedException | IOException | URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	public static String getCrawlXML(User user, Client client, String url) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/index/{index}/crawler/web/crawl", user, client, "url", url);
	}

	public static String getCrawlJSON(User user, Client client, String url) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/index/{index}/crawler/web/crawl", user, client, "url", url);
	}

	@Override
	public CommonResult injectUrls(String index, String login, String key, Boolean replaceAll, List<String> urls) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			UrlManager urlManager = client.getUrlManager();
			CommonResult result = new CommonResult(true, null);
			if (replaceAll != null && replaceAll)
				urlManager.deleteAll(null);
			urlManager.inject(urls, result);
			return result;
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult getUrls(String index, String login, String key) {
		try {
			getLoggedClientAnyRole(index, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			return null;
		} catch (InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult robotstxt(String index, String login, String key, Boolean enabled) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			if (enabled == null)
				enabled = client.getWebPropertyManager().getRobotsTxtEnabled().getValue();
			else
				client.getWebPropertyManager().getRobotsTxtEnabled().setValue(enabled);
			return new CommonResult(true, "Robots.txt status: " + enabled);
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public HostnamesResult getHostnames(String index, String login, String key, Integer minCount) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			if (minCount == null)
				minCount = 0;
			return new HostnamesResult(client.getUrlManager().getHostFacetList(minCount));
		} catch (InterruptedException | IOException | SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult getProperties(String index, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			WebPropertyManager webProperties = client.getWebPropertyManager();
			CommonResult result = new CommonResult(true, "getProperties");
			final HashMap<String, Comparable> props = new HashMap<>();
			webProperties.fillProperties(props);
			for (Map.Entry<String, Comparable> entry : props.entrySet())
				result.addDetail(entry.getKey(), entry.getValue());
			return result;
		} catch (InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult setProperty(String index, String login, String key, String property, String value) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key, Role.WEB_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			WebPropertyManager webProperties = client.getWebPropertyManager();
			webProperties.setProperty(property, value);
			return new CommonResult(true, "setProperty");
		} catch (InterruptedException | SearchLibException | IOException e) {
			throw new CommonServiceException(e);
		}
	}
}
