/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.field;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class RendererField {

	private String fieldName;

	private RendererFieldType fieldType;

	/**
	 * @deprecated
	 */
	private String oldStyle;

	private String urlFieldName;

	private boolean urlDecode;

	private RendererWidgets widgetName;

	private String cssClass;

	private String pattern;

	private String replacement;

	private final static String RENDERER_FIELD_ATTR_FIELDNAME = "fieldName";

	private final static String RENDERER_FIELD_ATTR_FIELD_TYPE = "fieldType";

	private final static String RENDERER_FIELD_NODE_CSS_STYLE = "cssStyle";

	private final static String RENDERER_FIELD_ATTR_CSS_CLASS = "cssClass";

	private final static String RENDERER_FIELD_ATTR_URL_FIELDNAME = "urlFieldName";

	private final static String RENDERER_FIELD_ATTR_URL_DECODE = "urlDecode";

	private final static String RENDERER_FIELD_ATTR_WIDGETNAME = "widgetName";

	private final static String RENDERER_FIELD_ATTR_REGEXP_PATTERN = "regexpPattern";

	private final static String RENDERER_FIELD_ATTR_REGEXP_REPLACE = "regexpReplace";

	public RendererField() {
		fieldName = StringUtils.EMPTY;
		fieldType = RendererFieldType.FIELD;
		oldStyle = StringUtils.EMPTY;
		cssClass = StringUtils.EMPTY;
		urlFieldName = StringUtils.EMPTY;
		urlDecode = false;
		widgetName = RendererWidgets.TEXT;
		pattern = null;
		replacement = null;
	}

	public RendererField(XPathParser xpp, Node node)
			throws XPathExpressionException {
		fieldName = XPathParser.getAttributeString(node,
				RENDERER_FIELD_ATTR_FIELDNAME);
		setFieldType(RendererFieldType.find(XPathParser.getAttributeString(
				node, RENDERER_FIELD_ATTR_FIELD_TYPE)));
		oldStyle = xpp.getSubNodeTextIfAny(node, RENDERER_FIELD_NODE_CSS_STYLE,
				true);
		cssClass = XPathParser.getAttributeString(node,
				RENDERER_FIELD_ATTR_CSS_CLASS);
		urlFieldName = XPathParser.getAttributeString(node,
				RENDERER_FIELD_ATTR_URL_FIELDNAME);
		urlDecode = DomUtils.getAttributeBoolean(node,
				RENDERER_FIELD_ATTR_URL_DECODE, false);
		setWidgetName(RendererWidgets.find(XPathParser.getAttributeString(node,
				RENDERER_FIELD_ATTR_WIDGETNAME)));
		setPattern(DomUtils.getAttributeText(node,
				RENDERER_FIELD_ATTR_REGEXP_PATTERN));
		setReplacement(DomUtils.getAttributeText(node,
				RENDERER_FIELD_ATTR_REGEXP_REPLACE));
	}

	public RendererField(RendererField field) {
		field.copyTo(this);
	}

	public void copyTo(RendererField target) {
		target.fieldName = fieldName;
		target.fieldType = fieldType;
		target.oldStyle = oldStyle;
		target.cssClass = cssClass;
		target.urlFieldName = urlFieldName;
		target.urlDecode = urlDecode;
		target.widgetName = widgetName;
		target.pattern = pattern;
		target.replacement = replacement;
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
	}

	/**
	 * @return the style
	 */
	public String getOldStyle() {
		return oldStyle;
	}

	/**
	 * @return the css class
	 */
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * If the css class is empty, it returns an empty string. If there is
	 * classes, returns a space char followed by the css classes
	 * 
	 * @return
	 */
	public String renderCssClass() {
		if (StringUtils.isEmpty(cssClass))
			return StringUtils.EMPTY;
		return StringUtils.fastConcat(' ', cssClass);
	}

	/**
	 * @return the urlFieldName
	 */
	public String getUrlFieldName() {
		return urlFieldName;
	}

	/**
	 * @param urlFieldName
	 *            the urlFieldName to set
	 */
	public void setUrlFieldName(String urlFieldName) {
		this.urlFieldName = urlFieldName;
	}

	/**
	 * @return the urlDecode
	 */
	public boolean getUrlDecode() {
		return urlDecode;
	}

	/**
	 * @param urlDecode
	 *            the urlDecode to set
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlDecode = urlDecode;
	}

	/**
	 * @param fieldType
	 *            the fieldType to set
	 */
	public void setFieldType(RendererFieldType fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * @return the fieldType
	 */
	public RendererFieldType getFieldType() {
		return fieldType;
	}

	private String[] getValues(List<FieldValueItem> fieldValueItems,
			boolean replace, boolean urlDecode) {
		if (fieldValueItems == null)
			return null;
		replace = replace && !StringUtils.isEmpty(pattern);
		String[] fields = new String[fieldValueItems.size()];
		int i = 0;
		for (FieldValueItem fieldValueItem : fieldValueItems) {
			String value = fieldValueItem.value;
			if (value != null) {
				if (urlDecode)
					value = LinkUtils.UTF8_URL_QuietDecode(value);
				if (replace)
					value = value.replaceAll(pattern, replacement);
			}
			fields[i++] = value;
		}
		return fields;
	}

	final public String[] getFieldValue(ResultDocument resultDocument) {
		if (fieldType == RendererFieldType.FIELD) {
			boolean isUrl = urlFieldName != null
					&& urlFieldName.equals(fieldName);
			boolean replace = StringUtils.isEmpty(urlFieldName) || isUrl;
			return getValues(resultDocument.getValues(fieldName), replace,
					(isUrl || StringUtils.isEmpty(urlFieldName)) && urlDecode);
		} else if (fieldType == RendererFieldType.SNIPPET)
			return getValues(resultDocument.getSnippetValues(fieldName), false,
					false);
		return null;
	}

	final public String getUrlField(ResultDocument resultDocument) {
		if (urlFieldName == null)
			return null;
		String url = resultDocument.getValueContent(urlFieldName, 0);
		if (url == null)
			return null;
		if (url.length() == 0)
			return null;
		if (urlDecode)
			url = LinkUtils.UTF8_URL_QuietDecode(url);
		if (!(StringUtils.isEmpty(pattern))
				&& !(StringUtils.isEmpty(replacement)))
			url = url.replaceAll(pattern, replacement);
		return url;
	}

	public RendererWidgets getWidgetName() {
		return widgetName;
	}

	public void setWidgetName(RendererWidgets widgetName) {
		this.widgetName = widgetName;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void writeXml(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, RENDERER_FIELD_ATTR_FIELDNAME,
				fieldName, RENDERER_FIELD_ATTR_FIELD_TYPE, fieldType.name(),
				RENDERER_FIELD_ATTR_URL_FIELDNAME, urlFieldName,
				RENDERER_FIELD_ATTR_URL_DECODE, Boolean.toString(urlDecode),
				RENDERER_FIELD_ATTR_CSS_CLASS, cssClass,
				RENDERER_FIELD_ATTR_WIDGETNAME, widgetName.name(),
				RENDERER_FIELD_ATTR_REGEXP_PATTERN, pattern,
				RENDERER_FIELD_ATTR_REGEXP_REPLACE, replacement);
		xmlWriter.endElement();
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the replacement
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * @param replacement
	 *            the replacement to set
	 */
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}
