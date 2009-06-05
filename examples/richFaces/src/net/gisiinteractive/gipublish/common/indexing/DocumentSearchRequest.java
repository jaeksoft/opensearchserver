package net.gisiinteractive.gipublish.common.indexing;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.index.IndexDocument;

public class DocumentSearchRequest extends IndexRequest {

	public DocumentSearchRequest(Client client, IndexDocument document,
			String searcher, String textSearch) throws ParseException {
		super(client, searcher, document);

		if (document != null)
			addFilter(document.getField("id"), false);

		if (textSearch != null) {
			addQuery("description", textSearch, Operator.OR);
			addQuery("title", textSearch, Operator.OR);
		}

		if (document != null)
			addRangeTime(document.getField("date"));

		addSort("id", true);

	}
}
