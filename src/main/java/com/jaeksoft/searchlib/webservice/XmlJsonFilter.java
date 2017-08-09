/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.webservice;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@PreMatching
public class XmlJsonFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		final String type = ctx.getUriInfo().getQueryParameters().getFirst("_type");
		final String mediaType;
		switch (type == null ? "json" : type) {
		default:
		case "json":
			mediaType = MediaType.APPLICATION_JSON;
			break;
		case "xml":
			mediaType = MediaType.APPLICATION_XML;
			break;
		}
		if (ctx.getAcceptableMediaTypes().contains(mediaType))
			ctx.getHeaders().putSingle("Accept", mediaType);
	}

}
