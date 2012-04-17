/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.StringUtils;

public class PdfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.title,
			ParserFieldEnum.author, ParserFieldEnum.subject,
			ParserFieldEnum.content, ParserFieldEnum.producer,
			ParserFieldEnum.keywords, ParserFieldEnum.creation_date,
			ParserFieldEnum.modification_date, ParserFieldEnum.language,
			ParserFieldEnum.number_of_pages, ParserFieldEnum.ocr_content };

	public PdfParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	private Calendar getCreationDate(PDDocumentInformation pdfInfo) {
		try {
			return pdfInfo.getCreationDate();
		} catch (IOException e) {
			Logging.warn(e);
			return null;
		}
	}

	private Calendar getModificationDate(PDDocumentInformation pdfInfo) {
		try {
			return pdfInfo.getCreationDate();
		} catch (IOException e) {
			Logging.warn(e);
			return null;
		}
	}

	private String getDate(Calendar cal) {
		if (cal == null)
			return null;
		Date time = cal.getTime();
		if (time == null)
			return null;
		return time.toString();
	}

	private void extractContent(PDDocument pdf) throws IOException {
		PDDocumentInformation info = pdf.getDocumentInformation();
		if (info != null) {
			addField(ParserFieldEnum.title, info.getTitle());
			addField(ParserFieldEnum.subject, info.getSubject());
			addField(ParserFieldEnum.author, info.getAuthor());
			addField(ParserFieldEnum.producer, info.getProducer());
			addField(ParserFieldEnum.keywords, info.getKeywords());
			String d = getDate(getCreationDate(info));
			if (d != null)
				addField(ParserFieldEnum.creation_date, d);
			d = getDate(getModificationDate(info));
			if (d != null)
				addField(ParserFieldEnum.modification_date, d);
		}
		PDDocumentCatalog catalog = pdf.getDocumentCatalog();
		if (catalog != null) {
			addField(ParserFieldEnum.language, catalog.getLanguage());
		}
		int pages = pdf.getNumberOfPages();
		addField(ParserFieldEnum.number_of_pages, pages);
		PDFTextStripper stripper = new PDFTextStripper("UTF-8");
		String text = stripper.getText(pdf);
		String[] frags = text.split("\\n");
		for (String frag : frags)
			addField(ParserFieldEnum.content, StringUtils
					.replaceConsecutiveSpaces(frag, " ").trim());
		langDetection(10000, ParserFieldEnum.content);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		PDDocument pdf = null;
		RandomAccessFile raf = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("oss", ".pdf");
			raf = new RandomAccessFile(tempFile, "rw");
			pdf = PDDocument.load(streamLimiter.getNewInputStream(), raf, true);
			if (pdf.isEncrypted())
				throw new IOException("Encrypted PDF.");
			extractContent(pdf);
			extractImagesForOCR(pdf, lang);
		} catch (SearchLibException e) {
			throw new IOException(e);
		} finally {
			if (pdf != null)
				pdf.close();
			if (raf != null)
				raf.close();
			if (tempFile != null)
				tempFile.delete();
		}
	}

	private void extractImagesForOCR(PDDocument pdf, LanguageEnum lang)
			throws IOException, SearchLibException {
		OcrManager ocr = ClientCatalog.getOcrManager();
		if (ocr == null || ocr.isDisabled())
			return;
		if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content))
			return;
		List<?> pages = pdf.getDocumentCatalog().getAllPages();
		Iterator<?> iter = pages.iterator();
		while (iter.hasNext()) {
			PDPage page = (PDPage) iter.next();
			PDResources resources = page.getResources();
			Map<?, ?> images = resources.getImages();
			if (images != null) {
				Iterator<?> imageIter = images.keySet().iterator();
				while (imageIter.hasNext()) {
					String key = (String) imageIter.next();
					PDXObjectImage image = (PDXObjectImage) images.get(key);
					File imageFile = File.createTempFile("osspdfimg",
							'.' + image.getSuffix());
					File textFile = File.createTempFile("ossocr", ".txt");
					image.write2file(imageFile);
					ocr.ocerize(imageFile, textFile, lang);
					addField(ParserFieldEnum.ocr_content,
							FileUtils.readFileToString(textFile, "UTF-8"));
					imageFile.delete();
					textFile.delete();
				}
			}
		}
	}
}
