/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.util.Locale;

public class Lang {

	public static Locale findLocaleISO639(String lang) {
		if (lang == null)
			return null;
		int l = lang.indexOf('-');
		if (l != -1)
			lang = lang.substring(0, l);
		lang = new Locale(lang).getLanguage();
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales)
			if (locale.getLanguage().equalsIgnoreCase(lang))
				return locale;
		return null;
	}

	public static Locale findLocaleDescription(String language) {
		if (language == null)
			return null;
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales)
			if (locale.getDisplayName(Locale.ENGLISH)
					.equalsIgnoreCase(language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(
					language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayName().equalsIgnoreCase(language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayLanguage().equalsIgnoreCase(language))
				return locale;
		return null;

	}
}
