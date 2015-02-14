/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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

import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;

import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class ParserTypeEnum extends ExtensibleEnum<ParserType> {

	private final MultivaluedHashMap<String, ParserType> mimeTypesMap;
	private final MultivaluedHashMap<String, ParserType> extensionsMap;

	private ParserTypeEnum() {
		new ParserType(this, "Audio", AudioParser.class,
				AudioParser.DEFAULT_MIMETYPES, AudioParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "DOC", DocParser.class,
				DocParser.DEFAULT_MIMETYPES, DocParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "DOCX", DocxParser.class,
				DocxParser.DEFAULT_MIMETYPES, DocxParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "EML", EmlParser.class,
				EmlParser.DEFAULT_MIMETYPES, EmlParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "File system", FileSystemParser.class, null, null);
		new ParserType(this, "HTML", HtmlParser.class,
				HtmlParser.DEFAULT_MIMETYPES, HtmlParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "Image", ImageParser.class,
				ImageParser.DEFAULT_MIMETYPES, ImageParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "ODS (OpenOffice spreadsheet)", OdsParser.class,
				OdsParser.DEFAULT_MIMETYPES, OdsParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "ODT (OpenOffice text file)", OdtParser.class,
				OdtParser.DEFAULT_MIMETYPES, OdtParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "ODP (OpenOffice presentation)", OdpParser.class,
				OdpParser.DEFAULT_MIMETYPES, OdpParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "MAPI Msg (Outlook message)", MapiMsgParser.class,
				MapiMsgParser.DEFAULT_MIMETYPES,
				MapiMsgParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "PDF (Pdfbox)", PdfParser.class,
				PdfParser.DEFAULT_MIMETYPES, PdfParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "PDF (IcePdf)", IcePdfParser.class, null, null);
		new ParserType(this, "PPT", PptParser.class,
				PptParser.DEFAULT_MIMETYPES, PptParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "PPTX", PptxParser.class,
				PptxParser.DEFAULT_MIMETYPES, PptxParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "PUB", PublisherParser.class,
				PublisherParser.DEFAULT_MIMETYPES,
				PublisherParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "RSS", RssParser.class,
				RssParser.DEFAULT_MIMETYPES, RssParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "RTF", RtfParser.class,
				RtfParser.DEFAULT_MIMETYPES, RtfParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "Text", TextParser.class,
				TextParser.DEFAULT_MIMETYPES, TextParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "Torrent", TorrentParser.class,
				TorrentParser.DEFAULT_MIMETYPES,
				TorrentParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "VSD", VisioParser.class,
				VisioParser.DEFAULT_MIMETYPES, VisioParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "XLS", XlsParser.class,
				XlsParser.DEFAULT_MIMETYPES, XlsParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "XLSX", XlsxParser.class,
				XlsParser.DEFAULT_MIMETYPES, XlsParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "XML", XmlParser.class,
				XmlParser.DEFAULT_MIMETYPES, XmlParser.DEFAULT_EXTENSIONS);
		new ParserType(this, "XML (XPATH)", XmlXPathParser.class, null, null);

		mimeTypesMap = new MultivaluedHashMap<String, ParserType>();
		extensionsMap = new MultivaluedHashMap<String, ParserType>();
		for (ParserType parserType : getList()) {
			String[] mimeTypes = parserType.getDefaultMimeTypes();
			if (mimeTypes != null)
				for (String mimeType : mimeTypes)
					mimeTypesMap.add(mimeType.intern(), parserType);
			String[] extensions = parserType.getDefaultExtensions();
			if (extensions != null)
				for (String extension : extensions)
					extensionsMap.add(extension.intern(), parserType);
		}
	}

	public ParserType find(Class<? extends ParserFactory> classRef) {
		for (ParserType pt : this.getList())
			if (pt.getParserClass() == classRef)
				return pt;
		return null;
	}

	public ParserType findByName(String name) {
		for (ParserType pt : this.getList())
			if (pt.simpleName.equalsIgnoreCase(name))
				return pt;
		return null;
	}

	public List<ParserType> findByExtension(String extension) {
		if (extension == null)
			return null;
		return extensionsMap.get(extension.intern());
	}

	public ParserType findByExtensionFirst(String extension) {
		if (extension == null)
			return null;
		return extensionsMap.getFirst(extension.intern());
	}

	public List<ParserType> findByMimeType(String mimeType) {
		if (mimeType == null)
			return null;
		return mimeTypesMap.get(mimeType.intern());
	}

	public ParserType findByMimeTypeFirst(String mimeType) {
		if (mimeType == null)
			return null;
		return mimeTypesMap.getFirst(mimeType.intern());
	}

	public final static ParserTypeEnum INSTANCE = new ParserTypeEnum();

}
