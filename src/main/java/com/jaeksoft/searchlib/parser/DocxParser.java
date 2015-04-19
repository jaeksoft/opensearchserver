/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class DocxParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = {
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.template" };

	public static final String[] DEFAULT_EXTENSIONS = { "docx", "dotx" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.creator,
			ParserFieldEnum.subject, ParserFieldEnum.description,
			ParserFieldEnum.content, ParserFieldEnum.lang,
			ParserFieldEnum.lang_method };

	public DocxParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		ParserResultItem result = getNewParserResultItem();

		XWPFDocument document = new XWPFDocument(
				streamLimiter.getNewInputStream());
		XWPFWordExtractor word = null;
		try {
			word = new XWPFWordExtractor(document);

			CoreProperties info = word.getCoreProperties();
			if (info != null) {
				result.addField(ParserFieldEnum.title, info.getTitle());
				result.addField(ParserFieldEnum.creator, info.getCreator());
				result.addField(ParserFieldEnum.subject, info.getSubject());
				result.addField(ParserFieldEnum.description,
						info.getDescription());
				result.addField(ParserFieldEnum.keywords, info.getKeywords());
			}

			String content = word.getText();
			result.addField(ParserFieldEnum.content,
					StringUtils.replaceConsecutiveSpaces(content, " "));

			result.langDetection(10000, ParserFieldEnum.content);
		} finally {
			IOUtils.close(word);
		}
	}
}
