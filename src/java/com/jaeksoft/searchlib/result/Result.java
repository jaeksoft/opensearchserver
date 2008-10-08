/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.collapse.Collapse;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.util.Timer;

public abstract class Result<T extends Collapse<?>> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	transient protected Request request;
	protected DocumentResult documentResult;
	protected FacetList facetList;
	protected T collapse;
	private Timer timer;

	protected Result(Request request) {
		this.documentResult = null;
		this.request = request;
		this.timer = new Timer();
		if (request.getFacetFieldList().size() > 0)
			this.facetList = new FacetList();
		this.collapse = null;
	}

	public Request getRequest() {
		return this.request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Timer getTimer() {
		return this.timer;
	}

	public FacetList getFacetList() {
		return this.facetList;
	}

	public abstract DocumentResult documents() throws IOException,
			ParseException;

	public DocumentRequestItem document(int pos) throws CorruptIndexException,
			IOException, ParseException {
		if (request.isDelete())
			return null;
		if (documentResult == null)
			documentResult = documents();
		return documentResult.get(pos - request.getStart());
	}

	public abstract float getMaxScore();

	public abstract int getNumFound();

	public abstract float getScore(int pos);

	public abstract int[] getFetchedDoc();

	public abstract int[] getDocs();

	public Collapse<?> getCollapse() throws IOException {
		return this.collapse;
	}

	public void collapse() throws IOException {
		if (this.collapse != null)
			this.collapse.run();
	}

	@Override
	public String toString() {
		return "Found: " + this.getNumFound() + " maxScore: "
				+ this.getMaxScore();
	}

}
