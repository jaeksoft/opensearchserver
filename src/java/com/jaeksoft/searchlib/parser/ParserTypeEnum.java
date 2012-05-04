/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class ParserTypeEnum extends ExtensibleEnum<ParserType> {

	public ParserTypeEnum() {
		new ParserType(this, "Audio", AudioParser.class);
		new ParserType(this, "DOC", DocParser.class);
		new ParserType(this, "DOCX", DocxParser.class);
		new ParserType(this, "File system", FileSystemParser.class);
		new ParserType(this, "HTML", HtmlParser.class);
		new ParserType(this, "Image", ImageParser.class);
		new ParserType(this, "ODS (OpenOffice spreadsheet)", OdsParser.class);
		new ParserType(this, "ODT (OpenOffice text file)", OdtParser.class);
		new ParserType(this, "PDF", PdfParser.class);
		new ParserType(this, "PPT", PptParser.class);
		new ParserType(this, "PPTX", PptxParser.class);
		new ParserType(this, "RTF", RtfParser.class);
		new ParserType(this, "Text", TextParser.class);
		new ParserType(this, "Torrent", TorrentParser.class);
		new ParserType(this, "XLS", XlsParser.class);
		new ParserType(this, "XLSX", XlsxParser.class);
	}

	public ParserType find(Class<? extends ParserFactory> classRef) {
		for (ParserType pt : this.getList())
			if (pt.getParserClass() == classRef)
				return pt;
		return null;
	}
}
