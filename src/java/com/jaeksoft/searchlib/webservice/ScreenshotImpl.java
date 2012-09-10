/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.web.ScreenshotServlet;

public class ScreenshotImpl extends CommonServicesImpl implements Screenshot {

	private String message = null;

	@Override
	public String screenshot(String use, String login, String key,
			ScreenshotActionEnum action, URL url) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (isLogged(use, login, key)) {
				Client client = ClientCatalog.getClient(use);
				ScreenshotManager screenshotManager = client
						.getScreenshotManager();
				CredentialManager credentialManager = client
						.getWebCredentialManager();
				if (ScreenshotActionEnum.CAPTURE.getName().equalsIgnoreCase(
						action.getName())) {
					ScreenshotServlet.doCapture(null, screenshotManager,
							credentialManager, url);
					message = "Captured URL " + url;
				} else if (ScreenshotActionEnum.CHECK.getName()
						.equalsIgnoreCase(action.getName())) {
					message = ScreenshotServlet.doCheck(screenshotManager, url);
				}

			} else
				throw new WebServiceException("Bad Credential");
		} catch (MalformedURLException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
		return message;
	}

}
