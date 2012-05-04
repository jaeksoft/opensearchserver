/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class ImageParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.ocr_content };

	public ImageParser() {
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

		File textFile = null;
		try {
			OcrManager ocr = ClientCatalog.getOcrManager();
			if (ocr == null || ocr.isDisabled())
				return;
			if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content))
				return;
			File imageFile = streamLimiter.getFile();
			textFile = File.createTempFile("ossocr", ".txt");
			ocr.ocerize(imageFile, textFile, lang);
			addField(ParserFieldEnum.ocr_content,
					FileUtils.readFileToString(textFile, "UTF-8"));
		} catch (SearchLibException e) {
			throw new IOException(e);
		} finally {
			if (textFile != null)
				FileUtils.deleteQuietly(textFile);
		}

	}

}
