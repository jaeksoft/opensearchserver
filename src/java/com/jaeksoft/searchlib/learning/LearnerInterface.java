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

package com.jaeksoft.searchlib.learning;

import java.io.IOException;
import java.util.Map;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public interface LearnerInterface {

	/**
	 * Load the learner (reading any data on file)
	 * 
	 * @param client
	 * @param learner
	 */
	void init(Client client, Learner learner);

	/**
	 * Remove any data in the learner
	 * */
	public void reset();

	/**
	 * Classify the document passed in parameter
	 * 
	 * @param client
	 * @param indexDocument
	 * @throws IOException
	 */
	public void classify(IndexDocument indexDocument) throws IOException;

	/**
	 * Classify a block of text passed in parameter
	 * 
	 * @param data
	 * @throws IOException
	 */
	public Map<Double, String> classify(String data) throws IOException;

	/**
	 * Learn by reading the document returned by the search query
	 * 
	 * @param taskLog
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void learn(TaskLog taskLog) throws SearchLibException, IOException;

	/**
	 * Return the list of field required by the learner when learning
	 * 
	 * @return
	 */
	public String[] getSourceFieldList();

	/**
	 * Return the list of field provided by the classifier
	 * 
	 * @return
	 */
	public String[] getTargetFieldList();
}
