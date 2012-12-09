/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.sun.jna.Pointer;

public class ReaderNativeOSSE extends ReaderAbstract {

	private Pointer index;

	protected ReaderNativeOSSE(File configDir, IndexConfig indexConfig) {
		super(indexConfig);
		index = OsseLibrary.INSTANCE.index_new();
	}

	@Override
	public void close() {
		OsseLibrary.INSTANCE.index_delete(index);
	}

	protected Pointer getIndex() {
		return index;
	}

	@Override
	public int getDocFreq(Term term) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TermEnum getTermEnum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermEnum getTermEnum(String field, String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query rewrite(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
