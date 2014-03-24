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
import java.util.List;
import java.util.Map;

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
	 * Close the learner (free resource in memory)
	 * 
	 * @throws SearchLibException
	 * 
	 */
	public void close() throws SearchLibException;

	/**
	 * Remove any data in the learner
	 * 
	 * @throws SearchLibException
	 * 
	 */
	public void reset() throws SearchLibException;

	/**
	 * Learn from the document, and classify it the document if target are set
	 * 
	 * @param client
	 * @param requestName
	 * @param documents
	 * @param sourceFieldMap
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public void learn(Client client, String requestName,
			Collection<IndexDocument> documents, FieldMap sourceFieldMap)
			throws IOException, SearchLibException;

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
	public void classify(String data, FieldMap sourceFieldMap, int maxRank,
			double minScore, Collection<LearnerResultItem> collector)
			throws IOException, SearchLibException;

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
	public void similar(String data, FieldMap sourceFieldMap, int maxRank,
			double minScore, Collection<LearnerResultItem> collector)
			throws IOException, SearchLibException;

	/**
	 * Learn by reading the document returned by the search query
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
	 * Remove the mapped document
	 * 
	 * @param client
	 * @param searchRequest
	 * @param field
	 * @param values
	 * @param sourceFieldMap
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void remove(Client client, String searchRequest, String field,
			Collection<String> values, FieldMap sourceFieldMap)
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

	/**
	 * Return a map with the custom fields linked to the provided name
	 * 
	 * @param name
	 * @return
	 * @throws SearchLibException
	 */
	public Map<String, List<String>> getCustoms(String name)
			throws SearchLibException;

	/**
	 * 
	 * @return The number of documents in the learner
	 * @throws SearchLibException
	 * @throws IOException
	 */
	long getDocumentCount() throws IOException, SearchLibException;

}
