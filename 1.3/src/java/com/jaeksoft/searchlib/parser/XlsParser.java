/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.StringUtils;

public class XlsParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.lang };

	public XlsParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		HSSFWorkbook workbook = new HSSFWorkbook(
				streamLimiter.getNewInputStream());
		ExcelExtractor excel = new ExcelExtractor(workbook);

		SummaryInformation info = excel.getSummaryInformation();
		if (info != null) {
			addField(ParserFieldEnum.title, info.getTitle());
			addField(ParserFieldEnum.author, info.getAuthor());
			addField(ParserFieldEnum.subject, info.getSubject());
		}

		String content = excel.getText();
		addField(ParserFieldEnum.content,
				StringUtils.replaceConsecutiveSpaces(content, " "));

		langDetection(10000, ParserFieldEnum.content);

	}

}
