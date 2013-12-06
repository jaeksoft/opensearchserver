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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.map.TargetField;

public class StandardLearner implements LearnerInterface {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private File indexDir = null;

	private Client learnerClient = null;

	@Override
	public void init(File instancesDir) throws SearchLibException {
		rwl.r.lock();
		try {
			if (instancesDir != null && indexDir != null)
				if (instancesDir.compareTo(indexDir) == 0)
					return;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			closeNoLock();
			this.indexDir = instancesDir;
		} finally {
			rwl.w.unlock();
		}
	}

	private Collection<TargetField> checkDataFields(
			final FieldMap sourceFieldMap) throws SearchLibException {
		if (learnerClient == null)
			return null;
		Schema schema = learnerClient.getSchema();
		TreeMap<Float, TargetField> boostMap = new TreeMap<Float, TargetField>();
		sourceFieldMap.populateBoosts("data", boostMap);
		for (TargetField targetField : boostMap.values()) {
			String fieldName = targetField.getBoostedName();
			if (schema.getField(fieldName) != null)
				continue;
			schema.setField(fieldName, Stored.NO, Indexed.YES, TermVector.YES,
					"StandardAnalyzer");
		}
		return boostMap.values();
	}

	private Collection<TargetField> checkIndex(FieldMap sourceFieldMap)
			throws SearchLibException {
		if (learnerClient != null)
			return checkDataFields(sourceFieldMap);
		if (indexDir == null)
			throw new SearchLibException("Index directory not set");
		if (!indexDir.exists())
			indexDir.mkdir();
		learnerClient = new Client(indexDir,
				"/com/jaeksoft/searchlib/learner_config.xml", true);
		return checkDataFields(sourceFieldMap);
	}

