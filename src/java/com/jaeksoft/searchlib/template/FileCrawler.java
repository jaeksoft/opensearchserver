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

package com.jaeksoft.searchlib.template;

import java.io.File;

public class FileCrawler extends TemplateAbstract {

	public final static String publicName = "file crawler";

	public final static String description = "This is an index with predefined fields, "
			+ " analysers and parsers. "
			+ "This template is suited to parsing and indexing files (.doc, .pdf, etc.)";

	public final static String root = "file_crawler";

	public final static String[] resources = { "config.xml", "parsers.xml",
			"requests.xml", "filecrawler-mapping.xml",
			"renderers" + File.separator + "default.xml" };

	protected FileCrawler() {
		super(root, resources, publicName, description);
	}

}
