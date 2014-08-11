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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFMergerUtility;

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
import com.jaeksoft.searchlib.util.ExecuteUtils.ExecutionException;
import com.jaeksoft.searchlib.util.GhostScript;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.PdfCrack;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.pdfbox.PDFBoxUtils;
import com.jaeksoft.searchlib.util.pdfbox.PDFBoxUtils.TolerantPDFTextStripper;

public class PdfParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.producer, ParserFieldEnum.keywords,
			ParserFieldEnum.creation_date, ParserFieldEnum.modification_date,
			ParserFieldEnum.language, ParserFieldEnum.number_of_pages,
			ParserFieldEnum.ocr_content, ParserFieldEnum.image_ocr_boxes,
			ParserFieldEnum.pdfcrack_password };

	public PdfParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
		addProperty(ClassPropertyEnum.GHOSTSCRIPT_BINARYPATH, "", null, 50, 1);
		addProperty(ClassPropertyEnum.PDFCRACK_COMMANDLINE, "", null, 50, 1);
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

	private void extractMetaData(ParserResultItem result, PDDocument pdf)
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
		int pages = pdf.getNumberOfPages();
		result.addField(ParserFieldEnum.number_of_pages, pages);
		PDDocumentCatalog catalog = pdf.getDocumentCatalog();
		if (catalog != null) {
			result.addField(ParserFieldEnum.language, catalog.getLanguage());
		}
	}

	private int addLine(ParserResultItem result, String line) {
		if (line == null)
			return 0;
		line = StringUtils.replaceConsecutiveSpaces(line, " ").trim();
		int l = line.length();
		if (l == 0)
			return 0;
		result.addField(ParserFieldEnum.content, line);
		return line.length();
	}

	/**
	 * Extract text content using PDFBox
	 * 
	 * @param result
	 * @param pdf
	 * @throws IOException
	 */
	private int extractTextContent(ParserResultItem result, PDDocument pdf)
			throws IOException {
		TolerantPDFTextStripper stripper = new TolerantPDFTextStripper();
		String text = stripper.getText(pdf);
		if (StringUtils.isEmpty(text))
			return 0;
		String[] lines = StringUtils.splitLines(text);
		int characterCount = 0;
		for (String line : lines)
			characterCount += addLine(result, line);
		return characterCount;
	}

	/**
	 * Extract text content using Ghostscript
	 * 
	 * @param result
	 * @param ghostScript
	 * @param pdfFile
	 * @param pdfPassword
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private int extractTextContent(ParserResultItem result,
			GhostScript ghostScript, File pdfFile, String pdfPassword)
			throws IOException, InterruptedException {
		File textFile = null;
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		try {
			textFile = File.createTempFile("oss_pdfparser", "txt");
			ghostScript.extractText(pdfPassword, pdfFile, textFile);
			fileReader = new FileReader(textFile);
			bufferedReader = new BufferedReader(fileReader);
			int characterCount = 0;
			String line;
			while ((line = bufferedReader.readLine()) != null)
				characterCount += addLine(result, line);
			return characterCount;
		} catch (ExecutionException e) {
			Logging.warn("Ghostscript returned: " + e.getReturnedText());
			throw e;
		} finally {
			IOUtils.close(bufferedReader, fileReader);
			if (textFile != null)
				if (textFile.exists())
					textFile.delete();
		}
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		PDDocument pdf = null;
		String fileName = null;
		String password = null;
		try {
			String ghostScriptBinaryPath = getStringProperty(ClassPropertyEnum.GHOSTSCRIPT_BINARYPATH);
			GhostScript ghostScript = StringUtils
					.isEmpty(ghostScriptBinaryPath) ? null : new GhostScript(
					ghostScriptBinaryPath);
			fileName = streamLimiter.getFile().getName();
			File pdfFile = streamLimiter.getFile();
			pdf = PDDocument.loadNonSeq(pdfFile, null);
			if (pdf.isEncrypted()) {
				String pdfCrackCommandLine = getStringProperty(ClassPropertyEnum.PDFCRACK_COMMANDLINE);
				if (!StringUtils.isEmpty(pdfCrackCommandLine))
					password = PdfCrack.findPassword(pdfCrackCommandLine,
							streamLimiter.getFile());
				if (password == null)
					throw new IOException("Encrypted PDF.");
				pdf.openProtection(new StandardDecryptionMaterial(password));
			}
			ParserResultItem result = getNewParserResultItem();
			result.addField(ParserFieldEnum.pdfcrack_password, password);
			extractMetaData(result, pdf);
			int charCount;
			if (ghostScript == null)
				charCount = extractTextContent(result, pdf);
			else
				charCount = extractTextContent(result, ghostScript, pdfFile,
						password);
			if (charCount == 0)
				extractImagesForOCR(result, pdf, lang, ghostScript, pdfFile,
						password);
			result.langDetection(10000, ParserFieldEnum.content);
		} catch (SearchLibException e) {
			throw new IOException("Failed on " + fileName, e);
		} catch (InterruptedException e) {
			throw new IOException("Failed on " + fileName, e);
		} catch (BadSecurityHandlerException e) {
			throw new IOException("Failed on " + fileName, e);
		} catch (CryptographyException e) {
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
			hocrFile = File.createTempFile("ossocr",
					"." + ocr.getHocrFileExtension());
			ocr.ocerizeImage(image, hocrFile, lang, true);
			if (hocrFile.length() == 0)
				return null;
			return new HocrDocument(hocrFile);
		} finally {
			if (hocrFile != null)
				FileUtils.deleteQuietly(hocrFile);
		}
	}

	private HocrDocument doOcr(OcrManager ocr, LanguageEnum lang, File imageFile)
			throws IOException, InterruptedException, SearchLibException {
		File hocrFile = null;
		try {
			hocrFile = File.createTempFile("ossocr",
					"." + ocr.getHocrFileExtension());
			ocr.ocerize(imageFile, hocrFile, lang, true);
			if (hocrFile.length() == 0)
				return null;
			return new HocrDocument(hocrFile);
		} finally {
			if (hocrFile != null)
				FileUtils.deleteQuietly(hocrFile);
		}
	}

	private void ocrImageGhostcript(OcrManager ocr, HocrPdf hocrPdf,
			ParserResultItem result, GhostScript ghostScript, File pdfFile,
			String pdfPassword, LanguageEnum lang, int page)
			throws IOException, InterruptedException, SearchLibException {
		File imageFile = null;
		try {
			imageFile = File.createTempFile("oss_pdfparser", ".png");
			ghostScript.generateImage(pdfPassword, page, pdfFile, 300,
					imageFile);
			Dimension dimension = ImageUtils.getDimensions(imageFile);
			HocrPage hocrPage = hocrPdf.createPage(page - 1, dimension.width,
					dimension.height);
			hocrPage.addImage(doOcr(ocr, lang, imageFile));
		} finally {
			if (imageFile != null)
				if (imageFile.exists())
					imageFile.delete();
		}
	}

	private void extractImagesForOCR(ParserResultItem result, PDDocument pdf,
			LanguageEnum lang, GhostScript ghostScript, File pdfFile,
			String pdfPassword) throws SearchLibException, IOException,
			InterruptedException {
		OcrManager ocr = ClientCatalog.getOcrManager();
		if (ocr == null || ocr.isDisabled())
			return;
		if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content)
				&& !getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
			return;
		HocrPdf hocrPdf = new HocrPdf();
		List<?> pages = pdf.getDocumentCatalog().getAllPages();
		Iterator<?> iter = pages.iterator();
		int currentPage = 0;
		int emptyPageImages = 0;
		while (iter.hasNext()) {
			currentPage++;
			PDPage page = (PDPage) iter.next();
			if (PDFBoxUtils.countCheckImage(page) == 0)
				continue;
			if (ghostScript == null) {
				BufferedImage image = page.convertToImage(
						BufferedImage.TYPE_INT_BGR, 300);
				if (ImageUtils.checkIfManyColors(image)) {
					HocrPage hocrPage = hocrPdf.createPage(currentPage - 1,
							image.getWidth(), image.getHeight());
					hocrPage.addImage(doOcr(ocr, lang, image));
				} else
					emptyPageImages++;
			} else {
				ocrImageGhostcript(ocr, hocrPdf, result, ghostScript, pdfFile,
						pdfPassword, lang, currentPage);
			}
		}
		if (currentPage > 0 && emptyPageImages == currentPage)
			throw new SearchLibException("All pages are blank " + currentPage);

		if (getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
			hocrPdf.putHocrToParserField(result,
					ParserFieldEnum.image_ocr_boxes);
		if (getFieldMap().isMapped(ParserFieldEnum.ocr_content))
			hocrPdf.putTextToParserField(result, ParserFieldEnum.ocr_content);

	}

	@Override
	public void mergeFiles(File fileDir, File destFile)
			throws SearchLibException {
		PDFMergerUtility pdfMerger = new PDFMergerUtility();
		File[] files = new LastModifiedFileComparator().sort(fileDir
				.listFiles());
		for (File file : files) {
			String ext = FilenameUtils.getExtension(file.getName());
			if (!"pdf".equalsIgnoreCase(ext))
				continue;
			pdfMerger.addSource(file);
		}
		if (destFile.exists())
			destFile.delete();
		pdfMerger.setDestinationFileName(destFile.getAbsolutePath());
		try {
			pdfMerger.mergeDocuments();
		} catch (COSVisitorException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}
}
