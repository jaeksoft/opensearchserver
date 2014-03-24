/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.morelikethis;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.webservice.query.QueryAbstract;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.Filter;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.GeoFilter;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.QueryFilter;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.RelativeDateFilter;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.TermFilter;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class MoreLikeThisQuery extends QueryAbstract {

	final public String docQuery;
	final public String likeText;
	final public LanguageEnum lang;
	final public String analyzerName;
	final public List<String> fields;
	final public Integer minWordLen;
	final public Integer maxWordLen;
	final public Integer minDocFreq;
	final public Integer minTermFreq;
	final public Integer maxNumTokensParsed;
	final public Integer maxQueryTerms;
	final public Boolean boost;
	final public String stopWords;
	final public List<String> returnedFields;
	@XmlElements({
			@XmlElement(name = "QueryFilter", type = QueryFilter.class),
			@XmlElement(name = "TermFilter", type = TermFilter.class),
			@XmlElement(name = "GeoFilter", type = GeoFilter.class),
			@XmlElement(name = "RelativeDateFilter", type = RelativeDateFilter.class) })
	final public List<Filter> filters;
	final public Integer start;
	final public Integer rows;

	public MoreLikeThisQuery() {
		docQuery = null;
		likeText = null;
		lang = null;
		analyzerName = null;
		fields = null;
		minWordLen = null;
		maxWordLen = null;
		minDocFreq = null;
		minTermFreq = null;
		maxNumTokensParsed = null;
		maxQueryTerms = null;
		boost = null;
		stopWords = null;
		returnedFields = null;
		filters = null;
		start = null;
		rows = null;
	}

	public MoreLikeThisQuery(MoreLikeThisRequest request) {
		docQuery = request.getDocQuery();
		likeText = request.getLikeText();
		lang = request.getLang();
		analyzerName = request.getAnalyzerName();
		fields = request.getFieldList().toNameList(new ArrayList<String>(1));
		minWordLen = request.getMinWordLen();
		maxWordLen = request.getMaxWordLen();
		minDocFreq = request.getMinDocFreq();
		minTermFreq = request.getMinTermFreq();
		maxNumTokensParsed = request.getMaxNumTokensParsed();
		maxQueryTerms = request.getMaxQueryTerms();
		boost = request.getBoost();
		stopWords = request.getStopWords();
		returnedFields = request.getReturnFieldList().toNameList(
				new ArrayList<String>(1));
		filters = SearchQueryAbstract.newFilterList(request.getFilterList());
		start = request.getStart();
		rows = request.getRows();
	}

	public void apply(MoreLikeThisRequest request) {
		super.apply(request);
		if (docQuery != null)
			request.setDocQuery(docQuery);
		if (likeText != null)
			request.setLikeText(likeText);
		if (lang != null)
			request.setLang(lang);
		if (analyzerName != null)
			request.setAnalyzerName(analyzerName);
		if (fields != null)
			for (String field : fields)
				request.getFieldList().put(new ReturnField(field));
		if (returnedFields != null)
			for (String returnedField : returnedFields)
				request.getReturnFieldList()
						.put(new ReturnField(returnedField));
		if (minWordLen != null)
			request.setMinWordLen(minWordLen);
		if (maxWordLen != null)
			request.setMaxWordLen(maxWordLen);
		if (minDocFreq != null)
			request.setMinDocFreq(minDocFreq);
		if (minTermFreq != null)
			request.setMinTermFreq(minTermFreq);
		if (maxNumTokensParsed != null)
			request.setMaxNumTokensParsed(maxNumTokensParsed);
		if (maxQueryTerms != null)
			request.setMaxQueryTerms(maxQueryTerms);
		if (boost != null)
			request.setBoost(boost);
		if (stopWords != null)
			request.setStopWords(stopWords);
		if (filters != null)
			for (Filter filter : filters)
				request.getFilterList().add(filter.newFilter());
		if (start != null)
			request.setStart(start);
		if (rows != null)
			request.setRows(rows);
	}
}
