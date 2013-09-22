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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.InfoCallback;

public interface LearnerInterface {

	/**
	 * Load the learner (reading any data on file)
	 * 
	 * @throws SearchLibException
	 * 
	 */
	void init(File instancesFile) throws SearchLibException;

	/**
	 * Remove any data in the learner
	 * 
	 * @throws SearchLibException
	 * 
	 */
	public void reset() throws SearchLibException;

	/**
	 * Classify the document passed in parameter
	 * 
	 * @param source
	 * @param sourceFieldMap
	 * @param targetFieldMap
	 * @param maxRank
	 * @param minScore
	 * @throws IOException
	 */
	public void classify(IndexDocument source, FieldMap sourceFieldMap,
			FieldMap targetFieldMap, int maxRank, double minScore)
			throws IOException;

	/**
	 * Classify a block of text passed in parameter
	 * 
	 * @param data
	 * @param maxRank
	 * @param minScore
	 * @param collector
	 * @return
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public void classify(String data, int maxRank, double minScore,
			Collection<LearnerResultItem> collector) throws IOException,
			SearchLibException;

	/**
	 * Return the closest items
	 * 
	 * @param data
	 * @param maxRank
	 * @param minScore
	 * @param collector
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public void similar(String data, int maxRank, double minScore,
			Collection<LearnerResultItem> collector) throws IOException,
			SearchLibException;

	/**
	 * SearchLibException Learn by reading the document returned by the search
	 * query
	 * 
	 * @param client
	 * @param requestName
	 * @param sourceFieldMap
	 * @param buffer
	 * @param taskLog
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void learn(Client client, String requestName,
			FieldMap sourceFieldMap, int buffer, InfoCallback infoCallback)
			throws SearchLibException, IOException;

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
