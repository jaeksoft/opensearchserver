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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PInfo;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class IcePdfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.creator,
			ParserFieldEnum.content, ParserFieldEnum.producer,
			ParserFieldEnum.keywords, ParserFieldEnum.creation_date,
			ParserFieldEnum.modification_date, ParserFieldEnum.language,
			ParserFieldEnum.number_of_pages, ParserFieldEnum.ocr_content };

	public IcePdfParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
		// addProperty(ClassPropertyEnum.OCR_PDF_METHOD,
		// ClassPropertyEnum.OCR_PDF_METHODS[0],
		// ClassPropertyEnum.OCR_PDF_METHODS);
	}

	private void extractContent(Document pdf) throws IOException {
		PInfo info = pdf.getInfo();
		if (info != null) {
			addField(ParserFieldEnum.title, info.getTitle());
			addField(ParserFieldEnum.subject, info.getSubject());
			addField(ParserFieldEnum.author, info.getAuthor());
			addField(ParserFieldEnum.producer, info.getProducer());
			addField(ParserFieldEnum.keywords, info.getKeywords());
			addField(ParserFieldEnum.creator, info.getCreator());
			addField(ParserFieldEnum.creation_date, info.getCreationDate());
			addField(ParserFieldEnum.modification_date, info.getModDate());
		}

		int pages = pdf.getNumberOfPages();
		addField(ParserFieldEnum.number_of_pages, pages);

		int pagNumber = 0;
		PageText pageText = pdf.getPageText(pagNumber);
		if (pageText != null && pageText.getPageLines() != null) {
			List<LineText> lineTextArray = pageText.getPageLines();
			if (lineTextArray != null)
				for (LineText lineText : lineTextArray)
					addField(ParserFieldEnum.content, StringUtils
							.replaceConsecutiveSpaces(lineText.toString(), " ")
							.trim());
		}

		langDetection(10000, ParserFieldEnum.content);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		Document pdf = null;
		try {
			// boolean ocrFullPage = ClassPropertyEnum.OCR_PDF_METHODS[1]
			// .equals(getProperty(ClassPropertyEnum.OCR_PDF_METHOD)
			// .getValue());
			pdf = new Document();
			pdf.setInputStream(streamLimiter.getNewInputStream(), null);
			extractContent(pdf);
			extractImagesForOCR(pdf, lang);
		} catch (SearchLibException e) {
			throw new IOException(e);
		} catch (PDFException e) {
			throw new IOException(e);
		} catch (PDFSecurityException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} finally {
			if (pdf != null)
				pdf.dispose();
		}
	}

	private void imageOcr(Image image, float rotation, LanguageEnum lang,
			OcrManager ocr) throws InterruptedException, IOException,
			SearchLibException {
		BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
		if (rotation != 0)
			bufferedImage = ImageUtils.rotate(bufferedImage, rotation);
		String ocr_content = ocr.ocerizeImage(bufferedImage, lang);
		if (ocr_content != null)
			addField(ParserFieldEnum.ocr_content, ocr_content);
	}

	private void extractImagesForOCR(Document pdf, LanguageEnum lang)
			throws IOException, SearchLibException, InterruptedException {
		OcrManager ocr = ClientCatalog.getOcrManager();
		if (ocr == null || ocr.isDisabled())
			return;
		if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content))
			return;

		for (int i = 0; i < pdf.getNumberOfPages(); i++) {
			@SuppressWarnings("unchecked")
			Vector<Image> images = pdf.getPageImages(i);
			if (images == null || images.size() == 0)
				continue;
			float rotation = pdf.getPageTree().getPage(i, null)
					.getTotalRotation(0);
			for (Image image : images)
				imageOcr(image, 360 - rotation, lang, ocr);
		}

	}
}
