/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.util.pdfbox;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PDFBoxHighlighter extends PDFTextStripper {

	private final String[] keywords;
	private final Collection<Rectangle> boxes;
	private final Dimension imageDimension;
	private float xFactor;
	private float yFactor;

	public PDFBoxHighlighter(String[] keywords, Collection<Rectangle> boxes, Dimension imageDimension)
			throws IOException {
		this.keywords = keywords;
		this.boxes = boxes;
		this.imageDimension = imageDimension;
	}

	@Override
	protected void startPage(PDPage page) throws IOException {
		super.startPage(page);
		final PDRectangle rect = page.getCropBox();
		xFactor = imageDimension.width / rect.getWidth();
		yFactor = imageDimension.height / rect.getHeight();
	}

	@Override
	protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
		super.writeString(text, textPositions);
		if (textPositions == null)
			return;
		StringBuilder term = new StringBuilder();
		final List<Rectangle> rects = new ArrayList<>();
		for (TextPosition tp : textPositions) {
			String str = tp.getUnicode();
			if (str.length() > 1) {
				handleTerm(term, rects);
				term = new StringBuilder();
				rects.clear();
				addRect(tp, rects);
				handleTerm(str, rects);
				continue;
			}
			char c = str.charAt(0);
			if (!Character.isLetterOrDigit(c)) {
				handleTerm(term, rects);
				term = new StringBuilder();
				rects.clear();
				continue;
			}
			term.append(c);
			addRect(tp, rects);
		}
		handleTerm(term, rects);
	}

	private void addRect(TextPosition tp, List<Rectangle> rects) {
		Rectangle rect = new Rectangle();
		float xdelta = tp.getWidthDirAdj() * .2F;
		float ydelta = tp.getHeightDir() * .2F;
		rect.x = (int) ((tp.getXDirAdj() - xdelta) * xFactor);
		rect.y = (int) ((tp.getYDirAdj() - (tp.getHeightDir() + ydelta)) * yFactor);
		rect.width = (int) ((tp.getWidthDirAdj() + xdelta * 2) * xFactor);
		rect.height = (int) ((tp.getHeightDir() + ydelta * 2) * yFactor);
		rects.add(rect);
	}

	private void handleTerm(CharSequence term, List<Rectangle> rects) {
		if (term == null)
			return;
		if (term.length() == 0)
			return;
		if (rects == null)
			return;
		if (rects.size() == 0)
			return;
		String str = term.toString();
		for (String keyword : keywords) {
			if (keyword.equalsIgnoreCase(str)) {
				Rectangle unionRect = new Rectangle(rects.get(0));
				for (Rectangle rect : rects) {
					if (rect.y != unionRect.y) {
						unionRect = null;
						break;
					}
					unionRect = unionRect.union(rect);
				}
				if (unionRect != null)
					boxes.add(unionRect);
				else
					for (Rectangle rect : rects)
						boxes.add(rect);
				return;
			}
		}
	}
}
