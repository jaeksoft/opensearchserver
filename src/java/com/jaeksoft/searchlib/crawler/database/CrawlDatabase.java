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

package com.jaeksoft.searchlib.crawler.database;

import java.io.File;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManager;
import com.jaeksoft.searchlib.crawler.database.url.UrlManager;
import com.jaeksoft.searchlib.util.XPathParser;

public abstract class CrawlDatabase {

	public static CrawlDatabase fromXmlConfig(Node node, File homeDir)
			throws CrawlDatabaseException {

		String databaseDriver = XPathParser.getAttributeString(node, "driver");
		if (databaseDriver != null) {
			// Open or create a JDBC database
			String databaseUrl = XPathParser.getAttributeString(node, "url");
			if (homeDir != null)
				databaseUrl = databaseUrl.replace("${root}", homeDir
						.getAbsolutePath());
			return new CrawlDatabaseJdbc(databaseDriver, databaseUrl);
		}

		// Open or create a BDB database
		String databasePath = XPathParser.getAttributeString(node, "path");
		if (homeDir != null)
			databasePath = databasePath.replace("${root}", homeDir
					.getAbsolutePath());
		return new CrawlDatabaseBdb(new File(databasePath));
	}

	public abstract UrlManager getUrlManager() throws CrawlDatabaseException;

	public abstract PatternUrlManager getPatternUrlManager()
			throws CrawlDatabaseException;

	public abstract PropertyManager getPropertyManager()
			throws CrawlDatabaseException;

}
