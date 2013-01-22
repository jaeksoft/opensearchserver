/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice;

import com.jaeksoft.searchlib.webservice.action.ActionImpl;
import com.jaeksoft.searchlib.webservice.crawler.filecrawler.FileCrawlerImpl;
import com.jaeksoft.searchlib.webservice.crawler.filecrawler.FilePatternImpl;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.URLBrowserImpl;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.WebCrawlerImpl;
import com.jaeksoft.searchlib.webservice.crawler.webcrawler.WebPatternImpl;
import com.jaeksoft.searchlib.webservice.database.DatabaseImpl;
import com.jaeksoft.searchlib.webservice.delete.DeleteImpl;
import com.jaeksoft.searchlib.webservice.monitor.MonitorImpl;
import com.jaeksoft.searchlib.webservice.schema.SchemaImpl;
import com.jaeksoft.searchlib.webservice.screenshot.ScreenshotImpl;
import com.jaeksoft.searchlib.webservice.select.SelectImpl;
import com.jaeksoft.searchlib.webservice.update.UpdateImpl;

public enum WebServiceEnum {

	Action(ActionImpl.class, "/Action"),

	Database(DatabaseImpl.class, "/Database"),

	Delete(DeleteImpl.class, "/Delete"),

	FileCrawler(FileCrawlerImpl.class, "/FileCrawler"),

	FilePattern(FilePatternImpl.class, "/FilePattern"),

	Monitor(MonitorImpl.class, "/Monitor"),

	Shema(SchemaImpl.class, "/Schema"),

	Screenshot(ScreenshotImpl.class, "/Screenshot"),

	Select(SelectImpl.class, "/Select"),

	Update(UpdateImpl.class, "/Update"),

	URLBrowser(URLBrowserImpl.class, "/URLBrowser"),

	WebCrawler(WebCrawlerImpl.class, "/WebCrawler"),

	WebPattern(WebPatternImpl.class, "/WebPattern");

	final private Class<?> serviceClass;

	final private String defaultPath;

	private WebServiceEnum(Class<?> serviceClass, String defaultPath) {
		this.serviceClass = serviceClass;
		this.defaultPath = defaultPath;
	}

	final public Object getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return serviceClass.newInstance();
	}

	final public Class<?> getServiceClass() {
		return serviceClass;
	}

	final public String getDefaultPath() {
		return defaultPath;
	}
}
