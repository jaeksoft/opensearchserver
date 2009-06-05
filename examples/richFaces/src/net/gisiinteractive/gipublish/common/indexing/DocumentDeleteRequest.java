package net.gisiinteractive.gipublish.common.indexing;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;

public class DocumentDeleteRequest extends IndexRequest {

	public DocumentDeleteRequest(Client client, IndexDocument document,
			String requestName) throws ParseException, SearchLibException {
		super(client, requestName, document);
		addFilter(document.getField("id"), false);
		setDelete(true);
	}

	public DocumentDeleteRequest(Client client, IndexDocument document,
			String requestName, boolean all) throws ParseException,
			SearchLibException {
		super(client, requestName, document);

		if (!all)
			addFilter(document.getField("id"), false);
		setDelete(true);
	}

}
