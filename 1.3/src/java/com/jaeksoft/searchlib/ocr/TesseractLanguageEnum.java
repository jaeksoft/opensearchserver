/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.ocr;

import com.jaeksoft.searchlib.analysis.LanguageEnum;

public enum TesseractLanguageEnum {

	None(null, null),

	Arabic("ara", null),

	Bulgarian("bul", null),

	Catalan("cat", null),

	Czech("ces", null),

	ChineseSimplified("chi-sim", LanguageEnum.CHINESE),

	ChineseTraditional("chi-tra", null),

	Danish("dan", LanguageEnum.DANISH),

	DanishFraktur("dan-frak", null),

	German("deu", LanguageEnum.GERMAN),

	GermanFraktur("deu-frak", null),

	Greek("ell", null),

	English("eng", LanguageEnum.ENGLISH),

	Finnish("fin", LanguageEnum.FINNISH),

	French("fra", LanguageEnum.FRENCH),

	Hebrew("heb", null),

	HebrewCommunity("heb-com", null),

	Hindi("hin", null),

	Hungarian("hun", LanguageEnum.HUNGARIAN),

	Indonesian("ind", null),

	Italian("ita", LanguageEnum.ITALIAN),

	Japanese("jpn", LanguageEnum.JAPANESE),

	Korean("kor", null),

	Latvian("lav", null),

	Lithuanian("lit", null),

	Dutch("nld", LanguageEnum.DUTCH),

	Norwegian("nor", LanguageEnum.NORWEGIAN),

	Polish("pol", null),

	Portuguese("por", LanguageEnum.PORTUGUESE),

	Romanian("ron", LanguageEnum.ROMANIAN),

	Russian("rus", LanguageEnum.RUSSIAN),

	Slovakian("slk", null),

	SlovakianFraktur("slk-frak", null),

	Slovenian("slv", null),

	Spanish("spa", LanguageEnum.SPANISH),

	Serbian("srp", null),

	Swedish("swe", LanguageEnum.SWEDISH),

	SwedishFraktur("swe-frak", null),

	Tagalog("tgl", null),

	Thai("tha", null),

	Turkish("tur", LanguageEnum.TURKISH),

	Ukranian("ukr", null),

	Vietnamese("vie", null);

	final public String option;

	final private LanguageEnum langEnum;

	private TesseractLanguageEnum(String option, LanguageEnum langEnum) {
		this.option = option;
		this.langEnum = langEnum;
	}

	final public static TesseractLanguageEnum find(LanguageEnum lang) {
		if (lang == null)
			return null;
		for (TesseractLanguageEnum tle : values())
			if (tle.langEnum == lang)
				return tle;
		return null;
	}

	final public static TesseractLanguageEnum find(String property) {
		for (TesseractLanguageEnum tle : values())
			if (tle.name().equals(property))
				return tle;
		return null;
	}

}
