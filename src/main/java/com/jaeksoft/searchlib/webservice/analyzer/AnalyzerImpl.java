/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.FilterScope;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.TokenTerm;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class AnalyzerImpl extends CommonServices implements SoapAnalyzer,
		RestAnalyzer {

	@Override
	public AnalyzerResult test(String index, String login, String key,
			String name, LanguageEnum lang, FilterScope scope, String text) {
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
			return new AnalyzerResult(tokenTerms);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	private Analyzer getAnalyzer(Client client, String name, LanguageEnum lang)
			throws SearchLibException {
		Analyzer analyzer = client.getSchema().getAnalyzerList()
				.get(name, lang);
		if (analyzer == null)
			throw new CommonServiceException("Analyzer " + name + " not found");
		return analyzer;
	}

	@Override
	public AnalyzerResult testPost(String index, String login, String key,
			String name, LanguageEnum lang, FilterScope scope, String text) {
		return test(index, login, key, name, lang, scope, text);
	}

}
