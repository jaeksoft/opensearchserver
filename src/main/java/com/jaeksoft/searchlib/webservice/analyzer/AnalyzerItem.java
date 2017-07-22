/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2017 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.webservice.analyzer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.ClassFactory;
import com.jaeksoft.searchlib.analysis.ClassProperty;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.FilterScope;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerFactory;
import com.jaeksoft.searchlib.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AnalyzerItem {

	public final ClassFactoryItem queryTokenizer;
	public final ClassFactoryItem indexTokenizer;
	public final List<ClassFactoryItem> filters;

	public AnalyzerItem() {
		queryTokenizer = null;
		indexTokenizer = null;
		filters = null;
	}

	public AnalyzerItem(Analyzer analyzer, boolean listOnly) {
		if (listOnly) {
			queryTokenizer = null;
			indexTokenizer = null;
			filters = null;
			return;
		}
		TokenizerFactory tokenizer = analyzer.getQueryTokenizer();
		queryTokenizer = tokenizer == null ? null : new ClassFactoryItem(tokenizer, null);
		tokenizer = analyzer.getIndexTokenizer();
		indexTokenizer = tokenizer == null ? null : new ClassFactoryItem(tokenizer, null);
		List<FilterFactory> filterList = analyzer.getFilters();
		if (filterList == null) {
			filters = null;
			return;
		}
		filters = new ArrayList<ClassFactoryItem>(filterList.size());
		for (FilterFactory filterFactory : filterList)
			filters.add(new ClassFactoryItem(filterFactory, filterFactory.getScope()));
	}

	@JsonInclude(Include.NON_NULL)
	public static class ClassFactoryItem {

		public final String name;
		public final Map<String, Object> properties;
		public final FilterScope scope;

		public ClassFactoryItem() {
			name = null;
			properties = null;
			scope = null;
		}

		public ClassFactoryItem(ClassFactory classFactory, FilterScope scope) {
			this.name = classFactory.getClassName();
			this.scope = scope;
			List<ClassProperty> props = classFactory.getUserProperties();
			if (props == null) {
				properties = null;
				return;
			}
			properties = new HashMap<String, Object>();
			for (ClassProperty prop : props)
				properties.put(prop.getClassPropertyEnum().getName(), prop.getValue());
		}

		private void applyProperties(ClassFactory classFactory) throws SearchLibException {
			if (properties == null)
				return;
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value == null)
					continue;
				ClassPropertyEnum classPropertyEnum = ClassPropertyEnum.valueOf(key);
				if (classPropertyEnum == null)
					throw new SearchLibException("Property not found: " + key);
				ClassProperty classProperty = classFactory.getProperty(classPropertyEnum);
				if (classProperty == null)
					throw new SearchLibException("This property is not supported: " + key);
				classProperty.setValue(value.toString());
			}
		}

		private TokenizerFactory getTokenizerFactory(Config config) throws SearchLibException, ClassNotFoundException {
			TokenizerFactory tokenizer = TokenizerFactory.create(config, name);
			applyProperties(tokenizer);
			return tokenizer;
		}

		public FilterFactory getFilterFactory(Config config) throws SearchLibException, ClassNotFoundException {
			FilterFactory filter = FilterFactory.create(config, name);
			if (scope != null)
				filter.setScope(scope);
			applyProperties(filter);
			return filter;
		}

	}

	public Analyzer get(Config config, String name, LanguageEnum language)
			throws SearchLibException, ClassNotFoundException {
		Analyzer analyzer = new Analyzer(config);
		analyzer.setName(name);
		analyzer.setLang(language);
		if (queryTokenizer != null)
			analyzer.setQueryTokenizer(queryTokenizer.getTokenizerFactory(config));
		if (indexTokenizer != null)
			analyzer.setIndexTokenizer(indexTokenizer.getTokenizerFactory(config));
		if (filters != null)
			for (ClassFactoryItem filter : filters)
				analyzer.add(filter.getFilterFactory(config));
		return analyzer;
	}
}
