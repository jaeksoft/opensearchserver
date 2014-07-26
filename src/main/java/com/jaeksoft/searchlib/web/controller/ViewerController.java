/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.naming.NamingException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloadThread;
import com.jaeksoft.searchlib.ocr.HocrPdf;
import com.jaeksoft.searchlib.ocr.HocrPdf.HocrPage;
import com.jaeksoft.searchlib.renderer.RendererResult;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface;
import com.jaeksoft.searchlib.util.ExecuteUtils.ExecutionException;
import com.jaeksoft.searchlib.util.GhostScript;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.pdfbox.PDFBoxHighlighter;

@AfterCompose(superclass = true)
public class ViewerController extends CommonController {

	private int page;

	private int numberOfPages;

	private float zoom;

	private final static int[] zoomScale = { 10, 20, 30, 40, 50, 60, 70, 80,
			90, 100, 150, 200, 250, 300, 400 };

	private HttpDownloadThread downloadThread;

	private File tempFile;

	private BufferedImage currentImage;

	private URI uri;

	private String search;

	private String[] keywords;

	private HocrPdf hocrPdf;

	private GhostScript ghostScript = null;

	public ViewerController() throws SearchLibException, IOException,
			NamingException, URISyntaxException {
		super();
		hocrPdf = null;
		downloadThread = null;
		currentImage = null;
		Client client = null;
		numberOfPages = 0;
		page = 1;
		zoom = 100;
		String h = getRequestParameter("h");
		RendererResult result = null;
		if (h != null) {
			String p = getRequestParameter("p");
			Integer hashCode = Integer.parseInt(h);
			Integer pos = Integer.parseInt(p);
			result = ClientCatalog.getRendererResults().find(hashCode);
			if (result == null)
				return;
			client = result.getClient();
			RendererResult.Item item = result.getItem(pos);
			uri = new URI(item.getUrl());
			setSearch(result.getKeywords());
			hocrPdf = item.getHocrPdf();
		} else {
			String index = getRequestParameter("index");
			String u = getRequestParameter("uri");
			setSearch(getRequestParameter("search"));
			uri = new URI(u);
			client = ClientCatalog.getClient(index);
		}
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			try {
				tempFile = new File(uri);
			} catch (Throwable t) {
				Logging.warn(t);
				tempFile = new File(uri.getPath());
			}
		} else if ("smb".equalsIgnoreCase(uri.getScheme())) {
			tempFile = File.createTempFile("oss_pdf_viewer", ".pdf");
			NtlmPasswordAuthentication auth = null;
			SmbFile smbFile;
			String url = URLDecoder.decode(uri.toString(), "UTF-8");
			if (result != null) {
				AuthPluginInterface.User user = result.getLoggedUser();
				if (user != null)
					auth = new NtlmPasswordAuthentication(
							result.getAuthDomain(), user.username,
							user.password);
				else if (result.isAuthCredential())
					auth = new NtlmPasswordAuthentication(
							result.getAuthDomain(), result.getAuthUsername(),
							result.getAuthPassword());
			}
			smbFile = auth != null ? new SmbFile(url, auth) : new SmbFile(url);
			IOUtils.copy(smbFile.getInputStream(), tempFile, true);
		} else {
			tempFile = File.createTempFile("oss_pdf_viewer", ".pdf");
			downloadThread = new HttpDownloadThread(client, uri, tempFile, true);
			downloadThread.execute();
		}
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page
	 *            the page to set
	 * @throws SearchLibException
	 */
	@NotifyChange({ "currentImage", "page" })
	public void setPage(int page) {
		if (page < 1)
			page = 1;
		if (page > numberOfPages)
			page = numberOfPages;
		if (page == this.page)
			return;
		this.page = page;
		currentImage = null;
	}

	/**
	 * @return the zoom
	 */
	public int getZoom() {
		return (int) zoom;
	}

	/**
	 * @param zoom
	 *            the zoom to set
	 */
	@NotifyChange({ "currentImage", "zoom" })
	public void setZoom(int zoom) {
		this.zoom = zoom;
		currentImage = null;
	}

