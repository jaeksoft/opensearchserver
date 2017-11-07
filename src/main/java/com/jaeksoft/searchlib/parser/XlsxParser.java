/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;

public class XlsxParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES =
			{ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };

	public static final String[] DEFAULT_EXTENSIONS = { "xlsx" };

	// The default maximum size of extracted text
	private static long MAX_TEXT_SIZE = 100 * 1024 * 1024;

	static {
		ZipSecureFile.setMaxTextSize(MAX_TEXT_SIZE);
	}

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title,
			ParserFieldEnum.creator,
			ParserFieldEnum.subject,
			ParserFieldEnum.description,
			ParserFieldEnum.content,
			ParserFieldEnum.lang,
			ParserFieldEnum.lang_method };

	public XlsxParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook(streamLimiter.getNewInputStream());
		XSSFExcelExtractor excelExtractor = null;
		try {
			excelExtractor = new XSSFExcelExtractor(workbook);
			ParserResultItem result = getNewParserResultItem();

			CoreProperties info = excelExtractor.getCoreProperties();
			if (info != null) {
				result.addField(ParserFieldEnum.title, info.getTitle());
				result.addField(ParserFieldEnum.creator, info.getCreator());
				result.addField(ParserFieldEnum.subject, info.getSubject());
				result.addField(ParserFieldEnum.description, info.getDescription());
				result.addField(ParserFieldEnum.keywords, info.getKeywords());
			}

			excelExtractor.setIncludeCellComments(true);
			excelExtractor.setIncludeHeadersFooters(true);
			excelExtractor.setIncludeSheetNames(true);
			String content = excelExtractor.getText();
			result.addField(ParserFieldEnum.content, StringUtils.replaceConsecutiveSpaces(content, " "));

			result.langDetection(10000, ParserFieldEnum.content);
		} finally {
			IOUtils.close(excelExtractor);
		}

	}
}
