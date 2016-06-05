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
package com.jaeksoft.searchlib.test;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.Krb5Utils;
import jcifs.smb.Kerb5Authenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.junit.Assert;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;

public class SmbFileTest {

	@Test
	public void test1_krb5() throws LoginException, SearchLibException, MalformedURLException, SmbException {

		final String krb5ConfPath = System.getenv(SmbFileTest.class.getName() + ".krb5ConfPath");
		final String principal = System.getenv(SmbFileTest.class.getName() + ".principal");
		final String keyTabPath = System.getenv(SmbFileTest.class.getName() + ".keyTabPath");
		final String url = System.getenv(SmbFileTest.class.getName() + ".url");
		if (principal == null || keyTabPath == null || url == null || krb5ConfPath == null)
			return; // Bypass test

		Kerb5Authenticator auth =
				new Kerb5Authenticator(Krb5Utils.loginWithKeyTab(krb5ConfPath, principal, keyTabPath));
		String[] list = new SmbFile(url, auth).list();
		Assert.assertNotNull(list);
		Assert.assertTrue(list.length > 0);
	}
}
