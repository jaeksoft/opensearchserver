/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util.pdfbox;

import java.io.IOException;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFBoxUtils {

	public static class TolerantPDFTextStripper extends PDFTextStripper {

		public TolerantPDFTextStripper() throws IOException {
			super("UTF-8");
		}

		@Override
		public String getText(PDDocument doc) throws IOException {
			try {
				return super.getText(doc);
			} catch (RuntimeException e) {
				String text = output.toString();
				if (text == null || text.length() == 0)
					throw e;
				return text;
			}
		}
	}

	public static final int countCheckImage(PDPage page) throws IOException {
		PDResources resources = page.getResources();
		Map<String, PDXObject> objects = resources.getXObjects();
		if (objects == null)
			return 0;
		int count = 0;
		for (PDXObject object : objects.values())
			if (object instanceof PDXObjectImage)
				count++;
		return count;
	}

}
