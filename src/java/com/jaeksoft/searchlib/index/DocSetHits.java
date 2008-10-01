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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import com.jaeksoft.searchlib.util.XmlInfo;

public class DocSetHits implements XmlInfo, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1343588321039421631L;

	transient private int[] collectedDocs;
	transient private Hits hits;
	transient private ReaderLocal reader;
	private int[] sortFetchDocs;
	private float[] sortScore;
	private int docNumFound;
	private float maxScore;

	private class ScoreHitCollector extends HitCollector {
		@Override
		public void collect(int docId, float sc) {
			collectedDocs[docNumFound++] = docId;
			if (sc > maxScore)
				maxScore = sc;
		}
	}

	private class DeleteHitCollector extends ScoreHitCollector {
		@Override
		public void collect(int docId, float sc) {
			super.collect(docId, sc);
			try {
				reader.deleteDocument(docId);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected DocSetHits(ReaderLocal reader, Query query, Filter filter,
			Sort sort, boolean delete) throws IOException {
		this.docNumFound = 0;
		this.maxScore = 0;
		this.reader = reader;
		HitCollector hc;
		if (delete)
			hc = new DeleteHitCollector();
		else
			hc = new ScoreHitCollector();
		this.sortFetchDocs = new int[0];
		this.sortScore = new float[0];
		this.hits = reader.search(query, filter, sort);
		this.collectedDocs = new int[this.hits.length()];
		reader.search(query, filter, hc);
	}

	public void getHits(int rows) throws IOException {
		synchronized (this) {
			if (rows > this.hits.length())
				rows = this.hits.length();
			if (rows <= this.sortFetchDocs.length)
				return;
			int[] newDoc = new int[rows];
			float[] newScore = new float[rows];
			int newDocPos = 0;
			for (int id : this.sortFetchDocs) {
				newDoc[newDocPos] = id;
				newScore[newDocPos] = this.sortScore[newDocPos++];
			}
			int hitsPos = this.sortFetchDocs.length;
			while (newDocPos < rows) {
				newDoc[newDocPos] = hits.id(hitsPos);
				newScore[newDocPos++] = hits.score(hitsPos++) * this.maxScore;
			}
			this.sortFetchDocs = newDoc;
			this.sortScore = newScore;
		}
	}

	public int[] getSortFetchDocs() {
		synchronized (this) {
			return this.sortFetchDocs;
		}
	}

	public boolean contains(int docId) {
		for (int id : collectedDocs)
			if (id == docId)
				return true;
		return false;
	}

	public int getDocId(int pos) {
		synchronized (this) {
			return this.sortFetchDocs[pos];
		}
	}

	public float getScore(int pos) {
		synchronized (this) {
			return this.sortScore[pos];
		}
	}

	public int[] getCollectedDocs() {
		synchronized (this) {
			return this.collectedDocs;
		}
	}

	public float getMaxScore() {
		synchronized (this) {
			return this.maxScore;
		}
	}

	public int getDocNumFound() {
		synchronized (this) {
			return this.docNumFound;
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<docSetHits docFound=\"" + this.docNumFound
				+ "\" fetchedDoc=\"" + this.sortFetchDocs.length
				+ "\" maxScore=\"" + this.maxScore + "\"/>");
	}

}
