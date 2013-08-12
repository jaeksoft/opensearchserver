/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.search;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.crawler.common.database.TimeInterval.IntervalUnit;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.GeoFilter.CoordUnit;
import com.jaeksoft.searchlib.filter.GeoFilter.Type;
import com.jaeksoft.searchlib.filter.GeoFilter.Unit;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.snippet.NoFragmenter;
import com.jaeksoft.searchlib.snippet.SentenceFragmenter;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.webservice.CommonServices.CommonServiceException;

@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class SearchQueryAbstract {

	final public String query;
	final public Integer start;
	final public Integer rows;
	final public LanguageEnum lang;
	final public OperatorEnum operator;
	final public Collapsing collapsing;
	final public List<Filter> filters;
	final public List<Sort> sorts;
	final public List<String> returnedFields;
	final public List<Snippet> snippets;
	final public List<Facet> facets;
	final public List<Join> joins;
	final public Boolean enableLog;
	final public List<String> customLogs;

	public enum OperatorEnum {
		AND, OR;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Collapsing {

		final public String field;
		final public Integer max;
		final public CollapseParameters.Mode mode;
		final public CollapseParameters.Type type;

		public Collapsing(AbstractSearchRequest request) {
			field = request.getCollapseField();
			max = request.getCollapseMax();
			mode = request.getCollapseMode();
			type = request.getCollapseType();
		}

		public void apply(AbstractSearchRequest request) {
			if (field != null)
				request.setCollapseField(field);
			if (max != null)
				request.setCollapseMax(max);
			if (mode != null)
				request.setCollapseMode(mode);
			if (type != null)
				request.setCollapseType(type);
		}
	}

	public static abstract class Filter {

		final public Boolean negative;

		protected Filter(Boolean negative) {
			this.negative = negative;
		}

		protected void apply(FilterAbstract<?> filter) {
			if (negative != null)
				filter.setNegative(negative);
		}

		public abstract FilterAbstract<?> getFilter();
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class QueryFilter extends Filter {

		final public String query;

		protected QueryFilter(com.jaeksoft.searchlib.filter.QueryFilter src) {
			super(src.isNegative());
			this.query = src.getQueryString();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.QueryFilter queryFilter = (com.jaeksoft.searchlib.filter.QueryFilter) filter;
			if (query != null)
				queryFilter.setQueryString(query);
		}

		@Override
		public FilterAbstract<?> getFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.QueryFilter();
			apply(filter);
			return filter;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class GeoFilter extends Filter {

		final public Unit unit;
		final public Type type;
		final public Double distance;
		final public String latitudeField;
		final public String longitudeField;
		final public Double latitude;
		final public Double longitude;
		final public CoordUnit coordUnit;

		public GeoFilter(com.jaeksoft.searchlib.filter.GeoFilter src) {
			super(src.isNegative());
			unit = src.getUnit();
			type = src.getType();
			distance = src.getDistance();
			latitudeField = src.getLatitudeField();
			longitudeField = src.getLongitudeField();
			latitude = src.getLatitude();
			longitude = src.getLongitude();
			coordUnit = src.getCoordUnit();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.GeoFilter geoFilter = (com.jaeksoft.searchlib.filter.GeoFilter) filter;
			if (unit != null)
				geoFilter.setUnit(unit);
			if (type != null)
				geoFilter.setType(type);
			if (distance != null)
				geoFilter.setDistance(distance);
			if (latitudeField != null)
				geoFilter.setLatitudeField(latitudeField);
			if (longitudeField != null)
				geoFilter.setLongitudeField(longitudeField);
			if (latitude != null)
				geoFilter.setLatitude(latitude);
			if (longitude != null)
				geoFilter.setLongitude(longitude);
			if (coordUnit != null)
				geoFilter.setCoordUnit(coordUnit);
		}

		@Override
		public FilterAbstract<?> getFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.GeoFilter();
			apply(filter);
			return filter;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class RelativeDateFilter extends Filter {

		final public TimeInterval from;
		final public TimeInterval to;
		final public String field;
		final public String dateFormat;

		public RelativeDateFilter(
				com.jaeksoft.searchlib.filter.RelativeDateFilter src) {
			super(src.isNegative());
			from = src.getFrom() == null ? null : new TimeInterval(
					src.getFrom());
			to = src.getTo() == null ? null : new TimeInterval(src.getTo());
			field = src.getField();
			dateFormat = src.getDateFormat();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.RelativeDateFilter dateFilter = (com.jaeksoft.searchlib.filter.RelativeDateFilter) filter;
			if (from != null)
				from.apply(dateFilter.getFrom());
			if (to != null)
				to.apply(dateFilter.getTo());
			if (field != null)
				dateFilter.setField(field);
			if (dateFormat != null)
				dateFilter.setDateFormat(dateFormat);
		}

		@Override
		public FilterAbstract<?> getFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.RelativeDateFilter();
			apply(filter);
			return filter;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class TimeInterval {

		final public IntervalUnit unit;
		final public Long interval;

		protected TimeInterval(
				com.jaeksoft.searchlib.crawler.common.database.TimeInterval src) {
			unit = src.getUnit();
			interval = src.getInterval();
		}

		protected void apply(
				com.jaeksoft.searchlib.crawler.common.database.TimeInterval timeInterval) {
			if (unit != null)
				timeInterval.setUnit(unit);
			if (interval != null)
				timeInterval.setInterval(interval);
		}
	}

	private List<Filter> newFilterList(FilterList filterList) {
		if (filterList == null)
			return null;
		if (filterList.size() == 0)
			return null;
		List<Filter> filters = new ArrayList<Filter>(filterList.size());
		for (FilterAbstract<?> filterAbstract : filterList) {
			switch (filterAbstract.getFilterType()) {
			case QUERY_FILTER:
				filters.add(new QueryFilter(
						(com.jaeksoft.searchlib.filter.QueryFilter) filterAbstract));
				break;
			case GEO_FILTER:
				filters.add(new GeoFilter(
						(com.jaeksoft.searchlib.filter.GeoFilter) filterAbstract));
				break;
			case RELATIVE_DATE_FILTER:
				filters.add(new RelativeDateFilter(
						(com.jaeksoft.searchlib.filter.RelativeDateFilter) filterAbstract));
				break;
			}
		}
		return filters;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Sort {

		final public String field;
		final public Direction direction;

		public Sort(SortField sortField) {
			field = sortField.getName();
			direction = sortField.isDesc() ? Direction.DESC : Direction.ASC;
		}

		protected SortField getSortField() {
			SortField sortField = new SortField(field, false);
			if (direction != null)
				sortField.setDirection(direction.name());
			return sortField;
		}
	}

	public enum Direction {
		ASC, DESC;
	}

	private static List<Sort> newSortList(SortFieldList sortFieldList) {
		if (sortFieldList == null)
			return null;
		if (sortFieldList.size() == 0)
			return null;
		List<Sort> sorts = new ArrayList<Sort>(sortFieldList.size());
		for (SortField sortField : sortFieldList)
			sorts.add(new Sort(sortField));
		return sorts;
	}

	private static List<String> newReturnFieldList(
			ReturnFieldList returnFieldList) {
		if (returnFieldList == null)
			return null;
		if (returnFieldList.size() == 0)
			return null;
		List<String> returns = new ArrayList<String>(returnFieldList.size());
		for (ReturnField returnField : returnFieldList)
			returns.add(returnField.getName());
		return returns;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Snippet {
		final public String field;
		final public String tag;
		final public String separator;
		final public Integer maxSize;
		final public Integer maxNumber;
		final public FragmenterEnum fragmenter;

		public Snippet(SnippetField snippetField) {
			field = snippetField.getName();
			tag = snippetField.getTag();
			separator = snippetField.getSeparator();
			maxSize = snippetField.getMaxSnippetSize();
			maxNumber = snippetField.getMaxSnippetNumber();
			fragmenter = FragmenterEnum.find(snippetField.getFragmenter());
		}

		protected SnippetField getSnippetField() throws InstantiationException,
				IllegalAccessException {
			SnippetField snippetField = new SnippetField(field);
			if (tag != null)
				snippetField.setTag(tag);
			if (separator != null)
				snippetField.setSeparator(separator);
			if (maxSize != null)
				snippetField.setMaxSnippetSize(maxSize);
			if (maxNumber != null)
				snippetField.setMaxSnippetNumber(maxNumber);
			if (fragmenter != null)
				snippetField.setFragmenter(fragmenter.className);
			return snippetField;
		}
	}

	public enum FragmenterEnum {

		NO(NoFragmenter.class.getSimpleName()),

		SENTENCE(SentenceFragmenter.class.getSimpleName());

		private final String className;

		private FragmenterEnum(String className) {
			this.className = className;
		}

		private static FragmenterEnum find(String className) {
			if (SENTENCE.className.equals(className))
				return SENTENCE;
			return NO;
		}
	}

	private static List<Snippet> newSnippetList(
			SnippetFieldList snippetFieldList) {
		if (snippetFieldList == null)
			return null;
		if (snippetFieldList.size() == 0)
			return null;
		List<Snippet> snippets = new ArrayList<Snippet>(snippetFieldList.size());
		for (SnippetField snippetField : snippetFieldList)
			snippets.add(new Snippet(snippetField));
		return snippets;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Facet {

		final public String field;
		final public Integer minCount;
		final public Boolean multivalued;
		final public Boolean postCollapsing;

		public Facet(FacetField facetField) {
			field = facetField.getName();
			minCount = facetField.getMinCount();
			multivalued = facetField.isCheckMultivalued();
			postCollapsing = facetField.isCheckPostCollapsing();
		}

		public FacetField getFacetField() {
			FacetField facetField = new FacetField();
			if (field != null)
				facetField.setName(field);
			if (minCount != null)
				facetField.setMinCount(minCount);
			if (multivalued != null)
				facetField.setMultivalued(multivalued);
			if (postCollapsing != null)
				facetField.setPostCollapsing(postCollapsing);
			return facetField;
		}
	}

	private static List<Facet> newFacetList(FacetFieldList facetFieldList) {
		if (facetFieldList == null)
			return null;
		if (facetFieldList.size() == 0)
			return null;
		List<Facet> facets = new ArrayList<Facet>(facetFieldList.size());
		for (FacetField facetField : facetFieldList)
			facets.add(new Facet(facetField));
		return facets;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Join {

		final public String indexName;
		final public String queryTemplate;
		final public String queryString;
		final public String localField;
		final public String foreignField;
		final public Boolean returnFields;
		final public Boolean returnScores;
		final public Boolean returnFacets;

		public Join(JoinItem joinItem) {
			indexName = joinItem.getIndexName();
			queryTemplate = joinItem.getQueryTemplate();
			queryString = joinItem.getQueryString();
			localField = joinItem.getLocalField();
			foreignField = joinItem.getForeignField();
			returnFields = joinItem.isReturnFields();
			returnScores = joinItem.isReturnScores();
			returnFacets = joinItem.isReturnFacets();
		}

		protected JoinItem getJoinItem() {
			JoinItem joinItem = new JoinItem();
			if (indexName != null)
				joinItem.setIndexName(indexName);
			if (queryTemplate != null)
				joinItem.setQueryTemplate(queryTemplate);
			if (queryString != null)
				joinItem.setQueryString(queryString);
			if (localField != null)
				joinItem.setLocalField(localField);
			if (foreignField != null)
				joinItem.setForeignField(foreignField);
			if (returnFields != null)
				joinItem.setReturnFields(returnFields);
			if (returnScores != null)
				joinItem.setReturnScores(returnScores);
			if (returnFacets != null)
				joinItem.setReturnFacets(returnFacets);
			return joinItem;
		}
	}

	private static List<Join> newJoinList(JoinList joinList) {
		if (joinList == null)
			return null;
		if (joinList.size() == 0)
			return null;
		List<Join> joins = new ArrayList<Join>(joinList.size());
		for (JoinItem joinItem : joinList)
			joins.add(new Join(joinItem));
		return joins;
	}

	private static List<String> newLogList(List<String> customLogs) {
		if (customLogs == null)
			return null;
		if (customLogs.size() == 0)
			return null;
		return new ArrayList<String>(customLogs);
	}

	public SearchQueryAbstract(AbstractSearchRequest request) {
		query = request.getQueryString();
		start = request.getStart();
		rows = request.getRows();
		lang = request.getLang();
		operator = request.getDefaultOperator() == null ? null : OperatorEnum
				.valueOf(request.getDefaultOperator());
		collapsing = new Collapsing(request);
		filters = newFilterList(request.getFilterList());
		sorts = newSortList(request.getSortFieldList());
		returnedFields = newReturnFieldList(request.getReturnFieldList());
		snippets = newSnippetList(request.getSnippetFieldList());
		facets = newFacetList(request.getFacetFieldList());
		joins = newJoinList(request.getJoinList());
		enableLog = request.isLogReport();
		customLogs = newLogList(request.getCustomLogs());
	}

	public static SearchQueryAbstract newInstance(AbstractSearchRequest request) {
		if (request == null)
			return null;
		switch (request.requestType) {
		case SearchFieldRequest:
			return new SearchFieldQuery((SearchFieldRequest) request);
		case SearchRequest:
			return new SearchPatternQuery((SearchPatternRequest) request);
		default:
			return null;
		}
	}

	public void apply(AbstractSearchRequest request) {
		try {
			if (query != null)
				request.setQueryString(query);
			if (start != null)
				request.setStart(start);
			if (rows != null)
				request.setRows(rows);
			if (lang != null)
				request.setLang(lang);
			if (operator != null)
				request.setDefaultOperator(operator.name());
			if (collapsing != null)
				collapsing.apply(request);
			if (filters != null)
				for (Filter filter : filters)
					request.getFilterList().add(filter.getFilter());
			if (sorts != null)
				for (Sort sort : sorts)
					request.getSortFieldList().put(sort.getSortField());
			if (returnedFields != null)
				for (String returnedField : returnedFields)
					request.getReturnFieldList().put(
							new ReturnField(returnedField));
			if (snippets != null)
				for (Snippet snippet : snippets)
					request.getSnippetFieldList()
							.put(snippet.getSnippetField());
			if (facets != null)
				for (Facet facet : facets)
					request.getFacetFieldList().put(facet.getFacetField());
			if (joins != null)
				for (Join join : joins)
					request.getJoinList().add(join.getJoinItem());
			if (enableLog != null)
				request.setLogReport(enableLog);
			if (customLogs != null)
				for (String customLog : customLogs)
					request.getCustomLogs().add(customLog);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		}
	}
}
