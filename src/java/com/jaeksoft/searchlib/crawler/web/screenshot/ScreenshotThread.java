/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.screenshot;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class ScreenshotThread extends ThreadAbstract {

	private String url;
	private byte[] data;
	private int captureWidth, captureHeight;
	private int resizeWidth, resizeHeight;
	private FirefoxDriver driver;
	private File destFile;
	private BufferedImage finalImage;

	private final ReentrantLock lock = new ReentrantLock();

	public ScreenshotThread(Config config, String url, int captureWidth,
			int captureHeight, int resizeWidth, int resizeHeight, File destFile) {
		super(config, null);
		finalImage = null;
		this.destFile = destFile;
		driver = null;
		this.url = url;
		data = null;
		this.captureWidth = captureWidth;
		this.captureHeight = captureHeight;
		this.resizeWidth = resizeWidth;
		this.resizeHeight = resizeHeight;
	}

	private final void initDriver() {
		lock.lock();
		try {
			driver = new FirefoxDriver();
		} finally {
			lock.unlock();
		}

	}

	private final BufferedImage scaleWidth(BufferedImage image) {
		BufferedImage scaledImage = new BufferedImage(captureWidth,
				image.getHeight(), image.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.drawImage(image, (captureWidth - image.getWidth()) / 2, 0,
				image.getWidth(), image.getHeight(), null);
		graphics2D.dispose();
		return scaledImage;
	}

	private final BufferedImage scaleHeight(BufferedImage image) {
		BufferedImage scaledImage = new BufferedImage(image.getWidth(),
				captureHeight, image.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.drawImage(image, 0, (captureHeight - image.getHeight()) / 2,
				image.getWidth(), image.getHeight(), null);
		graphics2D.dispose();
		return scaledImage;
	}

	private final BufferedImage extractSubImage(BufferedImage image) {
		int left = (image.getWidth() - captureWidth) / 2;
		return image.getSubimage(left, 0, captureWidth, captureHeight);
	}

	private final BufferedImage resizeImage(BufferedImage image) {
		int type = (image.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) image;
		int w = image.getWidth();
		int h = image.getHeight();

		while (w != resizeWidth || h != resizeHeight) {
			if (w > resizeWidth) {
				w /= 2;
				if (w < resizeWidth)
					w = resizeWidth;
			}

			if (h > resizeHeight) {
				h /= 2;
				if (h < resizeHeight)
					h = resizeHeight;
			}

			BufferedImage tmp = new BufferedImage(w, h, type);

			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		}

		return ret;
	}

	@Override
	public void runner() throws Exception {
		try {
			initDriver();
			driver.get(url);
			data = driver.getScreenshotAs(OutputType.BYTES);
			BufferedImage image = ImageIO.read(new ByteInputStream(data,
					data.length));
			if (image.getWidth() < captureWidth)
				image = scaleWidth(image);
			if (image.getHeight() < captureHeight)
				image = scaleHeight(image);
			image = extractSubImage(image);
			if (resizeWidth != captureWidth && resizeHeight != captureHeight)
				image = resizeImage(image);
			ImageIO.write(image, "png", destFile);
			finalImage = image;
		} finally {
			release();
		}
	}

	public BufferedImage getImage() {
		return finalImage;
	}

	@Override
	public void release() {
		lock.lock();
		try {
			if (driver == null)
				return;
			driver.quit();
			driver = null;
		} finally {
			lock.unlock();
		}
	}

}
