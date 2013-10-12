/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.cxf.helpers.IOUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;

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

	public static final Locale findLocaleDescription(String language) {
		if (StringUtils.isEmpty(language))
			return null;
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales)
			if (locale.getLanguage().equalsIgnoreCase(language))
				return locale;
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

	static {
		try {
			List<String> profiles = new ArrayList<String>(0);
			for (LanguageEnum le : LanguageEnum.values()) {
				if (le == LanguageEnum.UNDEFINED)
					continue;
				InputStream is = com.cybozu.labs.langdetect.Detector.class
						.getResourceAsStream("/profiles/" + le.getCode());
				if (is == null && le.getAlternativeCode() != null)
					is = com.cybozu.labs.langdetect.Detector.class
							.getResourceAsStream("/profiles/"
									+ le.getAlternativeCode());
				if (is == null) {
					System.err.println("Not profile for lang " + le.getName());
					continue;
				}
				profiles.add(IOUtils.readStringFromStream(is));
				is.close();
			}
			DetectorFactory.loadProfile(profiles);
		} catch (LangDetectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final Locale langDetection(String text, int length)
			throws LangDetectException {
		if (StringUtils.isEmpty(text))
			return null;
		Detector detector = DetectorFactory.create();
		detector.setMaxTextLength(length);
		detector.append(text);
		String lang = detector.detect();
		return Lang.findLocaleDescription(lang);
	}

	private final static String HU_TEST = "OpenSearchServer documentation Spaces People Quick Search Help Online Help Keyboard Shortcuts Feed Builder What’s new Available Gadgets Log In Sign Up Settings Popular Labels All Labels Popular Labels Skip to Recently Updated userguide Powered by a free Atlassian Confluence Open Source Project License granted to OpenSearchServer. Evaluate Confluence today . Powered by Atlassian Confluence 5.1.3 , Team Collaboration Software Printed by Atlassian Confluence 5.1.3, Team Collaboration Software. Report a bug Atlassian News";

	public final static void main(String[] args) throws LangDetectException {
		System.out.println(langDetection(HU_TEST, 10000));
	}
}
