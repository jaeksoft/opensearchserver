/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;

public class WebPatternImpl extends CommonServicesImpl implements WebPattern {

	@Override
	public CommonResult webPattern(String use, String login, String key,
			Boolean deleteAll, List<String> injectList) {
		int count = 0;
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			List<PatternItem> patternList = new ArrayList<PatternItem>();
			Client client = ClientCatalog.getClient(use);
			if (isLogged(use, login, key)) {
				for (String inject : injectList) {
					patternList = inject(client.getInclusionPatternManager(),
							inject, deleteAll);
					count++;
				}
				addPatternList(patternList, client.getUrlManager());
			}

		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}

		if (count > 0)
			return new CommonResult(true, "inserted " + count + " web pattern ");
		else
			return new CommonResult(false, "Something Went Wrong");
	}

	private void addPatternList(List<PatternItem> patternList,
			UrlManager urlManager) throws SearchLibException {
		if (patternList == null)
			return;
		else
			urlManager.injectPrefix(patternList);
	}

	private List<PatternItem> inject(PatternManager patternManager,
			String patternTextList, boolean bDeleteAll)
			throws SearchLibException {
		List<PatternItem> patternList = PatternManager
				.getPatternList(patternTextList);
		patternManager.addList(patternList, bDeleteAll);
		return patternList;
	}

}
