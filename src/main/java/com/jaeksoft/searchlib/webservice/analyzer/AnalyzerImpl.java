/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.analyzer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.FilterScope;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.TokenTerm;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerImpl extends CommonServices implements RestAnalyzer {

	@Override
	public AnalyzerListResult list(String index, String login, String key) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			return new AnalyzerListResult(client.getSchema().getAnalyzerList());
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public AnalyzerResult get(String index, String login, String key, String name, LanguageEnum lang) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			return new AnalyzerResult(getAnalyzer(client, name, lang));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public AnalyzerTestResult test(String index, String login, String key, String name, LanguageEnum lang,
			FilterScope scope, String text) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			if (scope == null)
				scope = FilterScope.QUERY;
			Analyzer analyzer = getAnalyzer(client, name, lang);
			CompiledAnalyzer compiledAnalyzer = null;
			switch (scope) {
			case INDEX:
				compiledAnalyzer = analyzer.getIndexAnalyzer();
				break;
			case QUERY:
				compiledAnalyzer = analyzer.getQueryAnalyzer();
				break;
			default:
				throw new CommonServiceException("Scope must be INDEX or QUERY");
			}
			if (compiledAnalyzer == null)
				throw new CommonServiceException("No compiled analyzer");
			List<TokenTerm> tokenTerms = new ArrayList<TokenTerm>(0);
			compiledAnalyzer.populate(text, tokenTerms);
			return new AnalyzerTestResult(tokenTerms);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult put(String index, String login, String key, String name, LanguageEnum language,
			AnalyzerItem analyzer) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			AnalyzerList analyzerList = client.getSchema().getAnalyzerList();
			boolean created = analyzerList.addOrUpdate(analyzer.get(client, name, language));
			client.saveConfig();
			CommonResult result = new CommonResult(true, null);
			result.addDetail("transaction", created ? "created" : "updated");
			return result;
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult delete(String index, String login, String key, String name, LanguageEnum lang) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			Analyzer analyzer = getAnalyzer(client, name, lang);
			client.getSchema().getAnalyzerList().remove(analyzer);
			client.saveConfig();
			return new CommonResult(true, "Analyzer deleted " + name + " " + lang);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	private static final Analyzer getAnalyzer(Client client, String name, LanguageEnum lang) {
		Analyzer analyzer = client.getSchema().getAnalyzerList().get(name, lang);
		if (analyzer == null)
			throw new CommonServiceException(Status.NOT_FOUND, "Analyzer " + name + " not found");
		return analyzer;
	}

	@Override
	public AnalyzerTestResult testPost(String index, String login, String key, String name, LanguageEnum lang,
			FilterScope scope, String text) {
		return test(index, login, key, name, lang, scope, text);
	}

}
