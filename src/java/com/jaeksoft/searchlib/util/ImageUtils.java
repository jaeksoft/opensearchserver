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

package com.jaeksoft.searchlib.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.ImageIcon;

public class ImageUtils {

	public static BufferedImage toBufferedImage(Image image)
			throws InterruptedException {
		System.out.println(image.getHeight(null) + " x " + image.getWidth(null)
				+ " - " + image);
		image = new ImageIcon(image).getImage();
		int type = hasAlpha(image) ? BufferedImage.TYPE_INT_ARGB
				: BufferedImage.TYPE_INT_RGB;
		BufferedImage bimage = new BufferedImage(image.getWidth(null),
				image.getHeight(null), type);
		Graphics g = bimage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	public static boolean hasAlpha(Image image) throws InterruptedException {
		if (image instanceof BufferedImage)
			return ((BufferedImage) image).getColorModel().hasAlpha();
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		pg.grabPixels();
		return pg.getColorModel().hasAlpha();
	}

	public static void checkPlugins() {
		ImageIO.scanForPlugins();
		for (String suffix : ImageIO.getReaderFileSuffixes())
			System.out.println("ImageIO suffix: " + suffix);
	}

	public static ImageReader findImageReader(String formatName) {
		Iterator<ImageReader> readers = ImageIO
				.getImageReadersByFormatName(formatName);
		ImageReader reader = null;
		while (readers.hasNext()) {
			reader = (ImageReader) readers.next();
			if (reader.canReadRaster())
				return reader;
		}
		return null;
	}
}
