/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util;

import com.sun.security.auth.module.Krb5LoginModule;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

public class Krb5Utils {

	public static Subject loginWithKeyTab(final String krb5confPath, final String principal,
			final String krb5keyTabPath) throws LoginException {

		// Set state
		final Map<String, Object> state = new HashMap<>();
		state.put("java.security.krb5.conf", krb5confPath);

		// Set options
		final Map<String, Object> option = new HashMap<>();
		//option.put("debug", "true");
		option.put("principal", principal);
		option.put("useKeyTab", "true");
		option.put("keyTab", krb5keyTabPath);
		//option.put("refreshKrb5Config", "true");
		option.put("doNotPrompt", "true");
		option.put("storeKey", "true");
		option.put("useTicketCache", "true");

		// Login
		final Subject subject = new Subject();
		Krb5LoginModule login = new Krb5LoginModule();
		login.initialize(subject, null, state, option);
		if (login.login())
			login.commit();
		return subject;
	}

}
