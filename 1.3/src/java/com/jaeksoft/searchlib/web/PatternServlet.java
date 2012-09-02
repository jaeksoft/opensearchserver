/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletInputStream;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.ServletTransaction.Method;

public class PatternServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5971245254548915864L;

	private List<PatternItem> inject(PatternManager patternManager,
			String patternTextList, boolean bDeleteAll)
			throws SearchLibException {
		List<PatternItem> patternList = PatternManager
				.getPatternList(patternTextList);
		patternManager.addList(patternList, bDeleteAll);
		return patternList;
	}

	private List<PatternItem> inject(PatternManager patternManager,
			ServletInputStream in, PrintWriter writer, boolean bDeleteAll)
			throws IOException, SearchLibException {
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);
			List<PatternItem> patternList = PatternManager
					.getPatternList(reader);
			patternManager.addList(patternList, bDeleteAll);
			return patternList;
		} catch (IOException e) {
			throw (e);
		} catch (SearchLibException e) {
			throw (e);
		} finally {
			if (reader != null)
				reader.close();
			if (isr != null)
				isr.close();
		}
	}

	private void doPatternList(List<PatternItem> patternList,
			UrlManager urlManager, PrintWriter writer, boolean bExclusion)
			throws SearchLibException {
		if (patternList == null)
			return;
		for (PatternItem item : patternList) {
			writer.print(item.getStatus().name());
			writer.print(": ");
			writer.println(item.getPattern());
			writer.flush();
		}
		if (!bExclusion)
			urlManager.injectPrefix(patternList);
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {

		try {

			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.WEB_CRAWLER_EDIT_PATTERN_LIST))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();

			boolean bDeleteAll = transaction.getParameterBoolean("deleteAll",
					"yes", false);
			boolean bExclusion = transaction.getParameterBoolean("type",
					"exclusion", false);

			PatternManager patternManager = bExclusion ? client
					.getExclusionPatternManager() : client
					.getInclusionPatternManager();
			UrlManager urlManager = client.getUrlManager();

			PrintWriter writer = transaction.getWriter("utf-8");
			String contentType = transaction.getResponseContentType();
			Method method = transaction.getMethod();
			List<PatternItem> patternList = null;
			if (contentType != null
					&& contentType
							.startsWith("application/x-www-form-urlencoded"))
				patternList = inject(patternManager,
						transaction.getParameterString("inject"), bDeleteAll);
			else if ((method == Method.PUT || method == Method.POST)
					&& (contentType == null || contentType
							.startsWith("text/plain")))
				patternList = inject(patternManager,
						transaction.getInputStream(), writer, bDeleteAll);
			doPatternList(patternList, urlManager, writer, bExclusion);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}