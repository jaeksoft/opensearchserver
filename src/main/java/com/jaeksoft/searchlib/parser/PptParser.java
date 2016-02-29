/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.StringUtils;

public class PptParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "application/vnd.ms-powerpoint" };

	public static final String[] DEFAULT_EXTENSIONS = { "ppt" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name, ParserFieldEnum.title, ParserFieldEnum.note,
			ParserFieldEnum.body, ParserFieldEnum.other };

	public PptParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) throws IOException {

		HSLFSlideShow ppt = new HSLFSlideShow(streamLimiter.getNewInputStream());
		List<HSLFSlide> slides = ppt.getSlides();
		ParserResultItem result = getNewParserResultItem();
		for (HSLFSlide slide : slides) {
			List<List<HSLFTextParagraph>> textLevel0 = slide.getTextParagraphs();
			for (List<HSLFTextParagraph> textLevel1 : textLevel0) {
				for (HSLFTextParagraph textPara : textLevel1) {
					ParserFieldEnum field;
					switch (textPara.getRunType()) {
					case TextHeaderAtom.TITLE_TYPE:
					case TextHeaderAtom.CENTER_TITLE_TYPE:
						field = ParserFieldEnum.title;
						break;
					case TextHeaderAtom.NOTES_TYPE:
						field = ParserFieldEnum.note;
						break;
					case TextHeaderAtom.BODY_TYPE:
					case TextHeaderAtom.CENTRE_BODY_TYPE:
					case TextHeaderAtom.HALF_BODY_TYPE:
					case TextHeaderAtom.QUARTER_BODY_TYPE:
						field = ParserFieldEnum.body;
						break;
					case TextHeaderAtom.OTHER_TYPE:
					default:
						field = ParserFieldEnum.other;
						break;
					}
					StringBuilder sb = new StringBuilder();
					for (HSLFTextRun textRun : textPara.getTextRuns()) {
						sb.append(textRun.getRawText());
						sb.append(' ');
					}
					result.addField(field, StringUtils.replaceConsecutiveSpaces(sb.toString(), " "));
				}
			}
		}
		result.langDetection(10000, ParserFieldEnum.body);
	}

}
