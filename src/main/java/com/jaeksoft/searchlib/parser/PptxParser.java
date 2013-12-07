/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.xmlbeans.XmlException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class PptxParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.creator,
			ParserFieldEnum.subject, ParserFieldEnum.description,
			ParserFieldEnum.content, ParserFieldEnum.lang,
			ParserFieldEnum.lang_method };

	public PptxParser() {
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

		// TODO Optimise if it is already a file
		File tempFile = File.createTempFile("oss", ".pptx");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempFile);
			IOUtils.copy(streamLimiter.getNewInputStream(), fos);
			fos.close();
		} catch (IOException e) {
			IOUtils.close(fos);
			throw e;
		}

		XSLFPowerPointExtractor poiExtractor = null;
		try {
			XSLFSlideShow pptSlideShow = new XSLFSlideShow(
					tempFile.getAbsolutePath());
			poiExtractor = new XSLFPowerPointExtractor(pptSlideShow);

			ParserResultItem result = getNewParserResultItem();
			CoreProperties info = poiExtractor.getCoreProperties();
			if (info != null) {
				result.addField(ParserFieldEnum.title, info.getTitle());
				result.addField(ParserFieldEnum.creator, info.getCreator());
				result.addField(ParserFieldEnum.subject, info.getSubject());
				result.addField(ParserFieldEnum.description,
						info.getDescription());
				result.addField(ParserFieldEnum.keywords, info.getKeywords());
			}

			String content = poiExtractor.getText(true, true);
			result.addField(ParserFieldEnum.content,
					StringUtils.replaceConsecutiveSpaces(content, " "));

			result.langDetection(10000, ParserFieldEnum.content);

		} catch (OpenXML4JException e) {
			throw new IOException(e);
		} catch (XmlException e) {
			throw new IOException(e);
		} finally {
			IOUtils.close(poiExtractor);
		}

	}

}
