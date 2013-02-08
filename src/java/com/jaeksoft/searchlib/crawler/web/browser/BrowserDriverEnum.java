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

package com.jaeksoft.searchlib.crawler.web.browser;

public enum BrowserDriverEnum {

	CHROME(ChromeBrowserDriver.class, "Chrome"),

	FIREFOX(FirefoxBrowserDriver.class, "Firefox"),

	HTML_UNIT(HtmlUnitBrowserDriver.class, "HtmlUnit"),

	INTERNET_EXPLORER(InternetExplorerBrowserDriver.class, "Internet Exlorer"),

	SAFARI(SafariBrowserDriver.class, "Safari");

	private final Class<? extends BrowserDriver<?>> driverClass;

	private final String name;

	private BrowserDriverEnum(Class<? extends BrowserDriver<?>> driverClass,
			String name) {
		this.driverClass = driverClass;
		this.name = name;
	}

	public BrowserDriver<?> getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return driverClass.newInstance();
	}

	public static BrowserDriverEnum find(String value) {
		if (value == null)
			return FIREFOX;
		for (BrowserDriverEnum driver : BrowserDriverEnum.values())
			if (value.equalsIgnoreCase(driver.name())
					|| value.equalsIgnoreCase(driver.name))
				return driver;
		return FIREFOX;
	}

	public String getName() {
		return name;
	}

}
