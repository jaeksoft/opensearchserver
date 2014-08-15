/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class RendererFilter {

	private String publicName;

	private String fieldName;

	private String properties;

	private boolean replaceRequestFacet;

	private RendererFilterType filterType;

	private RendererFilterInterface rendererFilterInstance = null;

	private final static String RENDERER_FILTER_ATTR_PUBLICNAME = "publicName";

	private final static String RENDERER_FILTER_ATTR_FIELDNAME = "fieldName";

	private final static String RENDERER_FILTER_ATTR_REPLACE = "replace";

	private final static String RENDERER_FILTER_ATTR_FILTERTYPE = "filterType";

	public RendererFilter() {
		publicName = StringUtils.EMPTY;
		fieldName = StringUtils.EMPTY;
		filterType = RendererFilterType.DATE;
		replaceRequestFacet = false;
		properties = StringUtils.EMPTY;
	}

	public RendererFilter(XPathParser xpp, Node node)
			throws XPathExpressionException {
		publicName = XPathParser.getAttributeString(node,
				RENDERER_FILTER_ATTR_PUBLICNAME);
		fieldName = XPathParser.getAttributeString(node,
				RENDERER_FILTER_ATTR_FIELDNAME);
		filterType = RendererFilterType.find(XPathParser.getAttributeString(
				node, RENDERER_FILTER_ATTR_FILTERTYPE));
		replaceRequestFacet = DomUtils.getAttributeBoolean(node,
				RENDERER_FILTER_ATTR_REPLACE, false);
		setProperties(node.getTextContent());
	}

	public RendererFilter(RendererFilter filter) {
		filter.copyTo(this);
	}

	public void copyTo(RendererFilter target) {
		target.publicName = publicName;
		target.fieldName = fieldName;
		target.filterType = filterType;
		target.replaceRequestFacet = replaceRequestFacet;
		target.properties = properties;
	}

	public void writeXml(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, RENDERER_FILTER_ATTR_PUBLICNAME,
				publicName, RENDERER_FILTER_ATTR_FIELDNAME, fieldName,
				RENDERER_FILTER_ATTR_FILTERTYPE, filterType.name(),
				RENDERER_FILTER_ATTR_REPLACE,
				Boolean.toString(replaceRequestFacet));
		xmlWriter.textNode(properties);
		xmlWriter.endElement();
	}

	/**
	 * @return the publicName
	 */
	public String getPublicName() {
		return publicName;
	}

	/**
	 * @param publicName
	 *            the publicName to set
	 */
	public void setPublicName(String publicName) {
		this.publicName = publicName;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
		this.rendererFilterInstance = null;
	}

	/**
	 * @return the filterType
	 */
	public RendererFilterType getFilterType() {
		return filterType;
	}

	/**
	 * @return the replaceRequestFacet
	 */
	public boolean isReplaceRequestFacet() {
		return replaceRequestFacet;
	}

	/**
	 * @param replaceRequestFacet
	 *            the replaceRequestFacet to set
	 */
	public void setReplaceRequestFacet(boolean replaceRequestFacet) {
		this.replaceRequestFacet = replaceRequestFacet;
	}

	/**
	 * @param filterType
	 *            the filterType to set
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void setFilterType(RendererFilterType filterType) {
		this.filterType = filterType;
		this.rendererFilterInstance = null;
	}

	/**
	 * @return the properties
	 */
	public String getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(String properties) {
		this.properties = properties;
		this.rendererFilterInstance = null;
	}

	private final RendererFilterInterface getRenderFilterInstance()
			throws InstantiationException, IllegalAccessException, IOException {
		if (rendererFilterInstance == null) {
			rendererFilterInstance = filterType.newInstance();
			rendererFilterInstance.init(fieldName, properties);
		}
		return rendererFilterInstance;
	}

	public void setDefaultProperties() throws InstantiationException,
			IllegalAccessException, IOException {
		if (filterType == null)
			return;
		properties = getRenderFilterInstance().getDefaultProperties();
	}

	public List<RendererFilterItem> getFilterItems(AbstractResultSearch result)
			throws InstantiationException, IllegalAccessException, IOException {
		if (filterType == null)
			return null;
		List<RendererFilterItem> filterItem = new ArrayList<RendererFilterItem>();
		getRenderFilterInstance().populate(result, filterItem);
		return filterItem;
	}

	public boolean isReplacement(String fieldName) {
		if (!replaceRequestFacet)
			return false;
		if (this.fieldName == null)
			return false;
		return this.fieldName.equals(fieldName);
	}

}
