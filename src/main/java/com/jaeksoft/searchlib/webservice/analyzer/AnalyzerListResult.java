/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.analyzer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AnalyzerListResult extends CommonResult {

	public final List<AnalyzerListItem> analyzers;

	public AnalyzerListResult() {
		analyzers = null;
	}

	AnalyzerListResult(AnalyzerList analyzerList) {
		super(true, null);
		if (analyzerList == null) {
			analyzers = null;
			addDetail("count", 0);
			return;
		}
		List<String> nameSet = analyzerList.getNameSet();
		if (nameSet == null) {
			analyzers = null;
			addDetail("count", 0);
			return;
		}
		analyzers = new ArrayList<AnalyzerListItem>();
		for (String name : nameSet) {
			List<Analyzer> aList = analyzerList.get(name);
			if (aList == null)
				continue;
			for (Analyzer analyzer : aList)
				analyzers.add(new AnalyzerListItem(analyzer));
		}
		addDetail("count", analyzers.size());
	}

	@JsonInclude(Include.NON_NULL)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class AnalyzerListItem {

		public final String name;
		public final LanguageEnum lang;

		public AnalyzerListItem() {
			name = null;
			lang = null;
		}

		public AnalyzerListItem(Analyzer analyzer) {
			this.name = analyzer.getName();
			this.lang = analyzer.getLang();
		}

	}
}
