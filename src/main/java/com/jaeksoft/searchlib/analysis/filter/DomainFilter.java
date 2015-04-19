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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.filter.domain.AllDomainsTokenFilter;
import com.jaeksoft.searchlib.analysis.filter.domain.DomainTldTokenFilter;
import com.jaeksoft.searchlib.analysis.filter.domain.HostnameTokenFilter;
import com.jaeksoft.searchlib.analysis.filter.domain.TldTokenFilter;

public class DomainFilter extends FilterFactory {

	private boolean silent = true;
	private String strategy = null;

	private final static String TLD_ONLY = "tld only";
	private final static String DOMAIN_TLD_ONLY = "domain.tld only";
	private final static String HOSTNAME_ONLY = "hostname only";
	private final static String ALL_DOMAINS = "domain and sub domains";

	private final static String[] DOMAIN_EXTRACTION_LIST = { TLD_ONLY,
			DOMAIN_TLD_ONLY, HOSTNAME_ONLY, ALL_DOMAINS };

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.FAULT_TOLERANT, Boolean.TRUE.toString(),
				ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
		addProperty(ClassPropertyEnum.DOMAIN_EXTRACTION, ALL_DOMAINS,
				DOMAIN_EXTRACTION_LIST, 0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			silent = Boolean.parseBoolean(value);
		if (prop == ClassPropertyEnum.DOMAIN_EXTRACTION)
			strategy = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		if (TLD_ONLY.equals(strategy))
			return new TldTokenFilter(tokenStream, silent);
		if (DOMAIN_TLD_ONLY.equals(strategy))
			return new DomainTldTokenFilter(tokenStream, silent);
		if (HOSTNAME_ONLY.equals(strategy))
			return new HostnameTokenFilter(tokenStream, silent);
		if (ALL_DOMAINS.equals(strategy))
			return new AllDomainsTokenFilter(tokenStream, silent);
		return null;
	}
}
