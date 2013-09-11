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
package com.jaeksoft.searchlib.webservice.screenshot;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/screenshot")
public interface RestScreenshot {

	@GET
	@Produces("image/png")
	public byte[] captureScreenshotAPI(@QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("url") URL url,
			@QueryParam("width") Integer browserWidth,
			@QueryParam("height") Integer browserHeight,
			@QueryParam("reduction") Integer reductionPercent,
			@QueryParam("visible") Boolean visiblePartOnly,
			@QueryParam("wait") Integer wait,
			@Context HttpServletRequest request);
}
