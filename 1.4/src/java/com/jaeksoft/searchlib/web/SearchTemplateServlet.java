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

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckDistanceEnum;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class SearchTemplateServlet extends AbstractServlet {

	private static final long serialVersionUID = -6279193437936726711L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {

		boolean done = false;
		try {
			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.INDEX_QUERY))
				throw new SearchLibException("Not permitted");
			String cmd = transaction.getParameterString("cmd");
			if (cmd.equalsIgnoreCase("create")) {
				done = createTemplate(user, transaction);
				transaction.addXmlResponse("Status", "OK");
			}
			if (cmd.equalsIgnoreCase("delete")) {
				done = deletTemplate(user, transaction);
			}
			if (cmd.equalsIgnoreCase("setreturnfield")) {
				done = setReturnField(user, transaction);
			}
			if (cmd.equalsIgnoreCase("setsnippetfield")) {
				done = setSnippetField(user, transaction);
			}
			if (!done)
				transaction.addXmlResponse("Info", "Nothing to do");

		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (XPathExpressionException e) {
			throw new ServletException(e);
		} catch (DOMException e) {
			throw new ServletException(e);
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (ParseException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}

	}

	private boolean setSnippetField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		String searchTemplate = transaction.getParameterString("qt.name");
		String maxSnippetSize = transaction
				.getParameterString("qt.maxSnippetSize");
		String tag = transaction.getParameterString("qt.tag");
		String snippetField = transaction.getParameterString("snippetfield");
		String fragmenter = transaction.getParameterString("qt.fragmenter");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			SearchRequest request = (SearchRequest) client.getRequestMap().get(
					searchTemplate);
			if (snippetField != null) {
				request.getSnippetFieldList().put(
						new SnippetField(snippetField));
				SnippetField snippetFieldParameter = request
						.getSnippetFieldList().get(snippetField);
				if (maxSnippetSize != null && !maxSnippetSize.equals(""))
					snippetFieldParameter.setMaxSnippetSize(Integer
							.parseInt(maxSnippetSize));
				if (tag != null && !tag.equals(""))
					snippetFieldParameter.setTag(tag);
				if (fragmenter != null && !fragmenter.equals(""))
					snippetFieldParameter.setFragmenter(fragmenter);
				client.getRequestMap().put(request);
				client.saveRequests();
				transaction.addXmlResponse("Status", "OK");
			}
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;

	}

	private boolean setReturnField(User user, ServletTransaction transaction)
			throws SearchLibException, NamingException {
		String searchTemplate = transaction.getParameterString("qt.name");
		String returnField = transaction.getParameterString("returnfield");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			SearchRequest request = (SearchRequest) client.getRequestMap().get(
					searchTemplate);
			request.addReturnField(returnField);
			client.getRequestMap().put(request);
			client.saveRequests();
			transaction.addXmlResponse("Status", "OK");
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;

	}

	private boolean deletTemplate(User user, ServletTransaction transaction)
			throws InterruptedException, SearchLibException, NamingException {
		String searchTemplate = transaction.getParameterString("qt.name");
		Client client = transaction.getClient();
		if (client.getRequestMap().get(searchTemplate) != null) {
			client.getRequestMap().remove(searchTemplate);
			client.saveRequests();
			transaction.addXmlResponse("Status", "OK");
		} else {
			transaction.addXmlResponse("Info", "SearchTemplate Not Found");

		}
		return true;
	}

	private void createMoreLikeThisTemplate(
			MoreLikeThisRequest moreLikeThisRequest,
			ServletTransaction transaction) throws ParseException {

		String p;
		if ((p = transaction.getParameterString("qt.name")) != null)
			moreLikeThisRequest.setRequestName(p);

		if ((p = transaction.getParameterString("qt.query")) != null)
			moreLikeThisRequest.setDocQuery(p);

		if ((p = transaction.getParameterString("qt.lang")) != null)
			moreLikeThisRequest.setLang(LanguageEnum.findByNameOrCode(p));

		if ((p = transaction.getParameterString("qt.like")) != null)
			moreLikeThisRequest.setLikeText(p);

		if ((p = transaction.getParameterString("qt.analyzer")) != null)
			moreLikeThisRequest.setAnalyzerName(p);

		Integer i;
		if ((i = transaction.getParameterInteger("qt.minwordlen")) != null)
			moreLikeThisRequest.setMinWordLen(i);

		if ((i = transaction.getParameterInteger("qt.maxwordlen")) != null)
			moreLikeThisRequest.setMaxWordLen(i);

		if ((i = transaction.getParameterInteger("qt.mindocfreq")) != null)
			moreLikeThisRequest.setMinDocFreq(i);

		if ((i = transaction.getParameterInteger("qt.mintermfreq")) != null)
			moreLikeThisRequest.setMinTermFreq(i);

		if ((i = transaction.getParameterInteger("qt.maxqueryTerms")) != null)
			moreLikeThisRequest.setMaxQueryTerms(i);

		if ((i = transaction.getParameterInteger("qt.maxnumtokensparsed")) != null)
			moreLikeThisRequest.setMaxNumTokensParsed(i);

		if ((p = transaction.getParameterString("qt.stopwords")) != null)
			moreLikeThisRequest.setStopWords(p);

		if ((i = transaction.getParameterInteger("qt.rows")) != null)
			moreLikeThisRequest.setRows(i);

		if ((i = transaction.getParameterInteger("qt.start")) != null)
			moreLikeThisRequest.setStart(i);

		if ((p = transaction.getParameterString("qt.fields")) != null) {
			String fields[] = p.split("\\,");
			for (String mltField : fields) {
				moreLikeThisRequest.getFieldList().put(
						new ReturnField(mltField));
			}
		}

		if ((p = transaction.getParameterString("qt.returnfields")) != null) {
			String returnFields[] = p.split("\\,");
			for (String mltReturnField : returnFields) {
				moreLikeThisRequest.getReturnFieldList().put(
						new ReturnField(mltReturnField));
			}
		}

		String[] values;
		if ((values = transaction.getParameterValues("qt.fq")) != null) {
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						moreLikeThisRequest.addFilter(value, false);
		}

		if ((values = transaction.getParameterValues("qt.fqn")) != null) {
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						moreLikeThisRequest.addFilter(value, true);
		}

	}

	private void createSearchTemplate(SearchRequest request,
			ServletTransaction transaction) {
		String p;
		if ((p = transaction.getParameterString("qt.name")) != null)
			request.setRequestName(p);

		if ((p = transaction.getParameterString("qt.query")) != null)
			request.setPatternQuery(p);

		if ((p = transaction.getParameterString("qt.operator")) != null)
			request.setDefaultOperator(p);

		Integer i;
		if ((i = transaction.getParameterInteger("qt.rows")) != null)
			request.setRows(i);

		if ((i = transaction.getParameterInteger("qt.slop")) != null)
			request.setPhraseSlop(i);

		if ((p = transaction.getParameterString("qt.lang")) != null)
			request.setLang(LanguageEnum.findByNameOrCode(p));

	}

	private void createSpellTemplate(SpellCheckRequest spellCheckRequest,
			ServletTransaction transaction) {
		SpellCheckField spellCheckField = new SpellCheckField();

		String p;
		if ((p = transaction.getParameterString("qt.name")) != null)
			spellCheckRequest.setRequestName(p);

		if ((p = transaction.getParameterString("qt.query")) != null)
			spellCheckRequest.setQueryString(p);

		Integer i;
		if ((i = transaction.getParameterInteger("qt.suggestions")) != null)
			spellCheckField.setSuggestionNumber(i);

		if ((p = transaction.getParameterString("qt.field")) != null)
			spellCheckField.setName(p);

		if ((p = transaction.getParameterString("qt.lang")) != null)
			spellCheckRequest.setLang(LanguageEnum.findByNameOrCode(p));

		if ((p = transaction.getParameterString("qt.algorithm")) != null)
			spellCheckField.setStringDistance(SpellCheckDistanceEnum.find(p));

		spellCheckRequest.getSpellCheckFieldList().put(spellCheckField);
	}

	private boolean createTemplate(User user, ServletTransaction transaction)
			throws InterruptedException, SearchLibException, NamingException,
			ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Client client = transaction.getClient();
		String queryType = transaction.getParameterString("qt.type");
		if (queryType == null
				|| RequestTypeEnum.SearchRequest.name().equalsIgnoreCase(
						queryType)) {
			SearchRequest request = new SearchRequest(client);
			createSearchTemplate(request, transaction);
			client.getRequestMap().put(request);
		}
		if (RequestTypeEnum.SpellCheckRequest.name()
				.equalsIgnoreCase(queryType)) {
			SpellCheckRequest spellCheckRequest = new SpellCheckRequest(client);
			createSpellTemplate(spellCheckRequest, transaction);
			client.getRequestMap().put(spellCheckRequest);
		}
		if (RequestTypeEnum.MoreLikeThisRequest.name().equalsIgnoreCase(
				queryType)) {
			MoreLikeThisRequest moreLikeThisRequest = new MoreLikeThisRequest(
					client);
			createMoreLikeThisTemplate(moreLikeThisRequest, transaction);
			client.getRequestMap().put(moreLikeThisRequest);
		}
		client.saveRequests();
		return true;
	}
}
