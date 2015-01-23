/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class ProperNounFilter extends FilterFactory {

	private final static String PERSON = "person";
	private final static String ORGANIZATION = "organization";

	private final static String[] TYPES = { PERSON, ORGANIZATION };

	private final static String TOKEN_TYPE = "synonym";

	private String type = PERSON;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.PROPER_NOUN_TYPE, PERSON, TYPES, 12, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.PROPER_NOUN_TYPE)
			type = value;
	}

	@Override
	public TokenStream create(TokenStream input) throws SearchLibException {
		if (type == ORGANIZATION)
			return new OrganizationTokenFilter(input);
		return new PersonTokenFilter(input);
	}

	private final static void joinInitial(List<String> input,
			List<String> output, int initialCount, char separator,
			boolean withFinal) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String term : input) {
			if (initialCount-- > 0)
				term = term.substring(0, 1);
			if (first)
				first = false;
			else if (separator != Character.MIN_VALUE)
				sb.append(separator);
			sb.append(term);
		}
		if (withFinal)
			sb.append(separator);
		output.add(sb.toString());
	}

	public class PersonTokenFilter extends AbstractTermCollectFilter {

		protected PersonTokenFilter(TokenStream input) {
			super(TOKEN_TYPE, input);
		}

		@Override
		protected void createTokens(List<String> input, List<String> output) {
			if (input.size() == 1) {
				output.add(input.get(0));
				return;
			}
			for (int i = 0; i < input.size(); i++) {
				joinInitial(input, output, i, '.', false);
				joinInitial(input, output, i, ' ', false);
				joinInitial(input, output, i, '-', false);
			}
		}
	}

	public class OrganizationTokenFilter extends AbstractTermCollectFilter {

		protected OrganizationTokenFilter(TokenStream input) {
			super(TOKEN_TYPE, input);
		}

		private void createTokens(List<String> input, List<String> output,
				int size) {
			joinInitial(input, output, size, '.', false);
			joinInitial(input, output, size, ' ', false);
			joinInitial(input, output, size, '-', false);
			joinInitial(input, output, size, Character.MIN_VALUE, false);
			joinInitial(input, output, size, '.', true);
		}

		@Override
		protected void createTokens(List<String> input, List<String> output) {
			if (input.size() == 1) {
				output.add(input.get(0));
				return;
			}
			createTokens(input, output, 0);
			createTokens(input, output, input.size());
		}
	}

	public static void main(String[] args) {
		List<String> input = Arrays.asList("Louis", "Ferdinand", "Celine");
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < input.size(); i++) {
			joinInitial(input, output, i, '.', false);
			joinInitial(input, output, i, ' ', false);
			joinInitial(input, output, i, '-', false);
		}
		System.out.println(output);
		output.clear();
		input = Arrays.asList("International", "Business", "Machine");
		joinInitial(input, output, 0, '.', false);
		joinInitial(input, output, 0, ' ', false);
		joinInitial(input, output, 0, '-', false);
		joinInitial(input, output, 0, Character.MIN_VALUE, false);
		joinInitial(input, output, input.size(), '.', false);
		joinInitial(input, output, input.size(), ' ', false);
		joinInitial(input, output, input.size(), '-', false);
		joinInitial(input, output, input.size(), Character.MIN_VALUE, false);
		joinInitial(input, output, input.size(), '.', true);
		System.out.println(output);
	}
}
