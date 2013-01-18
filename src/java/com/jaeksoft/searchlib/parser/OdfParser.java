/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.common.TextExtractor;
import org.odftoolkit.simple.meta.Meta;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public abstract class OdfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.creator,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.producer, ParserFieldEnum.keywords,
			ParserFieldEnum.creation_date, ParserFieldEnum.modification_date,
			ParserFieldEnum.language };

	public OdfParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) {
		// Load file
		Document document = null;
		try {
			document = Document.loadDocument(streamLimiter.getNewInputStream());
			if (document == null)
				return;
			ParserResultItem result = getNewParserResultItem();
			Meta meta = document.getOfficeMetadata();
			if (meta != null) {
				result.addField(ParserFieldEnum.creation_date,
						meta.getCreationDate());
				result.addField(ParserFieldEnum.modification_date,
						meta.getDcdate());
				result.addField(ParserFieldEnum.title, meta.getTitle());
				result.addField(ParserFieldEnum.subject, meta.getSubject());
				result.addField(ParserFieldEnum.creator, meta.getCreator());
				result.addField(ParserFieldEnum.producer, meta.getGenerator());
				result.addField(ParserFieldEnum.keywords, meta.getKeywords());
				result.addField(ParserFieldEnum.language, meta.getLanguage());
			}

			OdfElement odfElement = document.getContentRoot();
			if (odfElement != null) {
				String text = TextExtractor.newOdfTextExtractor(odfElement)
						.getText();
				if (text != null) {
					result.addField(ParserFieldEnum.content, text);
					result.langDetection(10000, ParserFieldEnum.content);
				}
			}
		} catch (IOException e) {
			Logging.error(e.getMessage(), e);
		} catch (Exception e) {
			Logging.error(e.getMessage(), e);
		} finally {
			if (document != null)
				document.close();
		}
	}
}
