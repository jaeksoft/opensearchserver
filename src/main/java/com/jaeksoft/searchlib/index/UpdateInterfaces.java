/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.Collection;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Schema;

public interface UpdateInterfaces {

	public static interface Before extends UpdateInterfaces {

		public void update(final Schema schema, final IndexDocument document)
				throws SearchLibException;
	}

	public static interface After extends UpdateInterfaces {

		public void update(final IndexDocument document)
				throws SearchLibException;

		public void update(final Collection<IndexDocument> documents)
				throws SearchLibException;
	}

	public static interface Delete extends UpdateInterfaces {

		public void delete(final String field, final String value)
				throws SearchLibException, IOException;

		public void delete(final String field, final Collection<String> values)
				throws SearchLibException, IOException;
	}
}
