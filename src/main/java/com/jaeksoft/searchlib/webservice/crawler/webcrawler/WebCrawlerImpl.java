/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.RestApplication;
import com.jaeksoft.searchlib.webservice.crawler.CrawlerUtils;

public class WebCrawlerImpl extends CommonServices implements SoapWebCrawler,
		RestWebCrawler {

	private WebCrawlMaster getCrawlMaster(String use, String login, String key) {
		try {
			Client client = getLoggedClient(use, login, key,
					Role.WEB_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
			return client.getWebCrawlMaster();
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult run(String use, String login, String key, boolean once) {
		if (once)
			return CrawlerUtils.runOnce(getCrawlMaster(use, login, key));
		else
			return CrawlerUtils.runForever(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult stop(String use, String login, String key) {
		return CrawlerUtils.stop(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult status(String use, String login, String key) {
		return CrawlerUtils.status(getCrawlMaster(use, login, key));
	}

	private AbstractSearchRequest getRequest(UrlManager urlManager, String host)
			throws SearchLibException, ParseException {
		AbstractSearchRequest searchRequest = urlManager
				.getSearchRequest(SearchTemplate.urlExport);
		searchRequest.setQueryString("*:*");
		if (host != null && host.length() > 0)
			searchRequest.addFilter("host:\"" + host + '"', false);
		return searchRequest;
	}

	@Override
	public byte[] exportURLs(String use, String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			File file = client.getUrlManager().exportURLs(
					getRequest(client.getUrlManager(), null));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (FileNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ParseException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public byte[] exportSiteMap(String use, String host, String login,
			String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			File file = client.getUrlManager().exportSiteMap(
					getRequest(client.getUrlManager(), host));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (FileNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ParseException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	private CommonResult injectPatterns(String index, String login, String key,
			boolean replaceAll, List<String> patterns, boolean inclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key,
					Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			List<PatternItem> patternList = PatternManager
					.getPatternList(patterns);
			PatternManager patternManager = inclusion ? client
					.getInclusionPatternManager() : client
					.getExclusionPatternManager();
			patternManager.addList(patternList, replaceAll);
			int count = PatternManager.countStatus(patternList,
					PatternItem.Status.INJECTED);
			return new CommonResult(true, count + " patterns injected");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult injectPatternsInclusion(String index, String login,
			String key, boolean replaceAll, List<String> patterns) {
		return injectPatterns(index, login, key, replaceAll, patterns, true);
	}

	@Override
	public CommonResult injectPatternsExclusion(String index, String login,
			String key, boolean replaceAll, List<String> patterns) {
		return injectPatterns(index, login, key, replaceAll, patterns, false);
	}

	private CommonResult deletePatterns(String index, String login, String key,
			List<String> patterns, boolean inclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key,
					Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
			ClientFactory.INSTANCE.properties.checkApi();
			PatternManager patternManager = inclusion ? client
					.getInclusionPatternManager() : client
					.getExclusionPatternManager();
			int count = patternManager.delPattern(patterns);
			return new CommonResult(true, count + " patterns deleted");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deletePatternsInclusion(String index, String login,
			String key, List<String> deleteList) {
		return deletePatterns(index, login, key, deleteList, true);
	}

	@Override
	public CommonResult deletePatternsExclusion(String index, String login,
			String key, List<String> deleteList) {
		return deletePatterns(index, login, key, deleteList, false);
	}

	public CommonListResult extractPatterns(String index, String login,
			String key, String startsWith, boolean inclusion) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			PatternManager patternManager = inclusion ? client
					.getInclusionPatternManager() : client
					.getExclusionPatternManager();
			List<String> patterns = new ArrayList<String>();
			patternManager.getPatterns(startsWith, patterns);
			return new CommonListResult(patterns);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonListResult extractPatternsInclusion(String index,
			String login, String key, String startsWith) {
		return extractPatterns(index, login, key, startsWith, true);
	}

	@Override
	public CommonListResult extractPatternsExclusion(String index,
			String login, String key, String startsWith) {
		return extractPatterns(index, login, key, startsWith, false);
	}

	@Override
	public CommonResult crawl(String use, String login, String key, String url) {
		try {
			WebCrawlMaster crawlMaster = getCrawlMaster(use, login, key);
			WebCrawlThread webCrawlThread = crawlMaster.manualCrawl(
					LinkUtils.newEncodedURL(url), HostUrlList.ListType.MANUAL);
			if (!webCrawlThread.waitForStart(120))
				throw new WebServiceException("Time out reached (120 seconds)");
			if (!webCrawlThread.waitForEnd(3600))
				throw new WebServiceException("Time out reached (3600 seconds)");
			UrlItem urlItem = webCrawlThread.getCurrentUrlItem();
			String message = urlItem != null ? "Result: "
					+ urlItem.getFetchStatus() + " - "
					+ urlItem.getParserStatus() + " - "
					+ urlItem.getIndexStatus() : null;
			CommonResult cr = new CommonResult(true, message);
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
			return cr;
		} catch (MalformedURLException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (ParseException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SyntaxError e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult crawlPost(String use, String login, String key,
			String url) {
		return crawl(use, login, key, url);
	}

	@Override
	public CommonResult captureScreenshot(String use, String login, String key,
			String url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			CredentialManager credentialManager = client
					.getWebCredentialManager();
			ScreenshotServlet.doCapture(null, screenshotManager,
					credentialManager, LinkUtils.newEncodedURL(url));
			String message = "Captured URL " + url;
			return new CommonResult(true, message);
		} catch (MalformedURLException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult checkScreenshot(String use, String login, String key,
			String url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			String message = ScreenshotServlet.doCheck(screenshotManager,
					LinkUtils.newEncodedURL(url));
			return new CommonResult(true, message);
		} catch (MalformedURLException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	public static String getCrawlXML(User user, Client client, String url)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/index/{index}/crawler/web/crawl",
				user, client, "url", url);
	}

	public static String getCrawlJSON(User user, Client client, String url)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/index/{index}/crawler/web/crawl",
				user, client, "url", url);
	}

	@Override
	public CommonResult injectUrls(String index, String login, String key,
			boolean replaceAll, List<String> urls) {
		try {
			Client client = getLoggedClientAnyRole(index, login, key,
					Role.WEB_CRAWLER_EDIT_PARAMETERS);
			ClientFactory.INSTANCE.properties.checkApi();
			UrlManager urlManager = client.getUrlManager();
			CommonResult result = new CommonResult(true, null);
			if (replaceAll)
				urlManager.deleteAll(null);
			urlManager.inject(urls, result);
			return result;
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

}
