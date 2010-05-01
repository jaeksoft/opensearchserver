/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.plugin;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.jaeksoft.searchlib.index.IndexDocument;

public class IndexPluginBase implements IndexPluginInterface {

	final private static Logger logger = Logger.getLogger(IndexPluginBase.class
			.getCanonicalName());

	public IndexPluginBase() {
		logger.info("INIT");
	}

	public void setProperties(Properties properties) {
		for (Map.Entry<Object, Object> entry : properties.entrySet())
			logger.info("PROPERTY " + entry.getKey() + " VALUE: "
					+ entry.getValue());
	}

	public boolean run(IndexDocument indexDocument) {
		logger.info("RUN");
		return true;
	}

}
