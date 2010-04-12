/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis;

import java.util.List;

import org.apache.lucene.util.AttributeImpl;

public class SynonymAttributeImpl extends AttributeImpl implements
		SynonymAttribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3291684513073371401L;

	private SynonymQueues synonymsQueues;
	private List<String> synonyms;
	private int index;

	@Override
	public void clear() {
		synonymsQueues = null;
		synonyms = null;
		index = 0;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		SynonymAttribute t = (SynonymAttribute) target;
		t.setSynonymQueues(synonymsQueues);
		t.setSynonyms(synonyms);
		t.setIndex(index);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;

		if (!(other instanceof SynonymAttributeImpl))
			return false;
		SynonymAttributeImpl o = (SynonymAttributeImpl) other;
		return o.synonymsQueues == synonymsQueues && o.synonyms == synonyms
				&& o.index == index;
	}

	@Override
	public int hashCode() {
		return this.hashCode();
	}

	public void setSynonymQueues(SynonymQueues synonymsQueues) {
		this.synonymsQueues = synonymsQueues;
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<String> getSynonyms() {
		return this.synonyms;
	}

	public int incIndex() {
		return index++;
	}

	public SynonymQueues getSynonymQueues() {
		return this.synonymsQueues;
	}

	public void resetSynonyms() {
		this.synonyms = null;
		this.index = 0;
	}

	public void checkSynonymQueue(SynonymMap synonymMap) {
		if (synonymsQueues != null)
			return;
		setSynonymQueues(synonymMap.getSynonymQueues());
	}

}
