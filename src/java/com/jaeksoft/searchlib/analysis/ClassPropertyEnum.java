/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class ClassPropertyEnum extends ExtensibleEnumItem<ClassPropertyEnum> {
	public final static ExtensibleEnum<ClassPropertyEnum> ENUM = new ExtensibleEnum<ClassPropertyEnum>();

	public final static ClassPropertyEnum SCOPE = new ClassPropertyEnum(false,
			"scope", "Scope", "The scope of the class");

	public final static ClassPropertyEnum CLASS = new ClassPropertyEnum(false,
			"class", "Class", "The name of the class");

	public final static ClassPropertyEnum FILE_LIST = new ClassPropertyEnum(
			true, "file", "List", "The name of the list");

	public final static ClassPropertyEnum PREFIX_FILE_LIST = new ClassPropertyEnum(
			true, "prefixList", "Prefix list", "The name of the prefix list");

	public final static ClassPropertyEnum SUFFIX_FILE_LIST = new ClassPropertyEnum(
			true, "suffixList", "Suffix list", "The name of the suffix list");

	public final static ClassPropertyEnum SIZE_LIMIT = new ClassPropertyEnum(
			true, "sizeLimit", "Size Limit",
			"The Size Limit of the file to be Parsed");

	public final static ClassPropertyEnum DEFAULT_CHARSET = new ClassPropertyEnum(
			true, "defaultCharset", "Default Charset",
			"The charset to use if not charset is found");

	public final static ClassPropertyEnum PARSER_NAME = new ClassPropertyEnum(
			false, "name", "name", "The name of the parser ");

	public final static ClassPropertyEnum PARSER_FAIL_OVER_NAME = new ClassPropertyEnum(
			false, "fileOver", "Fail over parser",
			"The name of the parser used as fail over");

	public final static ClassPropertyEnum MIN_GRAM = new ClassPropertyEnum(
			true, "min_gram", "Min gram size",
			"The smallest n-gram to generate");

	public final static ClassPropertyEnum MAX_GRAM = new ClassPropertyEnum(
			true, "max_gram", "Max gram size", "The largest n-gram to generate");

	public final static ClassPropertyEnum SIDE = new ClassPropertyEnum(true,
			"side", "Edge side",
			"Specifies which side of the input the n-gram should be generated from");

	public final static ClassPropertyEnum TOKEN_SEPARATOR = new ClassPropertyEnum(
			true, "token_separator", "Token separator",
			"The string to use when joining adjacent tokens");

	public final static ClassPropertyEnum IGNORE_CASE = new ClassPropertyEnum(
			true, "ignore_case", "Ignore case", "");

	public final static ClassPropertyEnum MAX_SHINGLE_SIZE = new ClassPropertyEnum(
			true, "max_shingle_size", "Max shingle size",
			"Set the max shingle size (default: 2)");

	public final static ClassPropertyEnum MIN_SHINGLE_SIZE = new ClassPropertyEnum(
			true, "min_shingle_size", "Min shingle size",
			"Set the min shingle size (default: 1)");

	public final static ClassPropertyEnum FAULT_TOLERANT = new ClassPropertyEnum(
			true, "fault_tolerant", "Fault tolerant",
			"Decide wether or not the process will stops on errors");

	public static final ClassPropertyEnum DOMAIN_EXTRACTION = new ClassPropertyEnum(
			true, "domain_extraction", "Domain extraction",
			"Select a domain extraction strategy");

	public static final ClassPropertyEnum CODEC = new ClassPropertyEnum(true,
			"codec", "Codec algorithm", "Select a encoder/decoder algorithm");

	public static ClassPropertyEnum REMOVE_DUPLICATE_LETTERS = new ClassPropertyEnum(
			true, "removeDuplicateLetters", "Duplicate letters",
			"Detect and remove consecutive duplicate letters");

	public static ClassPropertyEnum NUMBER_FORMAT = new ClassPropertyEnum(
			true,
			"numberFormat",
			"Number format",
			"Format number using java decimal format, for example, \"#,##0.00;(#,##0.00)\".");

	public static ClassPropertyEnum JSON_PATH = new ClassPropertyEnum(true,
			"jsonPath", "Json Path",
			"Extract data from a JSON structure using Json Path syntax");

	public static ClassPropertyEnum REMOVE_DUPLICATE_DIGITS = new ClassPropertyEnum(
			true, "removeDuplicateDigits", "Duplicate digits",
			"Detect and remove consecutive duplicate digits");

	public static ClassPropertyEnum REMOVE_DUPLICATE_WHITESPACES = new ClassPropertyEnum(
			true, "removeDuplicateWhiteSpaces", "Duplicate spaces",
			"Detect and remove consecutive duplicate white spaces");

	public final static ClassPropertyEnum URL_FRAGMENT = new ClassPropertyEnum(
			true, "url_fragment", "URL Fragment",
			"The Option to remove or keep the URL Fragment");

	public final static ClassPropertyEnum REGULAR_EXPRESSION = new ClassPropertyEnum(
			true, "regular_expression", "Regular Expression",
			"The regular expression pattern");

	public final static ClassPropertyEnum H1_BOOST = new ClassPropertyEnum(
			true, "h1_boost", "H1 Boost", "Boost for H1 Tag in Html Content");

	public final static ClassPropertyEnum H2_BOOST = new ClassPropertyEnum(
			true, "h2_boost", "H2 Boost", "Boost for H2 Tag in Html Content");

	public final static ClassPropertyEnum H3_BOOST = new ClassPropertyEnum(
			true, "h3_boost", "H3 Boost", "Boost for H3 Tag in Html Content");

	public final static ClassPropertyEnum H4_BOOST = new ClassPropertyEnum(
			true, "h4_boost", "H4 Boost", "Boost for H4 Tag in Html Content");

	public final static ClassPropertyEnum H5_BOOST = new ClassPropertyEnum(
			true, "h5_boost", "H5 Boost", "Boost for H5 Tag in Html Content");

	public final static ClassPropertyEnum H6_BOOST = new ClassPropertyEnum(
			true, "h6_boost", "H6 Boost", "Boost for H6 Tag in Html Content");

	public final static ClassPropertyEnum TITLE_BOOST = new ClassPropertyEnum(
			true, "title_boost", "Title Boost",
			"Boost for Title Tag in Html Content");

	public final static String[] BOOLEAN_LIST = { Boolean.TRUE.toString(),
			Boolean.FALSE.toString() };

	public final static ClassPropertyEnum OCR_PDF_METHOD = new ClassPropertyEnum(
			true, "ocr_pdf_method", "OCR method for PDF",
			"Select an OCR stragegy for images in PDF");

	public final static String[] OCR_PDF_METHODS = { "Embedded images",
			"Full page" };

	public final static String[] KEEP_REMOVE_LIST = { "keep", "remove" };

	public final static String[] YOUTUBE_DATA_LIST = { "title", "description",
			"full (json)" };

	public final static String[] VIMEO_DATA_LIST = { "title", "description",
			"full (json)" };

	public static String[] DAILYMOTION_DATA_LIST = { "title", "full (json)" };

	public static ClassPropertyEnum VIMEO_DATA = new ClassPropertyEnum(true,
			"vimeo_data", "Vimeo Field", "The vimeo API data");

	public static final ClassPropertyEnum YOUTUBE_DATA = new ClassPropertyEnum(
			true, "youtube_data", "Youtube Field", "The youtube API data");

	public static final ClassPropertyEnum DAILYMOTION_DATA = new ClassPropertyEnum(
			true, "dailymotion_data", "Dailymotion Field",
			"The Dailymotion API data");

	public static final ClassPropertyEnum DEGREES_RADIANS_CONVERSION = new ClassPropertyEnum(
			true, "degrees_radians_conversion", "Conversion direction",
			"The direction of the conversion");

	public final static ClassPropertyEnum DEFAULT_VALUE = new ClassPropertyEnum(
			true, "defaultValue", "Default Value",
			"The default value to use if no value is found");

	public static String[] DEGREES_RADIANS_CONVERSION_LIST = {
			"Degrees to Radians", "Radians to Degrees", "Check degrees",
			"Check radians" };

	public static ClassPropertyEnum IGNORE_META_NOINDEX = new ClassPropertyEnum(
			true, "ignoreMetaNoIndex", "Ignore meta noindex",
			"Ignore the meta noindex tag in HTML header");

	private boolean isUser;

	private String label;

	private String info;

	private String xmlAttributeName;

	protected ClassPropertyEnum(boolean isUser, String xmlAttributeName,
			String label, String info) {
		super(ENUM, xmlAttributeName);
		this.isUser = isUser;
		this.xmlAttributeName = xmlAttributeName;
		this.label = label;
		this.info = info;
	}

	/**
	 * Returns a string used for XML attribute storage
	 * 
	 * @return
	 */
	public String getAttribute() {
		return xmlAttributeName;
	}

	/**
	 * Return true if the properties is a user property
	 * 
	 * @return
	 */
	public boolean isUser() {
		return isUser;
	}

	/**
	 * 
	 * @return the literal labe of the property
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the information
	 */
	public String getInfo() {
		return info;
	}

	public static ClassPropertyEnum valueOf(String enumValue) {
		return ClassPropertyEnum.ENUM.getValue(enumValue);
	}

}
