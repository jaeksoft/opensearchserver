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

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;

public class HocrPdf {

	public class HocrPage {

		private List<HocrDocument> imageList;

		private HocrPage() {
			imageList = new ArrayList<HocrDocument>(0);
		}

		public void addImage(HocrDocument hocrDocument) {
			imageList.add(hocrDocument);
		}

		@SuppressWarnings("unchecked")
		private JSONArray getJsonBoxMap() {
			JSONArray jsonImages = new JSONArray();
			for (HocrDocument image : imageList)
				jsonImages.add(image.getJsonBoxMap());
			return jsonImages;
		}

		private boolean isOneImage() {
			return imageList.size() == 1;
		}
	}

	private List<HocrPage> pageList;

	public HocrPdf() {
		pageList = new ArrayList<HocrPage>(0);
	}

	public HocrPage createPage() {
		HocrPage page = new HocrPage();
		pageList.add(page);
		return page;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getJsonBoxMap() {
		JSONArray jsonPages = new JSONArray();
		for (HocrPage page : pageList)
			jsonPages.add(page.getJsonBoxMap());
		return jsonPages;
	}

	public void putToParserField(Parser parser, ParserFieldEnum parserField) {
		parser.addField(parserField, getJsonBoxMap().toJSONString());
	}

	public boolean isOneImagePerPage() {
		for (HocrPage page : pageList)
			if (!page.isOneImage())
				return false;
		return true;
	}

}