	private void closeNoLock() {
		if (learnerClient == null)
			return;
		learnerClient.close();
		learnerClient = null;
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			closeNoLock();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void reset() throws SearchLibException {
		rwl.w.lock();
		try {
			if (learnerClient != null) {
				learnerClient.deleteAll();
				learnerClient.close();
				learnerClient.delete();
				learnerClient = null;
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void learn(Client client, String requestName,
			List<IndexDocument> sources, FieldMap sourceFieldMap,
			FieldMap targetFieldMap, int maxRank, double minScore)
			throws IOException, SearchLibException {
		if (CollectionUtils.isEmpty(sources))
			return;
		AbstractResultSearch result = null;
		List<IndexDocument> learnIndexDocuments = new ArrayList<IndexDocument>(
				sources.size());
		rwl.r.lock();
		try {
			checkIndex(sourceFieldMap);
			String uniqueField = client.getSchema().getUniqueField();
			if (StringUtils.isEmpty(uniqueField))
				return;
			for (IndexDocument source : sources) {
				AbstractSearchRequest request = (AbstractSearchRequest) client
						.getNewRequest(requestName);
				request.setStart(0);
				request.setRows(1);
				request.setEmptyReturnsAll(true);
				String uniqueTerm = source.getFieldValueString(uniqueField, 0);
				if (StringUtils.isEmpty(uniqueTerm))
					continue;
				request.addTermFilter(uniqueField, uniqueField, false);
				result = (AbstractResultSearch) client.request(request);
				if (result.getDocumentCount() != 1)
					continue;
				addNewlearnDocument(sourceFieldMap, result, 0,
						learnIndexDocuments);
			}
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			checkIndex(sourceFieldMap);
			learnerClient.updateDocuments(learnIndexDocuments);
		} finally {
			rwl.w.unlock();
		}
	}

	private BooleanQuery getBooleanQuery(String fieldName, String data)
			throws IOException, SearchLibException {
		BooleanQuery booleanQuery = new BooleanQuery();
		Schema schema = learnerClient.getSchema();
		SchemaField schemaField = schema.getFieldList().get(fieldName);
		Analyzer analyzer = schema.getAnalyzer(schemaField,
				LanguageEnum.UNDEFINED);
		analyzer.getQueryAnalyzer().toBooleanQuery(fieldName, data,
				booleanQuery, Occur.SHOULD);
		return booleanQuery;
	}

	@Override
	public void similar(String data, FieldMap sourceFieldMap, int maxRank,
			double minScore, Collection<LearnerResultItem> collector)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkIndex(sourceFieldMap);
			BooleanQuery booleanQuery = getBooleanQuery("data", data);
			if (booleanQuery == null || booleanQuery.getClauses() == null
					|| booleanQuery.getClauses().length == 0)
				return;
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) learnerClient
					.getNewRequest("search");
			int start = 0;
			final int rows = 1000;
			for (;;) {
				searchRequest.setStart(start);
				searchRequest.setRows(rows);
				searchRequest.setBoostedComplexQuery(booleanQuery);
				AbstractResultSearch result = (AbstractResultSearch) learnerClient
						.request(searchRequest);
				if (result.getDocumentCount() == 0)
					break;
				for (int i = 0; i < result.getDocumentCount(); i++) {
					int pos = start + i;
					double docScore = result.getScore(pos);
					if (docScore < minScore)
						break;
					ResultDocument document = result.getDocument(pos);
					FieldValue nameFieldValues = document.getReturnFields()
							.get("name");
					if (nameFieldValues == null)
						continue;
					for (FieldValueItem fvi : nameFieldValues.getValueArray()) {
						String value = fvi.getValue();
						if (value == null)
							continue;
						collector.add(new LearnerResultItem(docScore, pos,
								null, value, 1, null));
						break;
					}
				}
				searchRequest.reset();
				start += rows;
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private void fieldClassify(String fieldName, Float boost, String data,
			TreeMap<String, LearnerResultItem> targetMap)
			throws SearchLibException, IOException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) learnerClient
				.getNewRequest("search");
		BooleanQuery booleanQuery = getBooleanQuery(fieldName, data);
		if (booleanQuery == null || booleanQuery.getClauses() == null
				|| booleanQuery.getClauses().length == 0)
			return;
		int start = 0;
		final int rows = 1000;
		for (;;) {
			searchRequest.setStart(start);
			searchRequest.setRows(rows);
			searchRequest.setBoostedComplexQuery(booleanQuery);
			AbstractResultSearch result = (AbstractResultSearch) learnerClient
					.request(searchRequest);
			if (result.getDocumentCount() == 0)
				break;
			for (int i = 0; i < result.getDocumentCount(); i++) {
				int pos = start + i;
				double docScore = result.getScore(pos);
				ResultDocument document = result.getDocument(pos);
				FieldValue fieldValue = document.getReturnFields()
						.get("target");
				if (fieldValue == null)
					continue;
				if (boost != null)
					docScore = docScore * boost;
				for (FieldValueItem fvi : fieldValue.getValueArray()) {
					String value = fvi.getValue();
					if (value == null)
						continue;
					LearnerResultItem learnerResultItem = targetMap.get(value);
					if (learnerResultItem == null) {
						learnerResultItem = new LearnerResultItem(0, -1, value,
								null, 0, null);
						targetMap.put(value, learnerResultItem);
					}
					learnerResultItem.addScoreInstance(docScore, 1,
							document.getValueContent("name", 0));
				}
			}
			searchRequest.reset();
			start += rows;
		}
	}

	@Override
	public void classify(String data, FieldMap sourceFieldMap, int maxRank,
			double minScore, Collection<LearnerResultItem> collector)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			Collection<TargetField> targetFields = checkIndex(sourceFieldMap);
			TreeMap<String, LearnerResultItem> targetMap = new TreeMap<String, LearnerResultItem>();
			fieldClassify("data", null, data, targetMap);
			for (TargetField targetField : targetFields)
				fieldClassify(targetField.getBoostedName(),
						targetField.getBoost(), data, targetMap);
			for (LearnerResultItem learnerResultItem : targetMap.values()) {
				learnerResultItem.score = learnerResultItem.score
						/ learnerResultItem.count
						* Math.log1p(learnerResultItem.count);
				if (learnerResultItem.score > minScore)
					collector.add(learnerResultItem);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private void addNewlearnDocument(FieldMap sourceFieldMap,
			AbstractResultSearch result, int pos,
			Collection<IndexDocument> learnIndexDocuments) throws IOException,
			SearchLibException {
		ResultDocument resultDocument = result.getDocument(pos);
		if (resultDocument == null)
			return;
		IndexDocument target = new IndexDocument();
		sourceFieldMap.mapIndexDocument(resultDocument, target);
		sourceFieldMap.mapIndexDocumentJson("custom", resultDocument, target);
		List<ResultDocument> joinResultDocuments = result.getJoinDocumentList(
				pos, null);
		if (joinResultDocuments != null)
			for (ResultDocument joinResultDocument : joinResultDocuments)
				sourceFieldMap.mapIndexDocument(joinResultDocument, target);
		learnIndexDocuments.add(target);
	}

	@Override
	public void learn(Client client, String requestName,
			FieldMap sourceFieldMap, final int buffer, InfoCallback infoCallback)
			throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			checkIndex(sourceFieldMap);
			int count = 0;
			learnerClient.deleteAll();
			AbstractSearchRequest request = (AbstractSearchRequest) client
					.getNewRequest(requestName);
			int start = 0;
			List<IndexDocument> indexDocumentList = new ArrayList<IndexDocument>(
					buffer);
			request.setRows(buffer);
			request.setEmptyReturnsAll(true);
			for (;;) {
				request.setStart(start);
				AbstractResultSearch result = (AbstractResultSearch) client
						.request(request);
				if (result.getDocumentCount() == 0)
					break;
				for (int i = 0; i < result.getDocumentCount(); i++)
					addNewlearnDocument(sourceFieldMap, result, start + i,
							indexDocumentList);
				learnerClient.updateDocuments(indexDocumentList);
				count += indexDocumentList.size();
				indexDocumentList.clear();
				if (infoCallback != null)
					infoCallback.setInfo(count + " document(s) learned.");
				request.reset();
				start += buffer;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public Map<String, List<String>> getCustoms(String name)
			throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) learnerClient
				.getNewRequest("custom");
		searchRequest.setQueryString(name);
		AbstractResultSearch result = (AbstractResultSearch) learnerClient
				.request(searchRequest);
		if (result.getDocumentCount() == 0)
			return null;
		ResultDocument doc = result.getDocument(0);
		String json = doc.getValueContent("custom", 0);
		if (StringUtils.isEmpty(json))
			return null;
		try {
			return JsonUtils.getObject(json,
					JsonUtils.MapStringListStringTypeRef);
		} catch (JsonParseException e) {
			throw new SearchLibException(e);
		} catch (JsonMappingException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	private final String[] SOURCE_FIELDS = { "data", "target", "name", "custom" };

	@Override
	public String[] getSourceFieldList() {
		return SOURCE_FIELDS;
	}

	private final String[] TARGET_FIELDS = { "label", "score" };

	@Override
	public String[] getTargetFieldList() {
		return TARGET_FIELDS;
	}

}
