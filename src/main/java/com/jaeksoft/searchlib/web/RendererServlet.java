/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.renderer.PagingSearchResult;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.RendererException.AuthException;
import com.jaeksoft.searchlib.renderer.RendererException.NoUserException;
import com.jaeksoft.searchlib.renderer.RendererManager;
import com.jaeksoft.searchlib.renderer.RendererTemplateEnum;
import com.jaeksoft.searchlib.renderer.filter.RendererFilterQueries;
import com.jaeksoft.searchlib.renderer.log.RendererLogField;
import com.jaeksoft.searchlib.renderer.log.RendererLogParameterEnum;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction.Method;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.qwazr.library.freemarker.FreeMarkerTool;
import freemarker.template.TemplateException;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class RendererServlet extends AbstractServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -9214023062084084833L;

	private static final String[] hiddenParameterList = { "use", "name", "login", "key", "jsp" };

	final private void template(final ServletTransaction transaction, final FreeMarkerTool freeMarkerTool,
			final Renderer renderer, final String path) throws ServletException, TemplateException {
		transaction.setRequestAttribute("hiddenParameterList", hiddenParameterList);
		transaction.setRequestAttribute("renderer", renderer);
		transaction.template(freeMarkerTool, path);
	}

	final private void token(Renderer renderer, String username, String password, PrintWriter pw)
			throws IOException, SearchLibException, TransformerConfigurationException, SAXException {
		AuthPluginInterface.User rendererUser = renderer.testAuthRequest(username, password);
		if (rendererUser == null)
			throw new SearchLibException("Authentication failed");
		String token = renderer.getTokens().generateToken(rendererUser.username, rendererUser.password);
		XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
		xmlWriter.startElement("response");
		xmlWriter.startElement("token");
		xmlWriter.textNode(token);
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

	@Override
	protected void doRequest(ServletTransaction transaction) throws ServletException, TemplateException {
		try {

			final Client client = transaction.getClient();

			// Prepare FreeMarker rendering
			final RendererManager rendererManager = client.getRendererManager();
			final Renderer renderer = rendererManager.get(transaction.getParameterString("name"));
			if (renderer == null)
				throw new SearchLibException("The renderer has not been found");
			final RendererTemplateEnum rendererTemplate;
			final String template = transaction.getParameterString("template");
			if (template != null)
				rendererTemplate = RendererTemplateEnum.find(template);
			else
				rendererTemplate = renderer.getTemplateName();
			final FreeMarkerTool freeMarkerTool = rendererManager.getFreeMarkerTool(rendererTemplate);

			try {
				final User user = transaction.getLoggedUser();
				if (user != null && !user.hasRole(transaction.getIndexName(), Role.INDEX_QUERY))
					throw new SearchLibException("Not permitted");

				// Generate Auth token
				if (transaction.getMethod() == Method.GET) {
					String username = transaction.getParameterString("username");
					String password = transaction.getParameterString("password");
					if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
						transaction.setResponseContentType("text/xml");
						PrintWriter pw = transaction.getWriter("UTF-8");
						token(renderer, username, password, pw);
						return;
					}
				}

				String query = transaction.getParameterString("query");
				AbstractSearchRequest searchRequest = (AbstractSearchRequest) client.getNewRequest(
						renderer.getRequestName());
				if (searchRequest == null)
					throw new SearchLibException("No search request has been found");
				AbstractSearchRequest facetRequest =
						searchRequest.isFacet() ? (AbstractSearchRequest) searchRequest.duplicate() : null;
				HttpServletRequest servletRequest = transaction.getRequest();
				setLog(renderer, searchRequest, servletRequest);
				searchRequest.setFromServlet(transaction, "");
				HttpSession session = servletRequest.getSession();
				RendererFilterQueries filterQueries = (RendererFilterQueries) session.getAttribute("filterQueries");
				if (filterQueries == null) {
					filterQueries = new RendererFilterQueries();
					session.setAttribute("filterQueries", filterQueries);
				}
				filterQueries.applyServletRequest(servletRequest);
				filterQueries.applyToSearchRequest(searchRequest);
				AuthPluginInterface.User loggedUser = renderer.configureAuthRequest(searchRequest, servletRequest);
				if (query == null)
					query = (String) transaction.getRequest().getSession().getAttribute("query");
				if (query != null && query.length() > 0) {
					searchRequest.setQueryString(query);
					Integer page = transaction.getParameterInteger("page");
					if (page != null) {
						if (page < 1)
							page = 1;
						searchRequest.setStart(searchRequest.getRows() * (page - 1));
					}
					AbstractResultSearch<?> result = (AbstractResultSearch<?>) client.request(searchRequest);
					transaction.setRequestAttribute("result", result);
					if (result != null) {
						transaction.setRequestAttribute("paging", new PagingSearchResult(result, 10));
						transaction.setRequestAttribute("rendererResult", ClientCatalog.getRendererResults()
								.addResult(client, renderer, searchRequest.getQueryString(), loggedUser));
					}
					if (facetRequest != null) {
						facetRequest.setQueryString(query);
						facetRequest.setStart(searchRequest.getStart());
						facetRequest.setRows(searchRequest.getRows());
						renderer.configureAuthRequest(facetRequest, servletRequest);
						AbstractResultSearch<?> facetResult = (AbstractResultSearch<?>) client.request(facetRequest);
						transaction.setRequestAttribute("facetResult", facetResult);
					}
				}
				session.setAttribute("query", query);
				transaction.setRequestAttribute("query", query);
				transaction.setRequestAttribute("renderer", renderer);
				StringBuilder getUrl = new StringBuilder("?query=");
				if (query != null)
					getUrl.append(URLEncoder.encode(query, "UTF-8"));
				for (String p : hiddenParameterList) {
					String v = transaction.getParameterString(p);
					if (v != null) {
						getUrl.append("&amp;");
						getUrl.append(p);
						getUrl.append('=');
						getUrl.append(URLEncoder.encode(v, "UTF-8"));
					}
				}
				transaction.setRequestAttribute("getUrl", getUrl.toString());
				StringBuilder autocompUrl = new StringBuilder("autocompletion?use=");
				autocompUrl.append(URLEncoder.encode(client.getIndexName(), "UTF-8"));
				if (user != null)
					user.appendApiCallParameters(autocompUrl);
				String name = renderer.getAutocompletionName();
				if (name != null) {
					autocompUrl.append("&name=");
					autocompUrl.append(URLEncoder.encode(name, "UTF-8"));
				}
				transaction.setRequestAttribute("autocompUrl", autocompUrl.toString());

				template(transaction, freeMarkerTool, renderer, "index.ftl");
			} catch (AuthException e) {
				ThreadUtils.sleepMs(1000);
				transaction.setRequestAttribute("error", e.getMessage());
				template(transaction, freeMarkerTool, renderer, "login.ftl");
			} catch (NoUserException e) {
				template(transaction, freeMarkerTool, renderer, "login.ftl");
			}
		} catch (TemplateException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void setCustomLogs(RendererLogField logField, HttpServletRequest servletRequest,
			AbstractSearchRequest searchRequest) {
		RendererLogParameterEnum rendererLogParameterEnum = logField.getLogParameterEnum();
		String s;
		if (rendererLogParameterEnum == RendererLogParameterEnum.IP)
			if ((s = servletRequest.getRemoteAddr()) != null)
				searchRequest.addCustomLog(s);
		if (rendererLogParameterEnum == RendererLogParameterEnum.HTTP_HEADER_FROM)
			if ((s = servletRequest.getRemoteUser()) != null)
				searchRequest.addCustomLog(s);
		if (rendererLogParameterEnum == RendererLogParameterEnum.HTTP_HEADER_REMOTE_USER)
			if ((s = servletRequest.getRemoteUser()) != null)
				searchRequest.addCustomLog(s);
		if (rendererLogParameterEnum == RendererLogParameterEnum.USER_SESSION_ID) {
			HttpSession session = servletRequest.getSession();
			if (session != null)
				searchRequest.addCustomLog(session.getId());
		}
	}

	private void setLog(Renderer renderer, AbstractSearchRequest searchRequest, HttpServletRequest servletRequest)
			throws SearchLibException {
		if (renderer.isLogEnabled()) {
			List<RendererLogField> rendererLogFields = renderer.getLogFields();
			if (rendererLogFields.size() > 0) {
				for (RendererLogField logField : rendererLogFields) {
					if (logField.getLogParameterEnum() != null && !logField.getLogParameterEnum().name().equals("")) {
						searchRequest.setLogReport(true);
						setCustomLogs(logField, servletRequest, searchRequest);
					}
				}
			}
		}
	}

	public static String doRenderer(String name, String query) throws UnsupportedEncodingException {
		StringBuilder sb = CommonController.getApiUrl("/renderer");
		if (name != null) {
			sb.append("&name=");
			sb.append(URLEncoder.encode(name, "UTF-8"));
		}
		if (query != null) {
			sb.append("&query=");
			sb.append(URLEncoder.encode(query, "UTF-8"));
		}
		return sb.toString();
	}
}
