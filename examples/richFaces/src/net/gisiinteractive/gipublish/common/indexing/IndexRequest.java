package net.gisiinteractive.gipublish.common.indexing;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;

public abstract class IndexRequest {

	private SearchRequest request;
	private StringBuffer query;

	protected IndexRequest(Client client, String name, IndexDocument document)
			throws ParseException {
		request = client.getNewSearchRequest(name);
		request.setWithDocument(true);
		query = new StringBuffer();
	}

	protected void addFilter(String filterQuery) throws ParseException {
		request.getFilterList().add(filterQuery, Filter.Source.REQUEST);
	}

	protected void addFilter(FieldContent fieldContent, boolean tokenized)
			throws ParseException {
		if (fieldContent == null)
			return;
		String field = fieldContent.getField();
		for (String value : fieldContent.getValues()) {
			if (value == null || value.length() == 0)
				continue;
			if (!tokenized)
				value = '"' + value + '"';
			addFilter(field + ":" + value);
		}
	}

	protected void addRange(FieldContent fieldContent) throws ParseException {
		if (fieldContent == null)
			return;
		String field = fieldContent.getField();
		for (String value : fieldContent.getValues()) {
			if (value == null || value.length() == 0)
				continue;

			value.trim();
			value = value.replace("-", " TO ");
			value = '[' + value + ']';
			addFilter(field + ":" + value);
		}
	}

	protected void addRangeTime(FieldContent fieldContent)
			throws ParseException {
		if (fieldContent == null)
			return;
		String field = fieldContent.getField();
		// the editorialDate is formated like : IndexingFormatter.DATEFORMAT :
		// "yyyyMMdd-HH:mm".
		for (String value : fieldContent.getValues()) {
			if (value == null || value.length() == 0)
				continue;

			value = value.trim();
			StringTokenizer st = new StringTokenizer(value, "-");

			String date = null;
			if (st.hasMoreTokens()) {
				date = st.nextToken();
			}

			String time = null;
			if (st.hasMoreTokens()) {
				time = st.nextToken();
			}

			if (date != null && time != null) {
				value = value + " TO " + date + "-23:59";
				value = '[' + value + ']';
				addFilter(field + ":" + value);
			}
		}
	}

	protected void addSort(String fieldName, boolean desc) {
		request.addSort(fieldName, desc);
	}

	protected void addQuery(String q) {
		if (query.length() != 0)
			query.append(" ");
		query.append(q);
	}

	protected void setDelete(boolean delete) {
		request.setDelete(delete);
	}

	protected enum Operator {
		OR, AND;
	}

	protected void addQuery(String field, String content, Operator operator) {
		if (content == null)
			return;
		if (content.length() == 0)
			return;
		if (query.length() != 0) {
			if (operator == Operator.OR)
				query.append(" OR ");
			else if (operator == Operator.AND)
				query.append(" AND ");
			else
				query.append(" ");
		}
		query.append(field);
		query.append(":(");
		query.append(content);
		query.append(")");

	}

	protected void addQuery(String field, List<String> content,
			Operator operator) {
		if (content == null)
			return;
		StringBuffer values = new StringBuffer();
		for (String value : content) {
			if (value == null || value.length() == 0)
				continue;
			if (values.length() != 0)
				values.append(" ");
			values.append(value);
		}
		addQuery(field, values.toString(), operator);
	}

	protected void addQuery(FieldContent fieldContent, Operator operator) {
		if (fieldContent == null)
			return;
		addQuery(fieldContent.getField(), fieldContent.getValues(), operator);
	}

	protected void addStrictQuery(String field, List<String> content,
			Operator operator) {

		if (content == null)
			return;

		if (content.isEmpty())
			return;

		boolean first = true;

		for (String value : content) {
			if (!first) {
				if (operator == Operator.OR)
					query.append(" OR ");
				else if (operator == Operator.AND)
					query.append(" AND ");
				else
					query.append(" ");
			} else
				first = false;
			query.append(field).append(":(").append(value).append(")");
		}
	}

	// Fuzzy

	protected void addFuzzy(String field, String content) {
		if (content == null)
			return;
		if (content.length() == 0)
			return;
		if (query.length() != 0)
			query.append(" OR ");

		query.append(field);
		query.append(":(");
		query.append(content);
		query.append("*)");

	}

	protected void addFuzzy(String field, List<String> content) {
		if (content == null)
			return;
		StringBuffer values = new StringBuffer();
		for (String value : content) {
			if (value == null || value.length() == 0)
				continue;
			if (values.length() != 0)
				values.append(" ");
			values.append(value);
		}
		addFuzzy(field, values.toString());
	}

	protected void addFuzzy(FieldContent fieldContent, Operator operator) {
		if (fieldContent == null)
			return;
		addFuzzy(fieldContent.getField(), fieldContent.getValues());
	}

	protected void addFuzzy(String field, String content, Operator operator) {
		if (content == null)
			return;
		if (content.length() == 0)
			return;
		if (query.length() != 0) {
			if (operator == Operator.OR)
				query.append(" OR ");
			else if (operator == Operator.AND)
				query.append(" AND ");
			else
				query.append(" ");
		}
		query.append(field);
		query.append(":(");
		query.append(content);
		query.append("*)");

	}

	public SearchRequest getRequest(int rows) throws CorruptIndexException,
			IOException, ParseException {
		if (query.length() == 0)
			request.setQueryString("*:*");
		else
			request.setQueryString(query.toString());
		request.setRows(rows);
		return request;
	}

	public void setStart(int start) {
		request.setStart(start);
	}
}