	@Command
	@NotifyChange({ "currentImage", "page" })
	public void onPageUp() {
		setPage(page + +1);
	}

	@Command
	@NotifyChange({ "currentImage", "page" })
	public void onPageDown() {
		setPage(page - 1);
	}

	@Command
	@NotifyChange({ "currentImage", "zoom" })
	public void onZoomUp() {
		for (int zc : zoomScale) {
			if (zc > zoom) {
				setZoom(zc);
				return;
			}
		}
	}

	@Command
	@NotifyChange({ "currentImage", "zoom" })
	public void onZoomDown() {
		int lastzc = zoomScale[0];
		for (int zc : zoomScale) {
			if (zc >= zoom) {
				setZoom(lastzc);
				return;
			}
			lastzc = zc;
		}
	}

	@Command
	@NotifyChange("*")
	public void onTimer() {
	}

	@Command
	public void onDownload() throws FileNotFoundException {
		Filedownload.save(new FileInputStream(tempFile), null,
				FilenameUtils.getName(uri.getPath()));
	}

	public boolean isDownloading() {
		if (downloadThread == null)
			return false;
		return downloadThread.isRunning();
	}

	public boolean isDownloaded() {
		return !isDownloading();
	}

	public String getMessage() {
		if (downloadThread == null)
			return null;
		Exception e = downloadThread.getException();
		if (e != null)
			return e.getMessage();
		if (downloadThread.isRunning())
			return "Downloading";
		return uri.toString();
	}

	public int getDownloadPercent() {
		if (downloadThread == null)
			return 0;
		return downloadThread.getPercent();
	}

	private void checkHocrHighlight(float pageWidth, float pageHeight,
			List<Rectangle> boxList) {
		if (hocrPdf == null || keywords == null)
			return;
		float zoomFactor = zoom / 100;
		HocrPage page = hocrPdf.getPage(this.page - 1);
		float xFactor = (pageWidth / page.getPageWidth()) * zoomFactor;
		float yFactor = (pageHeight / page.getPageHeight()) * zoomFactor;
		if (page != null)
			for (String keyword : keywords)
				page.addBoxes(keyword, boxList, xFactor, yFactor);
	}

	private void checkIcePdfHighlight(Document pdf, float pageHeight,
			float zoomFactor, List<Rectangle> boxList) {
		if (keywords == null)
			return;
		PageText pageText = pdf.getPageViewText(this.page - 1);
		if (pageText != null) {
			List<LineText> lines = pageText.getPageLines();
			if (lines != null) {
				for (LineText lineText : lines) {
					for (WordText wordText : lineText.getWords()) {
						for (String keyword : keywords)
							if (keyword.equalsIgnoreCase(wordText.getText())) {
								Rectangle2D.Float rectf = wordText.getBounds();
								Rectangle rect = new Rectangle();
								rect.x = (int) (rectf.x * zoomFactor);
								rect.y = (int) ((pageHeight - rectf.y - rectf.height) * zoomFactor);
								rect.width = (int) (rectf.width * zoomFactor);
								rect.height = (int) (rectf.height * zoomFactor);
								boxList.add(rect);
								break;
							}
					}
				}
			}
		}
	}

	private void loadIcePdf() throws PDFException, PDFSecurityException,
			IOException {
		Document pdf = null;
		try {
			int pdfPage = page - 1;
			pdf = new Document();
			pdf.setFile(tempFile.getAbsolutePath());
			PDimension pd = pdf.getPageDimension(pdfPage, 0.0f);
			float zoomFactor = zoom / 100;
			float pageWidth = pd.getWidth();
			float pageHeight = pd.getHeight();
			List<Rectangle> boxList = new ArrayList<Rectangle>(0);
			checkIcePdfHighlight(pdf, pageHeight, zoomFactor, boxList);
			currentImage = (BufferedImage) pdf.getPageImage(pdfPage,
					GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX, 0.0f,
					zoom / 100);
			checkHocrHighlight(pageWidth, pageHeight, boxList);
			ImageUtils.yellowHighlight(currentImage, boxList);
			numberOfPages = pdf.getNumberOfPages();
		} finally {
			if (pdf != null)
				pdf.dispose();
		}
	}

