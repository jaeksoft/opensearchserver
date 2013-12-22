/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.ocr;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class OcrManager implements Closeable {

	private final static String OCR_PROPERTY_FILE = "ocr.xml";

	private final static String OCR_PROPERTY_ENABLED = "enabled";

	private final static String OCR_PROPERTY_DEFAULT_LANGUAGE = "defaultLanguage";

	private final static String OCR_PROPERTY_TESSERACT_PATH = "tesseractPath";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private boolean enabled = false;

	private String tesseractPath = null;

	private TesseractLanguageEnum defaultLanguage;

	private File propFile;

	private OcrManager(File dataDir) throws InvalidPropertiesFormatException,
			IOException, InstantiationException, IllegalAccessException {
		propFile = new File(dataDir, OCR_PROPERTY_FILE);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		enabled = "true".equalsIgnoreCase(properties.getProperty(
				OCR_PROPERTY_ENABLED, "false"));
		defaultLanguage = TesseractLanguageEnum.find(properties.getProperty(
				OCR_PROPERTY_DEFAULT_LANGUAGE,
				TesseractLanguageEnum.None.name()));
		tesseractPath = properties.getProperty(OCR_PROPERTY_TESSERACT_PATH);
		setEnabled(enabled);
	}

	private static OcrManager INSTANCE = null;
	final private static ReadWriteLock rwlInstance = new ReadWriteLock();

	public static final OcrManager getInstance() throws SearchLibException {
		rwlInstance.r.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
		} finally {
			rwlInstance.r.unlock();
		}
		rwlInstance.w.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
			return INSTANCE = new OcrManager(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE);
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			rwlInstance.w.unlock();
		}
	}

	private void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(OCR_PROPERTY_ENABLED, Boolean.toString(enabled));
		if (tesseractPath != null)
			properties.setProperty(OCR_PROPERTY_TESSERACT_PATH, tesseractPath);
		if (defaultLanguage != null)
			properties.setProperty(OCR_PROPERTY_DEFAULT_LANGUAGE,
					defaultLanguage.name());
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		rwl.r.lock();
		try {
			return enabled;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 * @throws IOException
	 */
	public void setEnabled(boolean enabled) throws IOException {
		rwl.w.lock();
		try {
			this.enabled = enabled;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the tesseractPath
	 */
	public String getTesseractPath() {
		rwl.r.lock();
		try {
			return tesseractPath;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param tesseractPath
	 *            the tesseractPath to set
	 * @throws IOException
	 */
	public void setTesseractPath(String tesseractPath) throws IOException {
		rwl.w.lock();
		try {
			this.tesseractPath = tesseractPath;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	private final static Pattern tesseractCheckPattern = Pattern
			.compile("Usage:.*tesseract.* imagename outputbase");

	public void checkTesseract() throws SearchLibException {
		rwl.r.lock();
		try {
			if (tesseractPath == null || tesseractPath.length() == 0)
				throw new SearchLibException("Please enter a path");
			File file = new File(tesseractPath);
			if (!file.exists())
				throw new SearchLibException("The file don't exist");
			CommandLine cmdLine = CommandLine.parse(tesseractPath);
			StringBuilder sbResult = new StringBuilder();
			run(cmdLine, 60, 1, sbResult);
			String result = sbResult.toString();
			if (!tesseractCheckPattern.matcher(result).find())
				throw new SearchLibException("Wrong returned message: "
						+ result);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	private String checkOutputPath(File outputFile, boolean hocr)
			throws SearchLibException {
		String outputPath = outputFile.getAbsolutePath();
		if (hocr) {
			if (!outputPath.endsWith(".html"))
				throw new SearchLibException(
						"Output file must ends with .txt OR .html ("
								+ outputPath + ")");
			outputPath = outputPath.substring(0, outputPath.length() - 5);
		} else {
			if (!outputPath.endsWith(".txt"))
				throw new SearchLibException(
						"Output file must ends with .txt OR .html ("
								+ outputPath + ")");
			outputPath = outputPath.substring(0, outputPath.length() - 4);
		}
		return outputPath;
	}

	public void ocerize(File input, File outputFile, LanguageEnum lang,
			boolean hocr) throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			if (!enabled)
				return;
			if (tesseractPath == null || tesseractPath.length() == 0)
				throw new SearchLibException("No path for the OCR");
			CommandLine cmdLine = CommandLine.parse(tesseractPath);
			cmdLine.addArgument(input.getAbsolutePath());
			cmdLine.addArgument(checkOutputPath(outputFile, hocr));
			TesseractLanguageEnum tle = TesseractLanguageEnum.find(lang);
			if (tle == null)
				tle = defaultLanguage;
			if (tle != null && tle != TesseractLanguageEnum.None)
				cmdLine.addArgument("-l " + tle.option);
			if (hocr)
				cmdLine.addArgument("hocr");
			int ev = run(cmdLine, 3600, null, null);
			if (ev == 3)
				Logging.warn("Image format not supported by Tesseract ("
						+ input.getName() + ")");
		} finally {
			rwl.r.unlock();
		}
	}

	private final static String OCR_IMAGE_FORMAT = "jpg";

	public void ocerizeImage(Image image, File outputFile, LanguageEnum lang,
			boolean hocr) throws InterruptedException, IOException,
			SearchLibException {
		File imageFile = null;
		try {
			RenderedImage renderedImage = ImageUtils.toBufferedImage(image);
			imageFile = File
					.createTempFile("ossocrimg", '.' + OCR_IMAGE_FORMAT);
			ImageIO.write(renderedImage, OCR_IMAGE_FORMAT, imageFile);
			image.flush();
			if (imageFile.length() == 0)
				throw new SearchLibException("Empty image "
						+ imageFile.getAbsolutePath());
			ocerize(imageFile, outputFile, lang, hocr);
		} finally {
			Logging.debug(imageFile);
			if (imageFile != null)
				FileUtils.deleteQuietly(imageFile);
		}
	}

	private final int run(CommandLine cmdLine, int secTimeOut,
			Integer expectedExitValue, StringBuilder returnedText)
			throws IOException, SearchLibException {
		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Logging.info("LOG OCR: " + cmdLine);
			PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
			executor.setStreamHandler(streamHandler);
			if (expectedExitValue != null)
				executor.setExitValue(expectedExitValue);
			ExecuteWatchdog watchdog = new ExecuteWatchdog(secTimeOut * 1000);
			executor.setWatchdog(watchdog);
			int ev = executor.execute(cmdLine);
			if (expectedExitValue != null)
				if (ev != expectedExitValue)
					throw new SearchLibException("Bad exit value (" + ev + ") ");
			if (returnedText != null)
				returnedText.append(baos.toString("UTF-8"));
			return ev;
		} finally {
			if (baos != null)
				IOUtils.closeQuietly(baos);
		}
	}

	/**
	 * @return the defaultLanguage
	 */
	public TesseractLanguageEnum getDefaultLanguage() {
		rwl.r.lock();
		try {
			return defaultLanguage;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param defaultLanguage
	 *            the defaultLanguage to set
	 * @throws IOException
	 */
	public void setDefaultLanguage(TesseractLanguageEnum defaultLanguage)
			throws IOException {
		rwl.w.lock();
		try {
			this.defaultLanguage = defaultLanguage;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

}
