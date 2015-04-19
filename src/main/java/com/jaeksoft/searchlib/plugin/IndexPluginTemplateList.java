package com.jaeksoft.searchlib.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexPluginTemplateList {

	private ArrayList<IndexPluginItem> pluginList;

	private IndexPluginTemplateList() {
		pluginList = new ArrayList<IndexPluginItem>();
	}

	private void add(IndexPluginItem item) {
		pluginList.add(item);
	}

	protected Iterator<IndexPluginItem> iterator() {
		return pluginList.iterator();
	}

	public static IndexPluginTemplateList fromXmlConfig(XPathParser xpp,
			Node parentNode) throws XPathExpressionException, DOMException,
			IOException {
		IndexPluginTemplateList indexPluginList = new IndexPluginTemplateList();
		if (parentNode == null)
			return indexPluginList;
		NodeList nodes = xpp.getNodeList(parentNode, "indexPlugin");
		for (int i = 0; i < nodes.getLength(); i++)
			indexPluginList.add(new IndexPluginItem(xpp, nodes.item(i)));
		return indexPluginList;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("indexPlugins");
		for (IndexPluginItem item : pluginList)
			item.writeXmlConfig(writer);
		writer.endElement();
	}

}
