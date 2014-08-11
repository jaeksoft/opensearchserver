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
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.InputSource;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.htmlParser.DomHtmlNode;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlNodeAbstract;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;

public class HocrDocument {

	private final List<StringBuilder> paragraphList;

	private final Map<String, List<HocrBox>> boxMap;

	private HocrDocument() {
		paragraphList = new ArrayList<StringBuilder>(0);
		boxMap = new TreeMap<String, List<HocrBox>>();
	}

	private final void ocrx_word(HtmlNodeAbstract<?> parentNode,
			StringBuilder currentParagraph) throws SearchLibException {
		if (parentNode == null)
			return;
		String parent_bbox = parentNode.getAttributeText("title").substring(5);
		for (HtmlNodeAbstract<?> xwordNode : parentNode.getNodes("span")) {
			if (!"ocrx_word".equals(xwordNode.getAttributeText("class")))
				continue;
			String word_bbox = xwordNode.getAttributeText("title").substring(5);
			String word = xwordNode.getText();
			if (word == null)
				continue;
			word = word.trim();
			String pword = word;
			if (word.length() == 0)
				continue;
			word = word.toLowerCase();
			List<HocrBox> boxList = boxMap.get(word);
			if (boxList == null) {
				boxList = new ArrayList<HocrBox>();
				boxMap.put(word, boxList);
			}
			boxList.add(new HocrBox(word_bbox == null ? parent_bbox : word_bbox));
			currentParagraph.append(pword);
			currentParagraph.append(' ');
		}
	}

	public HocrDocument(File ocrFile) throws SearchLibException {
		this();
		FileInputStream fis = null;
		try {

			fis = new FileInputStream(ocrFile);

			InputSource inputSource = new InputSource(fis);
			inputSource.setEncoding("UTF-8");
			DomHtmlNode doc = new DomHtmlNode(DomUtils.readXml(inputSource,
					true));

			for (HtmlNodeAbstract<?> pageNode : doc.getNodes("html", "body",
					"div")) {
				if (!"ocr_page".equals(pageNode.getAttributeText("class")))
					continue;
				for (HtmlNodeAbstract<?> areaNode : pageNode.getNodes("div")) {
					if (!"ocr_carea".equals(areaNode.getAttributeText("class")))
						continue;
					for (HtmlNodeAbstract<?> parNode : areaNode.getNodes("p")) {
						if (!"ocr_par"
								.equals(parNode.getAttributeText("class")))
							continue;
						StringBuilder currentParagraph = new StringBuilder();
						for (HtmlNodeAbstract<?> lineNode : parNode
								.getNodes("span")) {
							if (!"ocr_line".equals(lineNode
									.getAttributeText("class")))
								continue;
							for (HtmlNodeAbstract<?> wordNode : lineNode
									.getNodes("span")) {
								if (!"ocr_word".equals(wordNode
										.getAttributeText("class")))
									continue;
								ocrx_word(wordNode, currentParagraph);
							}
							ocrx_word(lineNode, currentParagraph);
						}
						paragraphList.add(currentParagraph);
					}
				}
			}
		} catch (Exception e) {
			Logging.error("Unable to read ocr file: " + ocrFile == null ? ""
					: ocrFile.getAbsolutePath() + " " + ocrFile.length());
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(fis);
		}
	}

	public HocrDocument(JSONObject jsonObject) throws SearchLibException {
		this();
		for (Object key : jsonObject.keySet()) {
			JSONArray jsonArray = (JSONArray) jsonObject.get(key);
			List<HocrBox> hocrBox = new ArrayList<HocrBox>(0);
			for (Object obj : jsonArray)
				hocrBox.add(new HocrBox(obj.toString()));
			boxMap.put(key.toString(), hocrBox);
		}
	}

	public void putTextToParserField(ParserResultItem result,
			ParserFieldEnum parserField) {
		for (StringBuilder paragraph : paragraphList)
			result.addField(parserField, paragraph.toString().trim());
	}

	public void putHocrToParserField(ParserResultItem result,
			ParserFieldEnum parserField) {
		result.addField(parserField, getJsonBoxMap().toJSONString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJsonBoxMap() {
		JSONObject jsonObject = new JSONObject();
		for (String word : boxMap.keySet()) {
			JSONArray jsonBoxes = new JSONArray();
			for (HocrBox box : boxMap.get(word))
				jsonBoxes.add(box.toString());
			jsonObject.put(word, jsonBoxes);
		}
		return jsonObject;
	}

	public void addBoxes(String keyword, List<Rectangle> boxList,
			float xFactor, float yFactor) {
		if (keyword == null)
			return;
		keyword = keyword.toLowerCase().trim();
		if (keyword.length() == 0)
			return;
		List<HocrBox> boxes = boxMap.get(keyword);
		if (boxes == null)
			return;
		for (HocrBox box : boxes)
			box.addRectangle(boxList, xFactor, yFactor);
	}

	final public static void main(String[] args) throws SearchLibException {
		HocrDocument hocrDocument = new HocrDocument(new File(
				"/Users/ekeller/Desktop/ossocr1998028053342416847.html"));
		System.out.println(hocrDocument.getJsonBoxMap().size());
	}
}
