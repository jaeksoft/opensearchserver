/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

public enum LanguageEnum {

	UNDEFINED("Undefined", ""),

	ARABIC("Arabic", "ar"),

	CHINESE("Chinese", "zh"),

	DANISH("Danish", "da"),

	DUTCH("Dutch", "nl"),

	ENGLISH("English", "en"),

	FINNISH("Finnish", "fi"),

	FRENCH("French", "fr"),

	GERMAN("German", "de"),

	HUNGARIAN("Hungarian", "hu"),

	ITALIAN("Italian", "it"),

	JAPANESE("Japansese", "ja"),

	KOREAN("Korean", "kr"),

	NORWEGIAN("Norwegian", "no"),

	PORTUGUESE("Portuguese", "pt"),

	ROMANIAN("Romanian", "ro"),

	RUSSIAN("Russian", "ru"),

	SPANISH("Spanish", "es"),

	SWEDISH("Swedish", "sv"),

	TURKISH("Turkish", "tr");

	private String name;

	private String code;

	private LanguageEnum(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static LanguageEnum findByCode(String code) {
		if (code == null)
			return UNDEFINED;
		for (LanguageEnum lang : LanguageEnum.values())
			if (code.equalsIgnoreCase(lang.code))
				return lang;
		return UNDEFINED;
	}

	public static LanguageEnum findByName(String name) {
		if (name == null)
			return UNDEFINED;
		for (LanguageEnum lang : LanguageEnum.values())
			if (name.equalsIgnoreCase(lang.name))
				return lang;
		return UNDEFINED;
	}

	public static LanguageEnum findByNameOrCode(String nameOrCode) {
		if (nameOrCode == null)
			return UNDEFINED;
		LanguageEnum lang = findByName(nameOrCode);
		if (lang != UNDEFINED)
			return lang;
		return findByCode(nameOrCode);
	}

	public static String[] stringArray() {
		String[] array = new String[values().length];
		int i = 0;
		for (LanguageEnum lang : LanguageEnum.values())
			array[i++] = lang.name;
		return array;
	}

}
