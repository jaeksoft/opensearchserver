/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter;

import org.apache.commons.codec.language.Caverphone1;
import org.apache.commons.codec.language.Caverphone2;
import org.apache.commons.codec.language.ColognePhonetic;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.filter.phonetic.BeiderMorseTokenFilter;
import com.jaeksoft.searchlib.analysis.filter.phonetic.EncoderTokenFilter;

public class PhoneticFilter extends FilterFactory {

	private String codec = null;

	private final static String BEIDER_MORSE = "Beider Morse";
	private final static String CAVERPHONE1 = "Caverphone 1";
	private final static String CAVERPHONE2 = "Caverphone 2";
	private final static String COLOGNE_PHONETIC = "Cologne phonetic";
	private final static String METAPHONE = "Metaphone";
	private final static String REFINED_SOUNDEX = "Refined soundex";
	private final static String SOUNDEX = "Soundex";

	private final static String[] CODEC_LIST = { BEIDER_MORSE, CAVERPHONE1,
			CAVERPHONE2, COLOGNE_PHONETIC, METAPHONE, REFINED_SOUNDEX, SOUNDEX };

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.CODEC, BEIDER_MORSE, CODEC_LIST);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.CODEC)
			codec = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		if (BEIDER_MORSE.equals(codec))
			return new BeiderMorseTokenFilter(tokenStream);
		if (COLOGNE_PHONETIC.equals(codec))
			return new EncoderTokenFilter(tokenStream, new ColognePhonetic());
		if (SOUNDEX.equals(codec))
			return new EncoderTokenFilter(tokenStream, new Soundex());
		if (REFINED_SOUNDEX.equals(codec))
			return new EncoderTokenFilter(tokenStream, new RefinedSoundex());
		if (METAPHONE.equals(codec))
			return new EncoderTokenFilter(tokenStream, new Metaphone());
		if (CAVERPHONE1.equals(codec))
			return new EncoderTokenFilter(tokenStream, new Caverphone1());
		if (CAVERPHONE2.equals(codec))
			return new EncoderTokenFilter(tokenStream, new Caverphone2());
		return null;
	}
}
