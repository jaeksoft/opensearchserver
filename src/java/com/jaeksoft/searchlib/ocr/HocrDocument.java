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

package com.jaeksoft.searchlib.ocr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.htmlParser.DomHtmlNode;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlNodeAbstract;
import com.jaeksoft.searchlib.util.DomUtils;

public class HocrDocument {

	private List<StringBuffer> paragraphList;

	private Map<String, List<HocrBox>> boxMap;

	public HocrDocument(File ocrFile) throws SearchLibException {
		FileInputStream fis = null;
		try {

			paragraphList = new ArrayList<StringBuffer>(0);
			boxMap = new TreeMap<String, List<HocrBox>>();

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
						StringBuffer currentParagraph = new StringBuffer();
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
								String bbox = wordNode
										.getAttributeText("title").substring(5);
								for (HtmlNodeAbstract<?> xwordNode : wordNode
										.getNodes("span")) {
									if (!"ocrx_word".equals(xwordNode
											.getAttributeText("class")))
										continue;
									String word = xwordNode.getText();
									List<HocrBox> boxList = boxMap.get(word);
									if (boxList == null) {
										boxList = new ArrayList<HocrBox>();
										boxMap.put(word, boxList);
									}
									boxList.add(new HocrBox(bbox));
									currentParagraph.append(word);
									currentParagraph.append(' ');
								}
							}
						}
						paragraphList.add(currentParagraph);
					}
				}
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			if (fis != null)
				IOUtils.closeQuietly(fis);
		}
	}

	public void putContentToParserField(Parser parser,
			ParserFieldEnum parserField) {
		for (StringBuffer paragraph : paragraphList)
			parser.addField(parserField, paragraph.toString().trim());
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
}
