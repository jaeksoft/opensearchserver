/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.pdfbox.PDFBoxUtils;
import com.jaeksoft.searchlib.util.pdfbox.PDFBoxUtils.TolerantPDFTextStripper;

public class PdfParser extends Parser {

    public static final String[] DEFAULT_MIMETYPES = { "application/pdf" };

    public static final String[] DEFAULT_EXTENSIONS = { "pdf" };

    public static final Semaphore gsSemaphore = new Semaphore(Runtime.getRuntime().availableProcessors());

    private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name, ParserFieldEnum.title, ParserFieldEnum.author,
	    ParserFieldEnum.subject, ParserFieldEnum.content, ParserFieldEnum.producer, ParserFieldEnum.keywords,
	    ParserFieldEnum.creation_date, ParserFieldEnum.modification_date, ParserFieldEnum.language,
	    ParserFieldEnum.number_of_pages, ParserFieldEnum.ocr_content, ParserFieldEnum.image_ocr_boxes,
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

    private void extractMetaData(ParserResultItem result, PDDocument pdf) throws IOException {
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
    private int extractTextContent(ParserResultItem result, PDDocument pdf) throws IOException {
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
    private int extractTextContent(ParserResultItem result, PdfOcrContext context)
	    throws IOException, InterruptedException {
	File textFile = null;
	BufferedReader bufferedReader = null;
	FileReader fileReader = null;
	try {
	    textFile = File.createTempFile("oss_pdfparser", "txt");
	    context.ghostScript.extractText(context.pdfPassword, context.pdfFile, textFile);
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

    private String decrypt(PDDocument pdf, File pdfFile)
	    throws BadSecurityHandlerException, IOException, CryptographyException {
	// Let's try first with an empty password
	String password = StringUtils.EMPTY;
	try {
	    pdf.openProtection(new StandardDecryptionMaterial(password));
	} catch (CryptographyException e) {
	    // New attempt with PDFCrack
	    String pdfCrackCommandLine = getStringProperty(ClassPropertyEnum.PDFCRACK_COMMANDLINE);
	    if (StringUtils.isEmpty(pdfCrackCommandLine))
		throw e;
	    password = PdfCrack.findPassword(pdfCrackCommandLine, pdfFile);
	    if (password == null) // No password found
		throw new IOException("Encrypted PDF.");
	    // Password found, let's open
	    pdf.openProtection(new StandardDecryptionMaterial(password));
	}
	return password;
    }

    @Override
    protected void parseContent(StreamLimiter streamLimiter, final LanguageEnum lang) throws IOException {
	PdfOcrContext context = new PdfOcrContext();
	context.lang = lang;
	String fileName = null;
	try {
	    String ghostScriptBinaryPath = getStringProperty(ClassPropertyEnum.GHOSTSCRIPT_BINARYPATH);
	    context.ghostScript = StringUtils.isEmpty(ghostScriptBinaryPath) ? null
		    : new GhostScript(ghostScriptBinaryPath);
	    fileName = streamLimiter.getFile().getName();
	    context.pdfFile = streamLimiter.getFile();
	    context.pdf = PDDocument.load(context.pdfFile, null);
	    try {
		if (context.pdf.isEncrypted())
		    context.pdfPassword = decrypt(context.pdf, context.pdfFile);
	    } catch (Exception e) {
		Logging.warn("PDFBox decryption failed " + fileName);
		IOUtils.closeQuietly(context.pdf);
		context.pdf = null;
	    }
	    ParserResultItem result = getNewParserResultItem();
	    result.addField(ParserFieldEnum.pdfcrack_password, context.pdfPassword);
	    if (context.pdf != null)
		extractMetaData(result, context.pdf);
	    int charCount = 0;
	    if (context.ghostScript == null) {
		if (context.pdf != null)
		    charCount = extractTextContent(result, context.pdf);
	    } else
		charCount = extractTextContent(result, context);
	    if (charCount == 0 && context.pdf != null)
		extractImagesForOCR(result, context);
	    result.langDetection(10000, ParserFieldEnum.content);
	} catch (SearchLibException e) {
	    throw new IOException("Failed on " + fileName, e);
	} catch (InterruptedException e) {
	    throw new IOException("Failed on " + fileName, e);
	} catch (java.util.concurrent.ExecutionException e) {
	    throw new IOException("Failed on " + fileName, e);
	} finally {
	    if (context.pdf != null)
		context.pdf.close();
	}
    }

    private HocrDocument doOcr(OcrManager ocr, LanguageEnum lang, BufferedImage image)
	    throws IOException, InterruptedException, SearchLibException {
	File hocrFile = null;
	try {
	    hocrFile = File.createTempFile("ossocr", "." + ocr.getHocrFileExtension());
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
	    hocrFile = File.createTempFile("ossocr", "." + ocr.getHocrFileExtension());
	    ocr.ocerize(imageFile, hocrFile, lang, true);
	    if (hocrFile.length() == 0)
		return null;
	    return new HocrDocument(hocrFile);
	} finally {
	    if (hocrFile != null)
		FileUtils.deleteQuietly(hocrFile);
	}
    }

    private void ocrImageGhostcript(PdfOcrContext context, int page)
	    throws IOException, InterruptedException, SearchLibException {
	File imageFile = null;
	try {
	    imageFile = File.createTempFile("oss_pdfparser", ".png");
	    gsSemaphore.acquire();
	    try {
		context.ghostScript.generateImage(context.pdfPassword, page, context.pdfFile, 300, imageFile);
	    } finally {
		gsSemaphore.release();
	    }
	    Dimension dimension = ImageUtils.getDimensions(imageFile);
	    HocrPage hocrPage = context.hocrPdf.createPage(page - 1, dimension.width, dimension.height);
	    hocrPage.addImage(doOcr(context.ocr, context.lang, imageFile));
	} finally {
	    if (imageFile != null)
		if (imageFile.exists())
		    imageFile.delete();
	}
    }

    public class PdfOcrContext {

	private PDDocument pdf = null;
	private OcrManager ocr = null;
	private LanguageEnum lang = null;
	private GhostScript ghostScript = null;
	private File pdfFile = null;
	private String pdfPassword = null;
	private HocrPdf hocrPdf = null;
    }

    public class ImageOcrCallable implements Callable<Boolean> {

	private final PdfOcrContext context;
	private final PDPage page;
	private final int currentPage;
	private final AtomicInteger emptyPageImages;

	public ImageOcrCallable(PdfOcrContext context, PDPage page, int currentPage, AtomicInteger emptyPageImages) {
	    this.context = context;
	    this.page = page;
	    this.currentPage = currentPage;
	    this.emptyPageImages = emptyPageImages;
	}

	@Override
	public Boolean call() throws IOException, InterruptedException, SearchLibException {
	    if (PDFBoxUtils.countCheckImage(page) == 0)
		return false;
	    if (context.ghostScript == null) {
		BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_BGR, 300);
		if (ImageUtils.checkIfManyColors(image)) {
		    HocrPage hocrPage = context.hocrPdf.createPage(currentPage - 1, image.getWidth(),
			    image.getHeight());
		    hocrPage.addImage(doOcr(context.ocr, context.lang, image));
		} else
		    emptyPageImages.incrementAndGet();
	    } else {
		ocrImageGhostcript(context, currentPage);
	    }
	    return true;
	}
    }

    private void extractImagesForOCR(ParserResultItem result, PdfOcrContext context)
	    throws SearchLibException, IOException, InterruptedException, java.util.concurrent.ExecutionException {

	context.ocr = ClientCatalog.getOcrManager();
	if (context.ocr == null || context.ocr.isDisabled())
	    return;
	if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content)
		&& !getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
	    return;

	context.hocrPdf = new HocrPdf();
	List<?> pages = context.pdf.getDocumentCatalog().getAllPages();
	Iterator<?> iter = pages.iterator();
	int currentPage = 0;
	AtomicInteger emptyPageImages = new AtomicInteger(0);

	ExecutorService executorService = config.getThreadPool();
	List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
	while (iter.hasNext()) {
	    PDPage page = (PDPage) iter.next();
	    ImageOcrCallable callable = new ImageOcrCallable(context, page, ++currentPage, emptyPageImages);
	    futures.add(executorService.submit(callable));
	}
	ThreadUtils.<Boolean> done(futures);

	if (currentPage > 0 && emptyPageImages.get() == currentPage)
	    throw new SearchLibException("All pages are blank " + currentPage);

	if (getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
	    context.hocrPdf.putHocrToParserField(result, ParserFieldEnum.image_ocr_boxes);
	if (getFieldMap().isMapped(ParserFieldEnum.ocr_content))
	    context.hocrPdf.putTextToParserField(result, ParserFieldEnum.ocr_content);

    }

    @Override
    public void mergeFiles(File fileDir, File destFile) throws SearchLibException {
	PDFMergerUtility pdfMerger = new PDFMergerUtility();
	File[] files = new LastModifiedFileComparator().sort(fileDir.listFiles());
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
