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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class HocrPdf {

	public class HocrPage {

		private final long pageNumber;
		private final List<HocrDocument> imageList;
		private final long pageWidth;
		private final long pageHeight;

		private HocrPage(long pageNumber, long pageWidth, long pageHeight) {
			this.pageNumber = pageNumber;
			this.pageWidth = pageWidth;
			this.pageHeight = pageHeight;
			imageList = new ArrayList<HocrDocument>(0);
		}

		private Integer getValue(JSONObject jsonObject, String key)
				throws SearchLibException {
			Object o = jsonObject.get(key);
			if (o == null)
				throw new SearchLibException("No " + key + " record");
			return Integer.parseInt(o.toString());
		}

		public HocrPage(FieldValueItem fieldValueItem)
				throws SearchLibException {
			JSONObject jsonObject = (JSONObject) JSONValue.parse(fieldValueItem
					.getValue());
			pageNumber = getValue(jsonObject, "page");
			pageWidth = getValue(jsonObject, "width");
			pageHeight = getValue(jsonObject, "height");
			imageList = new ArrayList<HocrDocument>(0);
			JSONArray jsonArray = (JSONArray) jsonObject.get("images");
			if (jsonArray == null)
				return;
			for (Object obj : jsonArray)
				imageList.add(new HocrDocument((JSONObject) obj));
		}

		public void addImage(HocrDocument hocrDocument) {
			imageList.add(hocrDocument);
		}

		@SuppressWarnings("unchecked")
		private JSONObject getJsonBoxMap() {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("page", pageNumber);
			jsonObject.put("width", pageWidth);
			jsonObject.put("height", pageHeight);
			JSONArray jsonImages = new JSONArray();
			for (HocrDocument image : imageList)
				jsonImages.add(image.getJsonBoxMap());
			jsonObject.put("images", jsonImages);
			return jsonObject;
		}

		public void addBoxes(String keyword, List<Rectangle> boxList,
				float xFactor, float yFactor) {
			for (HocrDocument hocrDocument : imageList)
				hocrDocument.addBoxes(keyword, boxList, xFactor, yFactor);
		}

		public long getPageWidth() {
			return pageWidth;
		}

		public long getPageHeight() {
			return pageHeight;
		}

		public void putTextToParserField(Parser parser,
				ParserFieldEnum parserField) {
			for (HocrDocument hocrDocument : imageList)
				hocrDocument.putTextToParserField(parser, parserField);
		}

		public void putHocrToParserField(Parser parser,
				ParserFieldEnum parserField) {
			parser.addField(parserField, getJsonBoxMap().toJSONString());
		}

	}

	private TreeMap<Long, HocrPage> pages;

	public HocrPdf() {
		pages = new TreeMap<Long, HocrPage>();
	}

	public HocrPdf(FieldValueItem[] valueArray) throws SearchLibException {
		this();
		if (valueArray == null)
			return;
		for (FieldValueItem fieldValueItem : valueArray) {
			HocrPage page = new HocrPage(fieldValueItem);
			pages.put(page.pageNumber, page);
		}
	}

	public HocrPage createPage(long pageNumber, long pageWidth, long pageHeight) {
		HocrPage page = new HocrPage(pageNumber, pageWidth, pageHeight);
		pages.put(pageNumber, page);
		return page;
	}

	public void putHocrToParserField(Parser parser, ParserFieldEnum parserField) {
		for (HocrPage page : pages.values())
			page.putHocrToParserField(parser, parserField);
	}

	public void putTextToParserField(Parser parser, ParserFieldEnum parserField) {
		for (HocrPage page : pages.values())
			page.putTextToParserField(parser, parserField);
	}

	public HocrPage getPage(long pageNumber) {
		return pages.get(pageNumber);
	}

}
