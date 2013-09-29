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

package com.jaeksoft.searchlib.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.ocr.HocrDocument;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.ImagePHash;

public class ImageParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.image_width, ParserFieldEnum.image_height,
			ParserFieldEnum.image_area_size, ParserFieldEnum.image_number,
			ParserFieldEnum.image_format, ParserFieldEnum.file_name,
			ParserFieldEnum.ocr_content, ParserFieldEnum.image_ocr_boxes,
			ParserFieldEnum.image_phash, ParserFieldEnum.md5 };

	public ImageParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	private void doOCR(ParserResultItem result, StreamLimiter streamLimiter,
			LanguageEnum lang) throws IOException {
		File hocrFile = null;
		try {
			OcrManager ocr = ClientCatalog.getOcrManager();
			if (ocr == null || ocr.isDisabled())
				return;
			if (!getFieldMap().isMapped(ParserFieldEnum.ocr_content))
				return;

			hocrFile = File.createTempFile("ossocr", ".html");
			ocr.ocerize(streamLimiter.getFile(), hocrFile, lang, true);
			HocrDocument hocrDoc = new HocrDocument(hocrFile);
			if (getFieldMap().isMapped(ParserFieldEnum.ocr_content))
				hocrDoc.putTextToParserField(result,
						ParserFieldEnum.ocr_content);
			if (getFieldMap().isMapped(ParserFieldEnum.image_ocr_boxes))
				hocrDoc.putHocrToParserField(result,
						ParserFieldEnum.image_ocr_boxes);
		} catch (SearchLibException e) {
			throw new IOException(e);
		} finally {
			if (hocrFile != null)
				FileUtils.deleteQuietly(hocrFile);
		}
	}

	private ImageInfo doMetaData(ParserResultItem result,
			StreamLimiter streamLimiter) throws IOException {
		try {
			ImageInfo info = Sanselan.getImageInfo(
					streamLimiter.getNewInputStream(),
					streamLimiter.getOriginalFileName());
			if (info == null)
				return null;
			int width = info.getWidth();
			int height = info.getHeight();
			long area_size = (long) width * height;
			result.addField(ParserFieldEnum.image_width, width);
			result.addField(ParserFieldEnum.image_height, height);
			result.addField(ParserFieldEnum.image_area_size, area_size);
			result.addField(ParserFieldEnum.image_number,
					info.getNumberOfImages());
			result.addField(ParserFieldEnum.image_format, info.getFormatName());
			return info;
		} catch (ImageReadException e) {
			throw new IOException(e);
		}
	}

	private void image_phash(ImagePHash imgPhash, Object image,
			ParserResultItem result) {
		if (!(image instanceof BufferedImage))
			return;
		BufferedImage bimage = (BufferedImage) image;
		String phash = imgPhash.getHash(bimage);
		result.addField(ParserFieldEnum.image_phash, phash);
	}

	/*
	 * private boolean try_sanselan_phash(ImagePHash imgPhash, ImageInfo info,
	 * ParserResultItem result, StreamLimiter streamLimiter) { if
	 * (info.getFormat() == ImageFormat.IMAGE_FORMAT_JPEG) return false; try {
	 * ArrayList<?> images = Sanselan.getAllBufferedImages(
	 * streamLimiter.getNewInputStream(), streamLimiter.getOriginalFileName());
	 * if (images == null) return false; for (Object image : images)
	 * image_phash(imgPhash, image, result); return true; } catch
	 * (ImageReadException e) { Logging.warn(e); return false; } catch
	 * (IOException e) { Logging.warn(e); return false; } }
	 */

	private void try_imageio_phash(ImagePHash imgPhash,
			ParserResultItem result, StreamLimiter streamLimiter)
			throws IOException {
		BufferedImage image = ImageIO.read(streamLimiter.getNewInputStream());
		if (image == null)
			return;
		image_phash(imgPhash, image, result);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		try {
			ImagePHash imgPhash = new ImagePHash();
			ParserResultItem resultItem = getNewParserResultItem();
			resultItem.addField(ParserFieldEnum.file_name,
					streamLimiter.getOriginalFileName());
			doMetaData(resultItem, streamLimiter);
			doOCR(resultItem, streamLimiter, lang);
			if (getFieldMap().isMapped(ParserFieldEnum.image_phash))
				try_imageio_phash(imgPhash, resultItem, streamLimiter);
			if (getFieldMap().isMapped(ParserFieldEnum.md5))
				resultItem.addField(ParserFieldEnum.md5,
						streamLimiter.getMD5Hash());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
}
