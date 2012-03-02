/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.api.ApiManager;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderOpenSearch;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class SearchServlet extends AbstractServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4440539263609727717L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {

			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.INDEX_QUERY))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClientApi(getIndexName());
			SearchRequest searchRequest = buildOpenSearchRequest(client,
					transaction);
			Render render = null;
			if (transaction.getParameterString("oe") != null)
				render = doQueryRequest(client, searchRequest,
						transaction.getParameterString("oe"));
			else
				render = doQueryRequest(client, searchRequest, null);
			render.render(transaction);

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	protected Render doQueryRequest(Client client, SearchRequest searchRequest,
			String outputEncoding) throws IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException,
			IllegalAccessException {

		AbstractResultSearch result = (AbstractResultSearch) client
				.request(searchRequest);
		return new RenderOpenSearch(result, serverURL, outputEncoding);

	}

	private SearchRequest buildOpenSearchRequest(Client client,
			ServletTransaction transaction)
			throws TransformerConfigurationException, SearchLibException,
			XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		ApiManager apiManager = client.getApiManager();

		SearchRequest searchRequest = (SearchRequest) client
				.getNewRequest(apiManager.getFieldValue("opensearch").trim());
		searchRequest.setQueryString(transaction.getParameterString("q"));
		if (transaction.getParameterInteger("start") != null)
			searchRequest.setStart(transaction.getParameterInteger("start"));

		if (transaction.getParameterInteger("num") != null)
			searchRequest.setRows(transaction.getParameterInteger("num"));

		if (transaction.getParameterString("hl") != null)
			searchRequest.setLang(LanguageEnum.findByCode(transaction
					.getParameterString("hl")));

		return searchRequest;
	}

	private String getIndexName() {
		String use = null;
		if (StringUtils.substringBetween(serverURL, "search/", "/") != null)
			use = StringUtils.substringBetween(serverURL, "search/", "/");
		else
			use = StringUtils.substringBetween(serverURL, "search/", "?");
		return use;
	}

	public static StringBuffer getOpenSearchApiUrl(StringBuffer sb,
			String servletPathName, Client client, User user)
			throws UnsupportedEncodingException {
		String q = null;
		sb.append(servletPathName);
		sb.append("/");
		sb.append(URLEncoder.encode(client.getIndexName(), "UTF-8"));
		q = "*:*";
		sb.append("?q=");
		sb.append(URLEncoder.encode(q, "UTF-8"));
		if (user != null)
			user.appendApiCallParameters(sb);
		return sb;
	}
}
