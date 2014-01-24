/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

public enum FilterEnum {

	StandardFilter("Normalizes tokens extracted with StandardTokenizer."),

	AcronymFilter("A filter which normalize acronyms"),

	ArabicStemFilter("A filter that filter Arabic words."),

	ArabicNormalizerFilter("A filter to normalize the Arabic orthography"),

	ChineseFilter("A filter that filter Chinese words."),

	DecodeHtmlEntitiesFilter("This filter decode the HTML entities"),

	DeduplicateTokenFilter("This filter remove any duplicates token"),

	DegreesRadiansFilter(
			"This filter convert radians to degrees or degrees to radians, or check degrees or radians values. Required by geospatial features."),

	DutchStemFilter("A filter that stems Dutch words."),

	DomainFilter("A filter that extract the domain.tld from an URL."),

	ElisionFilter("This filter removes elisions from words"),

	EdgeNGramFilter(
			"This filter create n-grams from the beginning edge or ending edge of a input token"),

	ExpressionLookupFilter(
			"This filter extracts the expressions found in an expression list"),

	FrenchStemFilter("A filter that stems French words"),

	GroupAllTokensFilter("This filter group all tokens to one"),

	HunspellStemFilter(
			"This filter apply lemmatisation using an Hunspell dictionary"),

	IndexLookupFilter("This filter extracts terms found in a search request"),

	ISOLatin1AccentFilter(
			"This filter replaces accented characters (characters with diacritics) in the ISO latin 1 character set (ISO-8859-1) with the unaccented (no diacritics) equivalent."),

	JsonPathFilter(
			"This filter extract terms from a JSON structure using Json Path syntax."),

	LowerCaseFilter("This filter normalises text into lower case characters"),

	NamedEntityExtractionFilter(
			"This filter extracts named entity using a configured NamedEntityRequest"),

	NGramFilter(
			"This filter tokenizes the input into n-grams of the given size(s)."),

	NumberFormatFilter(
			"This filter formats number following the provided pattern"),

	PhoneticFilter("A filter that encode words using phonetic algorithms."),

	PolishStemFilter("Stems Polish words"),

	PrefixSuffixStopFilter(
			"This filter removes words which match with prefixes or suffixes of a given term list."),

	RegularExpressionFilter(
			"This filter generates tokens by applying a regular expression"),

	RemoveDuplicateCharactersFilter("Remove consecutive duplicate characters."),

	RemoveIncludedTermFilter("Remove tokens included in any other token"),

	RemoveTokenTypeFilter("Remove token of given type."),

	RemoveTagFilter("Remove XML/HTML tags"),

	RussianStemFilter("A filter that stems Russian words."),

	ShingleFilter(
			"A ShingleFilter constructs shingles (token n-grams) from a token stream."),

	SnowballDanishFilter(
			"Stems Danish words using a Snowball-generated stemmer"),

	SnowballEnglishFilter(
			"Stems English words using a Snowball-generated stemmer"),

	SnowballFinnishFilter(
			"Stems Finnish words using a Snowball-generated stemmer"),

	SnowballGermanFilter(
			"Stems German words using a Snowball-generated stemmer"),

	SnowballHungarianFilter(
			"Stems Hungarian words using a Snowball-generated stemmer"),

	SnowballItalianFilter(
			"Stems Italian words using a Snowball-generated stemmer"),

	SnowballNorwegianFilter(
			"Stems Norwegian words using a Snowball-generated stemmer"),

	SnowballPortugueseFilter(
			"Stems Portuguese words using a Snowball-generated stemmer"),

	SnowballRomanianFilter(
			"Stems Romanian words using a Snowball-generated stemmer"),

	SnowballSpanishFilter(
			"Stems Spanish words using a Snowball-generated stemmer"),

	SnowballSwedishFilter(
			"Stems Swedish words using a Snowball-generated stemmer"),

	SnowballTurkishFilter(
			"Stems Turkish words using a Snowball-generated stemmer"),

	StopFilter("Removes stop words."),

	SynonymFilter("Add synonyms support."),

	URLNormalizerFilter("URL normalizer"),

	YouTubeFilter("Extract details from youtube using youtube API"),

	DailymotionFilter("Extract details from Dailymotion using Dailymotion API"),

	VimeoFilter("Extract details from Vimeo using Dailymotion API");

	private String description;

	private FilterEnum(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
