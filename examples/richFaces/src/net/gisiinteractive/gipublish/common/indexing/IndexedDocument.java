package net.gisiinteractive.gipublish.common.indexing;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

/**
 * Common methods for indexing
 * 
 * @author zhamdi
 * 
 * @param <T>
 *            the indexed document type
 */
public interface IndexedDocument<T> {
	/**
	 * Creates a {@link IndexDocument} representation of <code>document</code>
	 * with the interested in fields data so it is ready to insert/update in the
	 * index
	 * 
	 * @param document
	 * @return
	 */
	IndexDocument createIndexingDocument(T document);

	/**
	 * Resurrects a document from its indexed data
	 * 
	 * @param idxDoc
	 * @return
	 */
	T createIndexedDocument(ResultDocument idxDoc);
}
