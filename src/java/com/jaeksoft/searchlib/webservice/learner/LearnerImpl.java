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

package com.jaeksoft.searchlib.webservice.learner;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class LearnerImpl extends CommonServices implements SoapLearner,
		RestLearner {

	@Override
	public LearnerResult classify(String index, String login, String key,
			String name, String text) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			Learner learner = getLearner(client, name);
			return new LearnerResult(learner.classify(client, text));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	private Learner getLearner(Client client, String name)
			throws SearchLibException {
		LearnerManager manager = client.getLearnerManager();
		Learner learner = manager.get(name);
		if (learner == null)
			throw new WebServiceException("Learner " + name + " not found");
		return learner;
	}

	@Override
	public LearnerResult classifyXML(String index, String login, String key,
			String name, String text) {
		return classify(index, login, key, name, text);
	}

	@Override
	public LearnerResult classifyJSON(String index, String login, String key,
			String name, String text) {
		return classify(index, login, key, name, text);
	}

	@Override
	public CommonResult reset(String index, String login, String key,
			String name) {
		try {
			Client client = getLoggedClient(index, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			Learner learner = getLearner(client, name);
			learner.reset(client);
			return new CommonResult(true, null);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult resetXML(String index, String login, String key,
			String name) {
		return reset(index, login, key, name);
	}

	@Override
	public CommonResult resetJSON(String index, String login, String key,
			String name) {
		return reset(index, login, key, name);
	}

	@Override
	public CommonResult learn(String index, String login, String key,
			String name) {
		try {
			Client client = getLoggedClient(index, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			Learner learner = getLearner(client, name);
			learner.learn(client, null);
			return new CommonResult(true, null);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult learnXML(String index, String login, String key,
			String name) {
		return learn(index, login, key, name);
	}

	@Override
	public CommonResult learnJSON(String index, String login, String key,
			String name) {
		return learn(index, login, key, name);
	}

}
