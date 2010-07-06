/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.delete;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.lucene.queryParser.ParseException;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class DeleteController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3721788615871232348L;

	private class DeleteAlert extends AlertController {

		protected DeleteAlert(int num) throws InterruptedException {
			super(
					"Please, confirm that you want to delete the documents matching this query: "
							+ request.getQueryString() + ". " + num
							+ " document(s) will be erased", Messagebox.YES
							| Messagebox.NO, Messagebox.QUESTION);
		}

		@Override
		protected void onYes() throws SearchLibException {
			try {
				request.reset();
				getClient().deleteDocuments(request);
				reloadDesktop();
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (InstantiationException e) {
				throw new SearchLibException(e);
			} catch (IllegalAccessException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (SyntaxError e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (InterruptedException e) {
				throw new SearchLibException(e);
			}
		}

	}

	private SearchRequest request;

	public DeleteController() throws SearchLibException {
		super();
		request = getClient().getNewSearchRequest();
	}

	public SearchRequest getRequest() {
		return request;
	}

	public void onCheck() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		request.reset();
		int numFound = getClient().search(request).getNumFound();
		new AlertController(numFound + " document(s) found.",
				Messagebox.INFORMATION);
	}

	public void onDelete() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		request.reset();
		int numFound = getClient().search(request).getNumFound();
		new DeleteAlert(numFound);
	}

	public String getRequestApiCall() throws SearchLibException,
			UnsupportedEncodingException {
		String url = getBaseUrl()
				+ "/delete?use="
				+ URLEncoder.encode(getClient().getIndexDirectory().getName(),
						"UTF-8");
		String q = request.getQueryString();
		if (q == null)
			q = "";
		else
			q = q.replaceAll("\n", " ");
		url += "&q=" + URLEncoder.encode(q, "UTF-8");
		User user = getLoggedUser();
		if (user != null)
			url += "&" + user.getApiCallParameters();
		System.out.println("URL=" + url);
		return url;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
