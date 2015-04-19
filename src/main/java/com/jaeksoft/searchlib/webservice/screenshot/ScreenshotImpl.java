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
package com.jaeksoft.searchlib.webservice.screenshot;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotThread;
import com.jaeksoft.searchlib.webservice.ApiIdentifier;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.screenshot.RestScreenshot;

public class ScreenshotImpl extends CommonServices implements RestScreenshot {

	@Override
	public byte[] captureScreenshotAPI(String login, String key, URL url,
			Integer browserWidth, Integer browserHeight,
			Integer reductionPercent, Boolean visiblePartOnly, Integer wait,
			HttpServletRequest request) {
		try {
			ClientFactory.INSTANCE.properties.checkApi(key,
					ApiIdentifier.capt00, request.getRemoteAddr());
			if (browserWidth == null)
				browserWidth = 1280;
			if (browserHeight == null)
				browserHeight = 768;
			if (browserWidth > 1600)
				browserWidth = 1600;
			if (browserHeight > 1200)
				browserHeight = 1200;
			if (reductionPercent == null)
				reductionPercent = 100;
			if (visiblePartOnly == null)
				visiblePartOnly = false;
			if (wait == null)
				wait = 0;
			if (wait > 20)
				wait = 20;
			ScreenshotThread screenshotThread = new ScreenshotThread(
					new Dimension(browserWidth, browserHeight),
					reductionPercent, visiblePartOnly, url, wait,
					BrowserDriverEnum.find(ClientFactory.INSTANCE
							.getDefaultWebBrowserDriver().getValue(),
							BrowserDriverEnum.FIREFOX));
			screenshotThread.runner();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(screenshotThread.getImage(), "png", baos);
			return baos.toByteArray();
		} catch (WebApplicationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

}
