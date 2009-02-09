package com.jaeksoft.searchlib.facet;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.ResultSingle;

public class FacetMultivalued extends FacetSingle {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3251943561731498334L;

	public FacetMultivalued(ResultSingle result, FacetField facetField)
			throws IOException {
		super(facetField);
		ReaderLocal reader = result.getReader();
		StringIndex stringIndex = reader.getStringIndex(facetField.getName());
		int[] count = new int[stringIndex.lookup.length];
		String fieldName = facetField.getName();
		DocSetHits dsh = result.getDocSetHits();
		int i = 0;
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = new Term(fieldName, term);
				TermDocs termDocs = reader.getTermDocs(t);
				while (termDocs.next())
					if (termDocs.freq() > 0)
						if (dsh.contains(termDocs.doc()))
							count[i]++;
			}
			i++;
		}
		setResult(new FacetCount(stringIndex.lookup, count, facetField
				.getMinCount()));
	}
}
