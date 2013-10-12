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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.ocr.HocrDocument;
import com.jaeksoft.searchlib.ocr.HocrPdf;
import com.jaeksoft.searchlib.ocr.HocrPdf.HocrPage;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class PdfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.producer, ParserFieldEnum.keywords,
			ParserFieldEnum.creation_date, ParserFieldEnum.modification_date,
			ParserFieldEnum.language, ParserFieldEnum.number_of_pages,
			ParserFieldEnum.ocr_content, ParserFieldEnum.image_ocr_boxes };

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

	private void extractContent(ParserResultItem result, PDDocument pdf)
			throws IOException {
		PDDocumentInformation info = pdf.getDocumentInformation();
		if (info != null) {
			result.addField(ParserFieldEnum.title, info.getTitle());
			result.addField(ParserFieldEnum.subject, info.getSubject());
			result.addField(ParserFieldEnum.author, info.getAuthor());
			result.addField(ParserFieldEnum.producer, info.getProducer());
			result.addField(ParserFieldEnum.keywords, info.getKeywords());
			String d = getDate(getCreationDate(info));
			if (d != null)
				result.addField(ParserFieldEnum.creation_date, d);
			d = getDate(getModificationDate(info));
			if (d != null)
				result.addField(ParserFieldEnum.modification_date, d);
		}
		PDDocumentCatalog catalog = pdf.getDocumentCatalog();
		if (catalog != null) {
			result.addField(ParserFieldEnum.language, catalog.getLanguage());
		}
		int pages = pdf.getNumberOfPages();
		result.addField(ParserFieldEnum.number_of_pages, pages);
		TolerantPDFTextStripper stripper = new TolerantPDFTextStripper();
		String text = stripper.getText(pdf);
		String[] frags = text.split("\\n");
		for (String frag : frags)
			result.addField(ParserFieldEnum.content, StringUtils
					.replaceConsecutiveSpaces(frag, " ").trim());
		result.langDetection(10000, ParserFieldEnum.content);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		PDDocument pdf = null;
		String fileName = null;
		try {
			fileName = streamLimiter.getFile().getName();
			pdf = PDDocument.load(streamLimiter.getFile());
			if (pdf.isEncrypted())
				throw new IOException("Encrypted PDF.");
			ParserResultItem result = getNewParserResultItem();
			extractContent(result, pdf);
			extractImagesForOCR(result, pdf, lang);
		} catch (SearchLibException e) {
			throw new IOException("Failed on " + fileName, e);
		} catch (InterruptedException e) {
			throw new IOException("Failed on " + fileName, e);
		} finally {
			if (pdf != null)
				pdf.close();
		}
	}

	private HocrDocument doOcr(OcrManager ocr, LanguageEnum lang,
			BufferedImage image) throws IOException, InterruptedException,
			SearchLibException {
		File hocrFile = null;
		try {
			hocrFile = File.createTempFile("ossocr", ".html");
			ocr.ocerizeImage(image, hocrFile, lang, true);
			return new HocrDocument(hocrFile);
		} finally {
			if (hocrFile != null)
				FileUtils.deleteQuietly(hocrFile);
		}
	}

	private int countCheckImage(PDPage page) throws IOException {
		PDResources resources = page.getResources();
		Map<String, PDXObjectImage> images = resources.getImages();
		if (images == null)
			return 0;
		int count = 0;
		for (PDXObjectImage image : images.values())
			if (image.getRGBImage() == null)
				Logging.warn("RGB image is null");
			else
				count++;
		return count;
	}

	private void extractImagesForOCR(ParserResultItem result, PDDocument pdf,
			LanguageEnum lang) throws IOException, SearchLibException,
			InterruptedException {
		OcrManager ocr = ClientCatalog.getOcrManager();
		if (ocr == null || ocr.isDisabled())
			return;
		if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content)
				&& !getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
			return;
		List<?> pages = pdf.getDocumentCatalog().getAllPages();
		Iterator<?> iter = pages.iterator();
		HocrPdf hocrPdf = new HocrPdf();
		int currentPage = 0;
		int emptyPageImages = 0;
		while (iter.hasNext()) {
			currentPage++;
			PDPage page = (PDPage) iter.next();
			if (countCheckImage(page) == 0)
				continue;
			BufferedImage image = page.convertToImage(
					BufferedImage.TYPE_INT_BGR, 300);
			if (ImageUtils.checkIfManyColors(image)) {
				HocrPage hocrPage = hocrPdf.createPage(currentPage - 1,
						image.getWidth(), image.getHeight());
				hocrPage.addImage(doOcr(ocr, lang, image));
			} else
				emptyPageImages++;
		}
		if (currentPage > 0 && emptyPageImages == currentPage)
			throw new SearchLibException("All pages are blank " + currentPage);
		if (getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
			hocrPdf.putHocrToParserField(result,
					ParserFieldEnum.image_ocr_boxes);
		if (getFieldMap().isMapped(ParserFieldEnum.ocr_content))
			hocrPdf.putTextToParserField(result, ParserFieldEnum.ocr_content);
	}
}
