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

package com.jaeksoft.searchlib.crawler.web.screenshot;

import java.net.URL;

public enum ScreenshotMethodEnum {

	NO_SCREENSHOT(new ScreenshotMethod()),

	HOMEPAGE(new ScreenshotMethodHomepage()),

	ALL(new ScreenshotMethodAll());

	protected final ScreenshotMethod method;

	private ScreenshotMethodEnum(ScreenshotMethod method) {
		this.method = method;
	}

	final public boolean doScreenshot(URL url) {
		return method.doScreenshot(url);
	}

	@Override
	final public String toString() {
		return method.name;
	}

	final public String getName() {
		return method.name;
	}

	public static ScreenshotMethodEnum find(String value) {
		if (value == null)
			return NO_SCREENSHOT;
		for (ScreenshotMethodEnum methodEnum : ScreenshotMethodEnum.values())
			if (value.equalsIgnoreCase(methodEnum.method.name)
					|| value.equalsIgnoreCase(methodEnum.name()))
				return methodEnum;
		return NO_SCREENSHOT;
	}
}
