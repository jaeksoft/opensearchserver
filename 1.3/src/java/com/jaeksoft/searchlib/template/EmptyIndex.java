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

public class EmptyIndex extends TemplateAbstract {

	public final static String root = "empty_index";

	public final static String[] resources = {

	"config.xml", "parsers.xml", "jobs.xml",

	"renderers" + '/' + "default.xml",

	"stopwords" + '/' + "English stop words",

	"stopwords" + '/' + "French stop words",

	"stopwords" + '/' + "German stop words",

	};

	public final static String publicName = "Empty index";

	public final static String description = "This is an empty index - "
			+ "if you use this setting you will have to set a schema up "
			+ "(fields, analysers, parser mapping) before "
			+ "it can be populated with data.";

	protected EmptyIndex() {
		super(root, resources, publicName, description);
	}

}
