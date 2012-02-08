package com.jaeksoft.searchlib.parser.htmlParser;

import java.util.List;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.jaeksoft.searchlib.util.JSoupUtils;

public class JSoupHtmlNode extends HtmlNodeAbstract<Node> {

	public JSoupHtmlNode(Node node) {
		super(node);
	}

	@Override
	public int countElements() {
		return JSoupUtils.countElements(node);
	}

	@Override
	public String getTextNode(String... path) {
		return JSoupUtils.getTextNode(node, path);
	}

	@Override
	public String getNodeValue() {
		Element element = (Element) node;
		return element.text();
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<?>> nodes, String... path) {
		List<Node> nodeList = JSoupUtils.getNodes(node, path);
		for (Node node : nodeList)
			nodes.add(new JSoupHtmlNode(node));
	}

	@Override
	public String getAttributeText(String name) {
		return JSoupUtils.getAttributeText(node, name);
	}

	@Override
	public boolean isComment() {
		if (node instanceof Comment) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isTextNode() {
		if (node instanceof TextNode) {
			return true;
		}
		return false;
	}

	@Override
	public String getNodeName() {
		return node.nodeName();
	}

	@Override
	public String getAttribute(String name) {
		return JSoupUtils.getAttributeText(node, name);
	}

	@Override
	public List<HtmlNodeAbstract<?>> getChildNodes() {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		List<Node> nodeList = node.childNodes();
		int l = nodeList.size();
		for (int i = 0; i < l; i++)
			nodes.add(new JSoupHtmlNode(nodeList.get(i)));
		return nodes;
	}

	@Override
	public List<HtmlNodeAbstract<?>> getAllNodes(String... tags) {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		List<Node> nodeList = JSoupUtils.getAllNodes(node, tags);
		for (Node node : nodeList)
			nodes.add(new JSoupHtmlNode(node));
		return nodes;
	}
}
