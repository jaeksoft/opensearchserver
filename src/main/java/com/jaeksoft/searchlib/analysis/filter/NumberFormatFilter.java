/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class NumberFormatFilter extends FilterFactory {

	private final String OUTPUT_DEFAULT_FORMAT = "0000000000";

	private final String INPUT_DEFAULT_FORMAT = "#,##0.00";

	private final String DEFAULT_DECIMAL_SEP = ".";

	private final String DEFAULT_GROUP_SEP = ",";

	private String input_format = INPUT_DEFAULT_FORMAT;

	private String output_format = OUTPUT_DEFAULT_FORMAT;

	private Character input_decimal_sep = '.';

	private Character output_decimal_sep = '.';

	private Character input_group_sep = ',';

	private Character output_group_sep = ',';

	private String defaultValue = "0";

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.INPUT_NUMBER_FORMAT,
				INPUT_DEFAULT_FORMAT, null, 20, 1);
		addProperty(ClassPropertyEnum.INPUT_DECIMAL_SEPARATOR,
				DEFAULT_DECIMAL_SEP, null, 20, 1);
		addProperty(ClassPropertyEnum.INPUT_GROUP_SEPARATOR, DEFAULT_GROUP_SEP,
				null, 10, 1);
		addProperty(ClassPropertyEnum.OUTPUT_NUMBER_FORMAT,
				OUTPUT_DEFAULT_FORMAT, null, 20, 1);
		addProperty(ClassPropertyEnum.OUTPUT_DECIMAL_SEPARATOR,
				DEFAULT_DECIMAL_SEP, null, 20, 1);
		addProperty(ClassPropertyEnum.OUTPUT_GROUP_SEPARATOR,
				DEFAULT_GROUP_SEP, null, 10, 1);
		addProperty(ClassPropertyEnum.DEFAULT_VALUE, "", null, 30, 1);
	}

	private static Character getSeparatorChar(String value) {
		if (value == null || value.length() == 0)
			return null;
		return value.charAt(0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.INPUT_NUMBER_FORMAT) {
			new DecimalFormat(value);
			input_format = value;
		} else if (prop == ClassPropertyEnum.INPUT_DECIMAL_SEPARATOR) {
			input_decimal_sep = getSeparatorChar(value);
		} else if (prop == ClassPropertyEnum.INPUT_GROUP_SEPARATOR) {
			input_group_sep = getSeparatorChar(value);
		} else if (prop == ClassPropertyEnum.OUTPUT_NUMBER_FORMAT) {
			new DecimalFormat(value);
			output_format = value;
		} else if (prop == ClassPropertyEnum.OUTPUT_DECIMAL_SEPARATOR) {
			output_decimal_sep = getSeparatorChar(value);
		} else if (prop == ClassPropertyEnum.OUTPUT_GROUP_SEPARATOR) {
			output_group_sep = getSeparatorChar(value);
		} else if (prop == ClassPropertyEnum.DEFAULT_VALUE) {
			defaultValue = value;
			if (defaultValue != null && defaultValue.length() == 0)
				defaultValue = null;
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new NumberFormatTermFilter(tokenStream, input_format,
				input_decimal_sep, input_group_sep, output_format,
				output_decimal_sep, output_group_sep);
	}

	private static DecimalFormat newDecimalFormat(String formatPattern,
			Character decimalSep, Character groupSel) {
		DecimalFormat format = new DecimalFormat(formatPattern);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		if (decimalSep != null)
			symbols.setDecimalSeparator(decimalSep);
		else
			format.setDecimalSeparatorAlwaysShown(false);
		if (groupSel != null)
			symbols.setGroupingSeparator(groupSel);
		else
			format.setGroupingUsed(false);
		format.setDecimalFormatSymbols(symbols);
		return format;
	}

	public class NumberFormatTermFilter extends AbstractTermFilter {

		private final DecimalFormat inputNumberFormat;
		private final DecimalFormat outputNumberFormat;

		public NumberFormatTermFilter(TokenStream input, String inputFormat,
				Character inputDecimalSep, Character inputGroupSep,
				String outputFormat, Character outputDecimalSep,
				Character outputGroupSep) {
			super(input);
			inputNumberFormat = newDecimalFormat(inputFormat, inputDecimalSep,
					inputGroupSep);
			outputNumberFormat = newDecimalFormat(outputFormat,
					outputDecimalSep, outputGroupSep);
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			try {
				Number number = inputNumberFormat.parse(termAtt.toString());
				String term = outputNumberFormat.format(number.doubleValue());
				if (term != null)
					createToken(term);
			} catch (NumberFormatException e) {
				if (defaultValue == null)
					return false;
				createToken(defaultValue);
			} catch (ParseException e) {
				if (defaultValue == null)
					return false;
				createToken(defaultValue);
			}
			return true;
		}
	}
}
