/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.jopendocument.model.OpenDocument;
import org.jopendocument.model.office.OfficeBody;

public class OdtParser extends OOParser {

	public OdtParser() {
		super();
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		// Parse meta
		super.parseContent(inputStream);

		OpenDocument doc = new OpenDocument();
		doc.loadFrom("template/invoice.ods");

		OfficeBody body = doc.getBody();

		/*
		 * Document doc = pkg.getContent().getDocument(); Iterator<Object>
		 * descendants = doc.getDescendants(); while (descendants.hasNext()) {
		 * 
		 * Text descendant = (Text) descendants.next();
		 * System.out.println("ele " + descendant.getTextNormalize()); }
		 */
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

}
