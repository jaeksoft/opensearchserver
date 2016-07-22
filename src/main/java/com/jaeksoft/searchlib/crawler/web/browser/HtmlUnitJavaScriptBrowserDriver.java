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

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jaeksoft.searchlib.crawler.web.browser.HtmlUnitJavaScriptBrowserDriver.JSHtmlUnitDriver;

public class HtmlUnitJavaScriptBrowserDriver extends BrowserDriver<JSHtmlUnitDriver> {

	public HtmlUnitJavaScriptBrowserDriver() {
		super(BrowserDriverEnum.HTML_UNIT_JS);
	}

	@Override
	public JSHtmlUnitDriver initialize() {
		return new JSHtmlUnitDriver();
	}

	public static class JSHtmlUnitDriver extends HtmlUnitDriver {

		private JSHtmlUnitDriver() {
			super(true);
		}

		@Override
		protected WebClient modifyWebClient(WebClient client) {
			WebClient modifiedClient = super.modifyWebClient(client);
			modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
			return modifiedClient;
		}
	}

}
