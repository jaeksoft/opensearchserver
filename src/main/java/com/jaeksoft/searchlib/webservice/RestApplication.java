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

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.LinkUtils;

@ApplicationPath("/rest")
public class RestApplication extends Application {

	public RestApplication(@Context ServletContext sc) {
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(RestException.class);
		classes.add(JacksonConfig.class);
		classes.add(JacksonJsonProvider.class);
		for (WebServiceEnum webServiceEnum : WebServiceEnum.values())
			classes.add(webServiceEnum.getServiceClass());
		return classes;
	}

	public static String getRestURL(String path, User user, Client client,
			String... args) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("/services/rest");
		if (client != null)
			path = path.replace("{index}",
					LinkUtils.UTF8_URL_Encode(client.getIndexName()));
		sb.append(path);
		boolean firstParam = true;
		if (user != null) {
			firstParam = false;
			sb.append("?login=");
			sb.append(LinkUtils.UTF8_URL_Encode(user.getName()));
			sb.append("&key=");
			sb.append(LinkUtils.UTF8_URL_Encode(user.getApiKey()));
		}
		if (args == null)
			return sb.toString();
		for (int i = 0; i < args.length; i += 2) {
			sb.append(firstParam ? '?' : '&');
			firstParam = false;
			sb.append(args[i]);
			if (args[i + 1] != null) {
				sb.append('=');
				sb.append(LinkUtils.UTF8_URL_Encode(args[i + 1]));
			}
		}
		return sb.toString();
	}
}
