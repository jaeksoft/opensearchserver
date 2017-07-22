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

package com.jaeksoft.searchlib.webservice.learner;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;
import com.jaeksoft.searchlib.learning.LearnerResultItem;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

import java.io.IOException;

public class LearnerImpl extends CommonServices implements RestLearner {

	public enum LearnerMode {
		classify, similar;
	}

	private Learner getLearner(Client client, String name) throws SearchLibException {
		LearnerManager manager = client.getLearnerManager();
		Learner learner = manager.get(name);
		if (learner == null)
			throw new CommonServiceException("Learner " + name + " not found");
		return learner;
	}

	@Override
	public LearnerResult classify(String index_name, String login, String key, String learner_name, int max_rank,
			double min_score, LearnerMode mode, String text) {
		try {
			if (mode == null)
				mode = LearnerMode.classify;
			Client client = getLoggedClient(index_name, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			Learner learner = getLearner(client, learner_name);
			LearnerResultItem[] learnerResults = null;
			switch (mode) {
			case similar:
				learnerResults = learner.similar(client, text, max_rank, min_score);
				break;
			case classify:
				learnerResults = learner.classify(client, text, max_rank, min_score);
				break;
			}
			return new LearnerResult(learnerResults);
		} catch (SearchLibException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public LearnerResult classifyPost(String index_name, String login, String key, String learner_name, int max_rank,
			double min_score, LearnerMode mode, String text) {
		return classify(index_name, login, key, learner_name, max_rank, min_score, mode, text);
	}

	@Override
	public CommonResult learn(String index, String login, String key, String learner_name) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			Learner learner = getLearner(client, learner_name);
			learner.reset();
			CommonResult result = new CommonResult(true, null);
			learner.learn(result);
			return result;
		} catch (SearchLibException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}
}
