/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2011-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Schema;

import java.util.Collection;

public interface UpdateInterfaces {

	interface Before extends UpdateInterfaces {

		void update(final Schema schema, final IndexDocument document) throws SearchLibException;
	}

	interface After extends UpdateInterfaces {

		void update(final IndexDocument document) throws SearchLibException;

		void update(final Collection<IndexDocument> documents) throws SearchLibException;
	}

	interface Delete extends UpdateInterfaces {

		void delete(final String field, final String value) throws SearchLibException;

		void delete(final String field, final Collection<String> values) throws SearchLibException;
	}

	interface Reload extends UpdateInterfaces {

		void reload() throws SearchLibException;
	}
}
