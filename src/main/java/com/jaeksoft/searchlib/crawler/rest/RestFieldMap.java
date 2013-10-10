/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.rest;

import java.io.IOException;
import java.net.URISyntaxException;

import net.minidev.json.JSONArray;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class RestFieldMap extends
		FieldMapGeneric<SourceField, CommonFieldTarget> {

	@Override
	protected CommonFieldTarget loadTarget(String targetName, Node node) {
		return new CommonFieldTarget(targetName, node);
	}

	@Override
	protected SourceField loadSource(String source) {
		return new SourceField(source);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, CommonFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	public boolean isUrl() {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			CommonFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isCrawlUrl())
				return true;
		}
		return false;
	}

	public void mapJson(WebCrawlMaster webCrawlMaster,
			ParserSelector parserSelector, LanguageEnum lang,
			Object jsonObject, IndexDocument target) throws SearchLibException,
			IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException,
			InstantiationException, IllegalAccessException {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			String jsonPath = link.getSource().getUniqueName();
			try {
				Object jsonContent = JsonPath.read(jsonObject, jsonPath);
				if (jsonContent == null)
					continue;
				if (jsonContent instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) jsonContent;
					for (Object content : jsonArray) {
						if (content != null)
							this.mapFieldTarget(webCrawlMaster, parserSelector,
									lang, link.getTarget(), content.toString(),
									target);
					}
				} else
					this.mapFieldTarget(webCrawlMaster, parserSelector, lang,
							link.getTarget(), jsonContent.toString(), target);
			} catch (PathNotFoundException e) {
				continue;
			} catch (IllegalArgumentException e) {
				Logging.warn(e);
				continue;
			}
		}

	}
}