	/**
	 * gs -q -dFirstPage=3 -dLastPage=3 -dNOPAUSE -dBATCH -sDEVICE=jpeg -r72
	 * -dEPSCrop -sOutputFile=out.jpg "document.pdf"
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void loadGS(String pdfPassword) throws IOException,
			InterruptedException {
		if (ghostScript == null)
			ghostScript = new GhostScript(null);
		File imageFile = null;
		try {
			imageFile = File.createTempFile("oss-viewer", ".png");
			float zoomFactor = zoom / 100;
			int factor = (int) ((zoomFactor * zoom));
			if (factor > 2400)
				factor = 2400;
			ghostScript.generateImage(pdfPassword, page, tempFile, factor,
					imageFile);
			currentImage = ImageIO.read(imageFile);
		} finally {
			if (imageFile != null)
				if (imageFile.exists())
					imageFile.delete();
		}
	}

	private void checkPdfBoxHighlight(PDDocument document,
			List<Rectangle> boxList) throws IOException {
		PDFBoxHighlighter pdfstripper = new PDFBoxHighlighter(keywords,
				boxList, new Dimension(currentImage.getWidth(),
						currentImage.getHeight()));
		pdfstripper.setStartPage(page - 1);
		pdfstripper.setEndPage(page);
		pdfstripper.getText(document);
	}

	private void loadPdfBox() throws IOException, CryptographyException,
			SearchLibException, InterruptedException {
		PDDocument document = null;
		try {
			document = PDDocument.loadNonSeq(tempFile, null);
			// Trying to open with empty password
			boolean isEncrypted = document.isEncrypted();
			if (isEncrypted)
				document.decrypt("");
			loadGS(isEncrypted ? "" : null);
			PDPage pdPage = (PDPage) document.getDocumentCatalog()
					.getAllPages().get(page - 1);
			PDRectangle pdRect = pdPage.getArtBox();
			List<Rectangle> boxList = new ArrayList<Rectangle>(0);
			checkPdfBoxHighlight(document, boxList);
			checkHocrHighlight(pdRect.getWidth(), pdRect.getHeight(), boxList);
			ImageUtils.yellowHighlight(currentImage, boxList);
			numberOfPages = document.getNumberOfPages();
		} finally {
			if (document != null)
				IOUtils.close(document);
		}
	}

	public Image getCurrentImage() throws SearchLibException {
		if (currentImage != null)
			return currentImage;
		if (downloadThread != null) {
			if (downloadThread.isRunning())
				return null;
			if (downloadThread.getException() != null)
				return null;
			if (!downloadThread.isDownloadSuccess())
				return null;
		}
		if (tempFile == null)
			return null;
		try {
			loadPdfBox();
			return currentImage;
		} catch (ExecutionException e) {
			Logging.warn(e.getCommandLine());
			Logging.warn(e.getReturnedText());
		} catch (Exception e) {
			Logging.warn(e);
		}
		try {
			loadIcePdf();
			return currentImage;
		} catch (Exception e) {
			Logging.error(e);
			throw new SearchLibException("Unable to generate the image.", e);
		}
	}

	public int getImageWidth() {
		if (currentImage == null)
			return 0;
		return currentImage.getWidth(null);
	}

	public int getImageHeight() {
		if (currentImage == null)
			return 0;
		return currentImage.getHeight(null);
	}

	/**
	 * @return the search
	 */
	public String getSearch() {
		return search;
	}

	/**
	 * @param search
	 *            the search to set
	 */
	@NotifyChange("currentImage")
	public void setSearch(String search) {
		this.search = search;
		keywords = StringUtils.split(search);
		currentImage = null;
	}
}
