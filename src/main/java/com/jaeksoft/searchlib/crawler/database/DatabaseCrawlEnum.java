/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

public enum DatabaseCrawlEnum {

	DB_SQL("JDBC SQL", "SQL column",
			"/WEB-INF/zul/crawler/database/crawl_edit_sql.zul"),

	DB_MONGO_DB("MongoDB", "Query",
			"/WEB-INF/zul/crawler/database/crawl_edit_mongodb.zul");

	private final String label;
	private final String fieldMapColumnName;
	private final String generalTemplate;

	private DatabaseCrawlEnum(String label, String fieldMapColumnName,
			String generalTemplate) {
		this.label = label;
		this.fieldMapColumnName = fieldMapColumnName;
		this.generalTemplate = generalTemplate;
	}

	public String getLabel() {
		return label;
	}

	public String getGeneralTemplate() {
		return generalTemplate;
	}

	public String getFieldMapColumnName() {
		return fieldMapColumnName;
	}

	public static DatabaseCrawlEnum find(String name) {
		for (DatabaseCrawlEnum type : values())
			if (type.name().equalsIgnoreCase(name))
				return type;
		return DB_SQL;
	}
}
