/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.lucene.queryParser.QueryParser.Operator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.collapse.CollapseParameters.Function;
import com.jaeksoft.searchlib.crawler.common.database.TimeInterval.IntervalUnit;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.GeoFilter.Type;
import com.jaeksoft.searchlib.filter.GeoFilter.Unit;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.geo.GeoParameters.CoordUnit;
import com.jaeksoft.searchlib.geo.GeoParameters.DistanceReturn;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.join.JoinItem.JoinType;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.scoring.AdvancedScoreItem;
import com.jaeksoft.searchlib.snippet.NoFragmenter;
import com.jaeksoft.searchlib.snippet.SentenceFragmenter;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.webservice.CommonServices.CommonServiceException;
import com.jaeksoft.searchlib.webservice.query.QueryAbstract;

@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class SearchQueryAbstract extends QueryAbstract {

	final public String query;
	final public Integer start;
	final public Integer rows;
	final public LanguageEnum lang;
	final public OperatorEnum operator;
	final public Collapsing collapsing;
	final public Geo geo;
	final public boolean emptyReturnsAll;

	@XmlElements({
			@XmlElement(name = "QueryFilter", type = QueryFilter.class),
			@XmlElement(name = "TermFilter", type = TermFilter.class),
			@XmlElement(name = "GeoFilter", type = GeoFilter.class),
			@XmlElement(name = "RelativeDateFilter", type = RelativeDateFilter.class),
			@XmlElement(name = "MirrorAndFilter", type = MirrorAndFilter.class),
			@XmlElement(name = "RequestTemplateFilter", type = RequestTemplateFilter.class) })
	final public List<Filter> filters;
	final public OperatorEnum filterOperator;

	final public List<Sort> sorts;
	final public List<String> returnedFields;
	final public List<Snippet> snippets;
	final public List<Facet> facets;
	final public List<Join> joins;
	final public List<Scoring> scorings;
	final public List<BoostingQuery> boostingQueries;
	final public Boolean enableLog;
	final public List<String> customLogs;

	public static enum BatchAction {

		/**
		 * The batch is stopped if the request found documents. IF not document
		 * was found, the batch execute the next request.
		 */
		STOP_IF_FOUND,

		/**
		 * Go to the next request even if the request found documents.
		 */
		CONTINUE
	}

	final public BatchAction batchAction;

	public SearchQueryAbstract() {
		query = null;
		start = null;
		rows = null;
		lang = null;
		operator = null;
		collapsing = null;
		geo = null;
		filters = null;
		filterOperator = null;
		sorts = null;
		returnedFields = null;
		snippets = null;
		facets = null;
		joins = null;
		scorings = null;
		boostingQueries = null;
		enableLog = null;
		customLogs = null;
		emptyReturnsAll = true;
		batchAction = null;
	}

	public enum OperatorEnum {

		AND(Operator.AND), OR(Operator.OR);

		@XmlTransient
		public final Operator lucop;

		private OperatorEnum(Operator operator) {
			lucop = operator;
		}

		public final static OperatorEnum find(String value) {
			for (OperatorEnum operator : values())
				if (value.equalsIgnoreCase(operator.name()))
					return operator;
			return null;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Collapsing {

		final public String field;
		final public Integer max;
		final public ModeEnum mode;
		final public TypeEnum type;
		final public List<FunctionField> functionFields;

		public Collapsing() {
			field = null;
			max = null;
			mode = null;
			type = null;
			functionFields = null;
		}

		public Collapsing(AbstractSearchRequest request) {
			field = request.getCollapseField();
			max = request.getCollapseMax();
			switch (request.getCollapseMode()) {
			default:
			case OFF:
				mode = ModeEnum.OFF;
				break;
			case ADJACENT:
				mode = ModeEnum.ADJACENT;
				break;
			case CLUSTER:
				mode = ModeEnum.CLUSTER;
				break;
			}
			switch (request.getCollapseType()) {
			case FULL:
				type = TypeEnum.FULL;
				break;
			default:
			case OPTIMIZED:
				type = TypeEnum.OPTIMIZED;
				break;
			}
			functionFields = FunctionField.newList(request
					.getCollapseFunctionFields());
		}

		public void apply(AbstractSearchRequest request) {
			if (field != null)
				request.setCollapseField(field);
			if (max != null)
				request.setCollapseMax(max);
			if (mode != null)
				request.setCollapseMode(mode.mode);
			if (type != null)
				request.setCollapseType(type.type);
			if (functionFields != null)
				for (FunctionField functionField : functionFields)
					request.addCollapseFunctionField(functionField
							.getCollapseFunctionField());
		}

		public enum ModeEnum {

			OFF(CollapseParameters.Mode.OFF), ADJACENT(
					CollapseParameters.Mode.ADJACENT), CLUSTER(
					CollapseParameters.Mode.CLUSTER);

			private final CollapseParameters.Mode mode;

			private ModeEnum(CollapseParameters.Mode mode) {
				this.mode = mode;
			}
		}

		public enum TypeEnum {

			FULL(CollapseParameters.Type.FULL), OPTIMIZED(
					CollapseParameters.Type.OPTIMIZED);

			private final CollapseParameters.Type type;

			private TypeEnum(CollapseParameters.Type type) {
				this.type = type;
			}
		}

		public static class FunctionField {
			final public Function function;
			final public String field;

			public FunctionField() {
				function = null;
				field = null;
			}

			private FunctionField(CollapseFunctionField functionField) {
				this.function = functionField.getFunction();
				this.field = functionField.getField();
			}

			private CollapseFunctionField getCollapseFunctionField() {
				return new CollapseFunctionField(function, field);
			}

			private static List<FunctionField> newList(
					Collection<CollapseFunctionField> collapseFunctionFields) {
				if (collapseFunctionFields == null)
					return null;
				List<FunctionField> functionFieldList = new ArrayList<FunctionField>(
						collapseFunctionFields.size());
				for (CollapseFunctionField functionField : collapseFunctionFields)
					functionFieldList.add(new FunctionField(functionField));
				return functionFieldList;
			}
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Geo {

		final public String latitudeField;
		final public String longitudeField;
		final public Double latitude;
		final public Double longitude;
		final public CoordUnit coordUnit;
		final public DistanceReturn distanceReturn;

		public Geo() {
			latitudeField = null;
			longitudeField = null;
			latitude = null;
			longitude = null;
			coordUnit = null;
			distanceReturn = null;
		}

		public Geo(GeoParameters geoParams) {
			latitudeField = geoParams.getLatitudeField();
			longitudeField = geoParams.getLongitudeField();
			latitude = geoParams.getLatitude();
			longitude = geoParams.getLongitude();
			coordUnit = geoParams.getCoordUnit();
			distanceReturn = geoParams.getDistanceReturn();
		}

		private void apply(GeoParameters geoParams) {
			if (latitude != null)
				geoParams.setLatitude(latitude);
			if (longitude != null)
				geoParams.setLongitude(longitude);
			if (latitudeField != null)
				geoParams.setLatitudeField(latitudeField);
			if (longitudeField != null)
				geoParams.setLongitudeField(longitudeField);
			if (coordUnit != null)
				geoParams.setCoordUnit(coordUnit);
			if (distanceReturn != null)
				geoParams.setDistanceReturn(distanceReturn);
		}
	}

	@XmlTransient
	@JsonTypeInfo(use = Id.NAME, property = "type")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = QueryFilter.class, name = "QueryFilter"),
			@JsonSubTypes.Type(value = TermFilter.class, name = "TermFilter"),
			@JsonSubTypes.Type(value = GeoFilter.class, name = "GeoFilter"),
			@JsonSubTypes.Type(value = RelativeDateFilter.class, name = "RelativeDateFilter"),
			@JsonSubTypes.Type(value = MirrorAndFilter.class, name = "MirrorAndFilter"),
			@JsonSubTypes.Type(value = RequestTemplateFilter.class, name = "RequestTemplateFilter") })
	public static abstract class Filter {

		final public Boolean negative;
		final public String type;

		public Filter() {
			negative = null;
			type = getClass().getSimpleName();
		}

		protected Filter(Boolean negative) {
			this.negative = negative;
			type = getClass().getSimpleName();
		}

		protected void apply(FilterAbstract<?> filter) {
			if (negative != null)
				filter.setNegative(negative);
		}

		public abstract FilterAbstract<?> newFilter();

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "QueryFilter")
	@XmlRootElement(name = "QueryFilter")
	@JsonTypeName("QueryFilter")
	public static class QueryFilter extends Filter {

		final public String query;

		public QueryFilter() {
			query = null;
		}

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
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.QueryFilter();
			apply(filter);
			return filter;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "TermFilter")
	@XmlRootElement(name = "TermFilter")
	@JsonTypeName("TermFilter")
	public static class TermFilter extends Filter {

		final public String field;
		final public String term;

		public TermFilter() {
			field = null;
			term = null;
		}

		protected TermFilter(com.jaeksoft.searchlib.filter.TermFilter src) {
			super(src.isNegative());
			this.field = src.getField();
			this.term = src.getTerm();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.TermFilter termFilter = (com.jaeksoft.searchlib.filter.TermFilter) filter;
			if (field != null)
				termFilter.setField(field);
			if (term != null)
				termFilter.setTerm(term);
		}

		@Override
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.TermFilter();
			apply(filter);
			return filter;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "GeoFilter")
	@XmlRootElement(name = "GeoFilter")
	@JsonTypeName("GeoFilter")
	public static class GeoFilter extends Filter {

		final public Unit unit;
		final public Type shape;
		final public Double distance;

		public GeoFilter() {
			unit = null;
			shape = null;
			distance = null;
		}

		public GeoFilter(com.jaeksoft.searchlib.filter.GeoFilter src) {
			super(src.isNegative());
			unit = src.getUnit();
			shape = src.getType();
			distance = src.getMaxDistance();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.GeoFilter geoFilter = (com.jaeksoft.searchlib.filter.GeoFilter) filter;
			if (unit != null)
				geoFilter.setUnit(unit);
			if (shape != null)
				geoFilter.setType(shape);
			if (distance != null)
				geoFilter.setMaxDistance(distance);
		}

		@Override
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.GeoFilter();
			apply(filter);
			return filter;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "RelativeDateFilter")
	@XmlRootElement(name = "RelativeDateFilter")
	@JsonTypeName("RelativeDateFilter")
	public static class RelativeDateFilter extends Filter {

		final public TimeInterval from;
		final public TimeInterval to;
		final public String field;
		final public String dateFormat;

		public RelativeDateFilter() {
			from = null;
			to = null;
			field = null;
			dateFormat = null;
		}

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
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.RelativeDateFilter();
			apply(filter);
			return filter;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class TimeInterval {

		final public IntervalUnit unit;
		final public Long interval;

		public TimeInterval() {
			unit = null;
			interval = null;
		}

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

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "MirrorAndFilter")
	@XmlRootElement(name = "MirrorAndFilter")
	@JsonTypeName("MirrorAndFilter")
	public static class MirrorAndFilter extends Filter {

		public MirrorAndFilter() {
		}

		protected MirrorAndFilter(
				com.jaeksoft.searchlib.filter.MirrorAndFilter src) {
			super(src.isNegative());
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
		}

		@Override
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.MirrorAndFilter();
			apply(filter);
			return filter;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "RequestTemplateFilter")
	@XmlRootElement(name = "RequestTemplateFilter")
	@JsonTypeName("RequestTemplateFilter")
	public static class RequestTemplateFilter extends Filter {

		final public String requestName;
		final public String queryString;

		public RequestTemplateFilter() {
			requestName = null;
			queryString = null;
		}

		protected RequestTemplateFilter(
				com.jaeksoft.searchlib.filter.RequestTemplateFilter src) {
			super(src.isNegative());
			this.requestName = src.getRequestName();
			this.queryString = src.getQueryString();
		}

		@Override
		protected void apply(FilterAbstract<?> filter) {
			super.apply(filter);
			com.jaeksoft.searchlib.filter.RequestTemplateFilter requestFilter = (com.jaeksoft.searchlib.filter.RequestTemplateFilter) filter;
			if (requestName != null)
				requestFilter.setRequestName(requestName);
			if (queryString != null)
				requestFilter.setQueryString(queryString);
		}

		@Override
		@JsonIgnore
		public FilterAbstract<?> newFilter() {
			FilterAbstract<?> filter = new com.jaeksoft.searchlib.filter.RequestTemplateFilter();
			apply(filter);
			return filter;
		}

	}

	public static List<Filter> newFilterList(FilterList filterList) {
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
			case TERM_FILTER:
				filters.add(new TermFilter(
						(com.jaeksoft.searchlib.filter.TermFilter) filterAbstract));
				break;
			case GEO_FILTER:
				filters.add(new GeoFilter(
						(com.jaeksoft.searchlib.filter.GeoFilter) filterAbstract));
				break;
			case RELATIVE_DATE_FILTER:
				filters.add(new RelativeDateFilter(
						(com.jaeksoft.searchlib.filter.RelativeDateFilter) filterAbstract));
				break;
			case MIRROR_AND_FILTER:
				filters.add(new MirrorAndFilter(
						(com.jaeksoft.searchlib.filter.MirrorAndFilter) filterAbstract));
				break;
			case REQUEST_TEMPLATE_FILTER:
				filters.add(new RequestTemplateFilter(
						(com.jaeksoft.searchlib.filter.RequestTemplateFilter) filterAbstract));
				break;
			}
		}
		return filters;
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Sort {

		public static enum Direction {
			ASC, DESC;
		}

		public static enum Empty {
			last, first;
		}

		final public Integer joinNumber;
		final public String field;
		final public Direction direction;
		final public Empty empty;

		public Sort() {
			joinNumber = null;
			field = null;
			direction = null;
			empty = null;
		}

		public Sort(SortField sortField) {
			joinNumber = sortField.getJoinNumber() == 0 ? null : sortField
					.getJoinNumber();
			field = sortField.getName();
			direction = sortField.isDesc() ? Direction.DESC : Direction.ASC;
			empty = sortField.isNullFirst() ? Empty.first : Empty.last;
		}

		@JsonIgnore
		final private SortField newSortField() {
			SortField sortField = new SortField(joinNumber == null ? 0
					: joinNumber, field, false, false);
			if (direction != null)
				sortField.setDirection(direction.name());
			if (empty != null)
				sortField.setNullFirst(empty == Empty.first);
			return sortField;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Scoring {

		public static enum Type {
			DISTANCE, FIELD_ORDER;
		}

		final public Boolean ascending;
		final public String fieldName;
		final public Double weight;
		final public Type type;

		public Scoring() {
			ascending = null;
			fieldName = null;
			weight = null;
			type = null;
		}

		public Scoring(AdvancedScoreItem scoreItem) {
			ascending = scoreItem.isAscending();
			fieldName = scoreItem.getFieldName();
			weight = scoreItem.getWeight();
			type = scoreItem.getType();
		}

		@JsonIgnore
		final private AdvancedScoreItem newAdvancedScoreItem() {
			return new AdvancedScoreItem(this);
		}
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class BoostingQuery {

		final public String patternQuery;
		final public Float boost;

		public BoostingQuery() {
			patternQuery = null;
			boost = null;
		}

		public BoostingQuery(BoostQuery boostQuery) {
			patternQuery = boostQuery.getQuery();
			boost = boostQuery.getBoost();
		}

		public BoostQuery newBoostQuery() {
			return new BoostQuery(patternQuery, boost == null ? 1.0F : boost);
		}
	}

	final private static List<Scoring> newScoringList(
			final AdvancedScore advancedScore) {
		if (advancedScore == null)
			return null;
		AdvancedScoreItem[] advancedScoreItems = advancedScore.getArray();
		if (advancedScoreItems == null)
			return null;
		List<Scoring> scorings = new ArrayList<Scoring>(
				advancedScoreItems.length);
		for (AdvancedScoreItem advancedScoreItem : advancedScoreItems)
			scorings.add(new Scoring(advancedScoreItem));
		return scorings;
	}

	final private static List<BoostingQuery> newBoostingQueryList(
			final BoostQuery[] boostQueries) {
		if (boostQueries == null)
			return null;
		List<BoostingQuery> boostingQueries = new ArrayList<BoostingQuery>(
				boostQueries.length);
		for (BoostQuery boostQuery : boostQueries)
			boostingQueries.add(new BoostingQuery(boostQuery));
		return boostingQueries;
	}

	final private static List<Sort> newSortList(
			final SortFieldList sortFieldList) {
		if (sortFieldList == null)
			return null;
		if (sortFieldList.size() == 0)
			return null;
		List<Sort> sorts = new ArrayList<Sort>(sortFieldList.size());
		for (SortField sortField : sortFieldList)
			sorts.add(new Sort(sortField));
		return sorts;
	}

	final private static List<String> newReturnFieldList(
			final ReturnFieldList returnFieldList) {
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
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Snippet {

		final public String field;
		final public String tag;
		final public String separator;
		final public Integer maxSize;
		final public Integer maxNumber;
		final public FragmenterEnum fragmenter;

		public Snippet() {
			field = null;
			tag = null;
			separator = null;
			maxSize = null;
			maxNumber = null;
			fragmenter = null;
		}

		public Snippet(SnippetField snippetField) {
			field = snippetField.getName();
			tag = snippetField.getTag();
			separator = snippetField.getSeparator();
			maxSize = snippetField.getMaxSnippetSize();
			maxNumber = snippetField.getMaxSnippetNumber();
			fragmenter = FragmenterEnum.find(snippetField.getFragmenter());
		}

		@JsonIgnore
		protected SnippetField newSnippetField() throws SearchLibException {
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

		public final String className;

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
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Facet {

		public static enum OrderByEnum {
			no_sort, term_asc, term_desc, count_asc, count_desc;
		}

		final public String field;
		final public Integer minCount;
		final public Boolean multivalued;
		final public Boolean postCollapsing;
		final public Integer limit;
		final public OrderByEnum orderBy;

		public Facet() {
			field = null;
			minCount = null;
			multivalued = null;
			postCollapsing = null;
			limit = null;
			orderBy = null;
		}

		public Facet(FacetField facetField) {
			field = facetField.getName();
			minCount = facetField.getMinCount();
			multivalued = facetField.isCheckMultivalued();
			postCollapsing = facetField.isCheckPostCollapsing();
			limit = facetField.getLimit();
			orderBy = facetField.getOrderBy();
		}

		@JsonIgnore
		protected FacetField newFacetField() {
			FacetField facetField = new FacetField();
			if (field != null)
				facetField.setName(field);
			if (minCount != null)
				facetField.setMinCount(minCount);
			if (multivalued != null)
				facetField.setMultivalued(multivalued);
			if (postCollapsing != null)
				facetField.setPostCollapsing(postCollapsing);
			if (limit != null)
				facetField.setLimit(limit);
			if (orderBy != null)
				facetField.setOrderBy(orderBy);
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
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Join {

		final public String indexName;
		final public String queryTemplate;
		final public String queryString;
		final public String localField;
		final public String foreignField;
		final public JoinType type;
		final public Boolean returnFields;
		final public Boolean returnScores;
		final public Boolean returnFacets;

		public Join() {
			indexName = null;
			queryTemplate = null;
			queryString = null;
			localField = null;
			foreignField = null;
			type = JoinType.INNER;
			returnFields = null;
			returnScores = null;
			returnFacets = null;
		}

		public Join(JoinItem joinItem) {
			indexName = joinItem.getIndexName();
			queryTemplate = joinItem.getQueryTemplate();
			queryString = joinItem.getQueryString();
			localField = joinItem.getLocalField();
			foreignField = joinItem.getForeignField();
			type = joinItem.getType();
			returnFields = joinItem.isReturnFields();
			returnScores = joinItem.isReturnScores();
			returnFacets = joinItem.isReturnFacets();
		}

		@JsonIgnore
		protected JoinItem newJoinItem() {
			JoinItem joinItem = new JoinItem();
			if (indexName != null)
				joinItem.setIndexName(indexName);
			if (queryTemplate != null)
				joinItem.setQueryTemplate(queryTemplate);
			if (queryString != null)
				joinItem.setQueryString(queryString);
			if (localField != null)
				joinItem.setLocalField(localField);
			if (type != null)
				joinItem.setType(type);
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

	final private static List<Join> newJoinList(final JoinList joinList) {
		if (joinList == null)
			return null;
		if (joinList.size() == 0)
			return null;
		List<Join> joins = new ArrayList<Join>(joinList.size());
		for (JoinItem joinItem : joinList)
			joins.add(new Join(joinItem));
		return joins;
	}

	final private static List<String> newLogList(final List<String> customLogs) {
		if (customLogs == null)
			return null;
		if (customLogs.size() == 0)
			return null;
		return new ArrayList<String>(customLogs);
	}

	public SearchQueryAbstract(final AbstractSearchRequest request) {
		query = request.getQueryString();
		emptyReturnsAll = request.getEmptyReturnsAll();
		start = request.getStart();
		rows = request.getRows();
		lang = request.getLang();
		operator = request.getDefaultOperator() == null ? null : OperatorEnum
				.valueOf(request.getDefaultOperator());
		collapsing = new Collapsing(request);
		geo = new Geo(request.getGeoParameters());
		filterOperator = request.getFilterList().getDefaultOperator() == null ? null
				: OperatorEnum.valueOf(request.getFilterList()
						.getDefaultOperatorString());
		filters = newFilterList(request.getFilterList());
		sorts = newSortList(request.getSortFieldList());
		returnedFields = newReturnFieldList(request.getReturnFieldList());
		snippets = newSnippetList(request.getSnippetFieldList());
		facets = newFacetList(request.getFacetFieldList());
		joins = newJoinList(request.getJoinList());
		scorings = newScoringList(request.getAdvancedScore());
		boostingQueries = newBoostingQueryList(request.getBoostingQueries());
		enableLog = request.isLogReport();
		customLogs = newLogList(request.getCustomLogs());
		batchAction = null;
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

	@Override
	protected void apply(AbstractRequest req) {
		try {
			super.apply(req);
			AbstractSearchRequest request = (AbstractSearchRequest) req;
			if (query != null)
				request.setQueryString(query);
			request.setEmptyReturnsAll(emptyReturnsAll);
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
			if (geo != null)
				geo.apply(request.getGeoParameters());
			if (filterOperator != null)
				request.getFilterList().setDefaultOperator(filterOperator);
			if (filters != null)
				for (Filter filter : filters)
					request.getFilterList().add(filter.newFilter());
			if (sorts != null)
				for (Sort sort : sorts)
					request.getSortFieldList().put(sort.newSortField());
			if (returnedFields != null)
				for (String returnedField : returnedFields)
					request.getReturnFieldList().put(
							new ReturnField(returnedField));
			if (snippets != null)
				for (Snippet snippet : snippets)
					request.getSnippetFieldList()
							.put(snippet.newSnippetField());
			if (facets != null)
				for (Facet facet : facets)
					request.getFacetFieldList().put(facet.newFacetField());
			if (joins != null)
				for (Join join : joins)
					request.getJoinList().add(join.newJoinItem());
			if (scorings != null)
				for (Scoring scoring : scorings)
					request.addAdvancedScore(scoring.newAdvancedScoreItem());
			if (boostingQueries != null)
				for (BoostingQuery boostingQuery : boostingQueries)
					request.setBoostingQuery(null,
							boostingQuery.newBoostQuery());
			if (enableLog != null)
				request.setLogReport(enableLog);
			if (customLogs != null)
				for (String customLog : customLogs)
					request.getCustomLogs().add(customLog);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}
}
