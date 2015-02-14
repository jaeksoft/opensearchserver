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
import java.io.InputStream;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class DocParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "application/msword" };

	public static final String[] DEFAULT_EXTENSIONS = { "doc", "dot" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.lang };

	public DocParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	private void currentWordExtraction(ParserResultItem result,
			InputStream inputStream) throws IOException {
		WordExtractor word = null;

		try {
			word = new WordExtractor(inputStream);

			SummaryInformation info = word.getSummaryInformation();
			if (info != null) {
				result.addField(ParserFieldEnum.title, info.getTitle());
				result.addField(ParserFieldEnum.author, info.getAuthor());
				result.addField(ParserFieldEnum.subject, info.getSubject());
			}

			String[] paragraphes = word.getParagraphText();
			for (String paragraph : paragraphes) {
				String[] frags = paragraph.split("\\n");
				for (String frag : frags)
					result.addField(ParserFieldEnum.content,
							StringUtils.replaceConsecutiveSpaces(frag, " "));
			}
		} finally {
			IOUtils.close(word);
		}
	}

	private void oldWordExtraction(ParserResultItem result,
			InputStream inputStream) throws IOException {
		Word6Extractor word6 = null;
		try {
			word6 = new Word6Extractor(inputStream);
			SummaryInformation si = word6.getSummaryInformation();
			if (si != null) {
				result.addField(ParserFieldEnum.title, si.getTitle());
				result.addField(ParserFieldEnum.author, si.getAuthor());
				result.addField(ParserFieldEnum.subject, si.getSubject());
			}

			String text = word6.getText();
			String[] frags = text.split("\\n");
			for (String frag : frags)
				result.addField(ParserFieldEnum.content,
						StringUtils.replaceConsecutiveSpaces(frag, " "));
		} finally {
			IOUtils.close(word6);
		}
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		ParserResultItem result = getNewParserResultItem();
		try {
			try {
				currentWordExtraction(result, streamLimiter.getNewInputStream());
			} catch (OldWordFileFormatException e) {
				oldWordExtraction(result, streamLimiter.getNewInputStream());
			}
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			Logging.warn("POI 3.7 bug (exception catched)");
			Logging.warn(e);
		}
		result.langDetection(10000, ParserFieldEnum.content);
	}

}
