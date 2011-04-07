/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.creator, ParserFieldEnum.subject,
			ParserFieldEnum.description, ParserFieldEnum.content,
			ParserFieldEnum.lang, ParserFieldEnum.lang_method,
			ParserFieldEnum.filename, ParserFieldEnum.content_type };

	public XlsxParser() {
		super(fl);
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		XSSFExcelExtractor excelExtractor = new XSSFExcelExtractor(workbook);

		CoreProperties info = excelExtractor.getCoreProperties();
		if (info != null) {
			addField(ParserFieldEnum.title, info.getTitle());
			addField(ParserFieldEnum.creator, info.getCreator());
			addField(ParserFieldEnum.subject, info.getSubject());
			addField(ParserFieldEnum.description, info.getDescription());
			addField(ParserFieldEnum.keywords, info.getKeywords());
		}

		excelExtractor.setIncludeCellComments(true);
		excelExtractor.setIncludeHeadersFooters(true);
		excelExtractor.setIncludeSheetNames(true);
		String content = excelExtractor.getText();
		addField(ParserFieldEnum.content, content.replaceAll("\\s+", " "));

		langDetection(10000, ParserFieldEnum.content);

	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}

}
