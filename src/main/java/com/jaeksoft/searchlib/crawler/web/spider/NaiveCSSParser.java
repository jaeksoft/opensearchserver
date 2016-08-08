package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;

public class NaiveCSSParser {

	private static Pattern commentLocator = Pattern.compile("(?s)/\\*.*?\\*/");

	private static String removeComments(String css) {
		Matcher matcher = commentLocator.matcher(css);

		StringBuilder sb = new StringBuilder();
		int pos = 0;
		while (matcher.find()) {
			sb.append(css.substring(pos, matcher.start()));
			pos = matcher.end();
		}
		if (pos < css.length())
			sb.append(css.substring(pos, css.length()));
		return sb.toString();
	}

	public abstract class CSSRule {

		protected CSSRule(Integer pos) {
			rules.put(pos, this);
		}

		protected abstract void write(PrintWriter pw);
	}

	public class CSSProperty {

		private final String name;
		private String value;

		protected CSSProperty(String property) {
			int i = property.indexOf(':');
			if (i == -1) {
				this.name = property;
				this.value = null;
			} else {
				this.name = property.substring(0, i);
				this.value = property.substring(i + 1);

			}
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public void write(PrintWriter pw) {
			pw.print(name);
			if (value != null) {
				pw.print(":");
				pw.print(value);
			}
		}

		public void write(StringBuilder sb) {
			sb.append(name);
			sb.append(":");
			sb.append(value);
		}
	}

	public class CSSStyleRule extends CSSRule {

		private final String selector;
		private final List<CSSProperty> properties;

		protected CSSStyleRule(int pos, String selector, String properties) {
			super(pos);
			this.selector = selector;
			this.properties = new ArrayList<CSSProperty>(0);
			String[] propArray = StringUtils.split(properties, ";");
			for (String property : propArray)
				this.properties.add(new CSSProperty(property));
		}

		protected CSSStyleRule(String properties) {
			this(0, null, properties);
		}

		public List<CSSProperty> getProperties() {
			return properties;
		}

		@Override
		public void write(PrintWriter pw) {
			if (StringUtils.isEmpty(selector))
				return;
			pw.print(selector);
			pw.print("{");
			boolean first = true;
			for (CSSProperty property : properties) {
				if (first)
					first = false;
				else
					pw.write(';');
				property.write(pw);
			}
			pw.println("}");
		}

		public String getPropertyString() {
			StringBuilder sb = new StringBuilder();
			for (CSSProperty property : properties)
				property.write(sb);
			return sb.toString();
		}
	}

	public class CSSAtRule extends CSSRule {

		private final String atRule;

		private final String atProperty;

		private final boolean withSemiColon;

		protected CSSAtRule(int pos, String atRule, String atProperty,
				boolean withSemiColon) {
			super(pos);
			this.withSemiColon = withSemiColon;
			this.atRule = atRule;
			this.atProperty = atProperty;
		}

		@Override
		public void write(PrintWriter pw) {
			pw.print(atRule);
			pw.print(' ');
			pw.print(atProperty);
			if (withSemiColon)
				pw.print(';');
			pw.println();
		}

	}

	final public static Pattern cssUrlPattern = Pattern
			.compile("(?s)[\\s]*url\\([\"']?(.*?)[\"']?\\)");

	public final static Matcher findUrl(String propertyValue) {
		synchronized (cssUrlPattern) {
			return cssUrlPattern.matcher(propertyValue);
		}
	}

	public final static String replaceUrl(String value, Matcher matcher,
			String url) {
		StringBuilder sb = new StringBuilder(value.substring(0,
				matcher.start(1)));
		sb.append(url);
		sb.append(value.substring(matcher.end(1)));
		return sb.toString();
	}

	public class CSSImportRule extends CSSRule {

		private String href = null;

		private List<String> medias = null;

		protected CSSImportRule(int pos, String atRule, String atProperty) {
			super(pos);
			String[] parms = StringUtils.split(atProperty);
			if (parms == null)
				return;
			if (parms.length == 0)
				return;
			href = parms[0];
			Matcher matcher = findUrl(href);
			if (matcher.find())
				href = matcher.group(1);
			else {
				if ((href.startsWith("\"") && href.endsWith("\""))
						|| (href.startsWith("'") && href.endsWith("'")))
					href = href.substring(1, href.length() - 1);
			}
			if (parms.length == 1)
				return;
			medias = new ArrayList<String>(parms.length - 1);
			for (int i = 1; i < parms.length; i++)
				medias.add(parms[i]);
		}

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		@Override
		public void write(PrintWriter pw) {
			pw.print("@import");
			if (href != null) {
				pw.print(" url('");
				pw.print(href);
				pw.print("')");
			}
			if (medias != null && medias.size() > 0) {
				pw.print(" ");
				pw.print(StringUtils.join(medias, ' '));
			}
			pw.println(';');
		}
	}

	// Find At Rule with ;
	private static Pattern atRuleLocator = Pattern
			.compile("(?s)\\s*[\\};]*\\s*(@[a-zA-Z0-9\\~,\\^\\*\\-_\\.#:\\(\\)\\s]*)\\s+([^;]*);");

	// Find At Rule followed by block
	private static Pattern atRuleBlockLocator = Pattern
			.compile("(?s)\\s*[\\};]*\\s*(@[a-zA-Z0-9\\~,\\^\\*\\-_\\.#:\\(\\)\\s]*)\\s+([^;]*)[^;]*$");

	// Find Styled Rule followed by block
	private static Pattern ruleLocator = Pattern
			.compile("(?s)\\s*([a-zA-Z0-9\\~,\\^\\*\\-\\+_\\.#:\\(\\)\\s\"=\\[\\]<>]*)\\s*$");

	private class Block {

		public final String css;
		public final int prev;
		public final int start;
		public int end;
		public final int depth;
		public Block next;

		private Block(String css, int prev, int start, int depth,
				Block previousBlock) {
			this.css = css;
			this.prev = prev;
			this.start = start;
			this.depth = depth;
			this.next = null;
			if (previousBlock != null)
				previousBlock.next = this;
		}

		public void findEnd() {
			Block block = next;
			if (block == null) {
				end = css.length();
				return;
			}
			while (block != null) {
				if (block.depth == depth) {
					end = block.prev;
					break;
				}
				block = block.next;
			}
		}

		@Override
		public String toString() {
			return depth + " : " + prev + " - " + start + " - " + end;
		}

		public String toPrev() {
			return css.substring(prev, start).trim();
		}

		public String toBlock(boolean in) {
			try {
				if (end == start)
					return "";
				return css.substring(start + (in ? 1 : 0), end - (in ? 1 : 0))
						.trim();
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				Logging.warn(this, e);
				return "";
			}
		}

		public Block nextSameDepth() {
			Block block = next;
			while (block != null)
				if (block.depth == depth)
					break;
				else
					block = block.next;
			return block;
		}

		public Block analyze() {
			String prevText = toPrev();
			Matcher matcher;
			synchronized (atRuleLocator) {
				matcher = atRuleLocator.matcher(prevText);
			}
			while (matcher.find()) {
				int offset = prev + matcher.start();
				String atRule = matcher.group(1);
				String atProperty = matcher.group(2);
				if ("@import".equalsIgnoreCase(atRule))
					new CSSImportRule(offset, atRule, atProperty);
				else
					new CSSAtRule(offset, atRule, atProperty, true);
			}
			synchronized (atRuleBlockLocator) {
				matcher = atRuleBlockLocator.matcher(prevText);
			}
			if (matcher.find()) {
				new CSSAtRule(prev + matcher.start(), matcher.group(),
						toBlock(false), false);
				return nextSameDepth();
			}
			synchronized (ruleLocator) {
				matcher = ruleLocator.matcher(prevText);
			}
			if (matcher.find())
				new CSSStyleRule(prev + matcher.start(), matcher.group(),
						toBlock(true));
			return next;
		}
	}

	public Block parseBlocks(String css) {
		int depth = 0;
		int pos = 0;
		int prev = 0;
		Block rootBlock = null;
		Block previousBlock = null;
		for (char c : css.toCharArray()) {
			switch (c) {
			case '{':
				previousBlock = new Block(css, prev, pos, ++depth,
						previousBlock);
				prev = pos + 1;
				if (rootBlock == null)
					rootBlock = previousBlock;
				break;
			case '}':
				prev = pos + 1;
				if (depth > 0)
					depth--;
				break;
			}
			pos++;
		}
		if (rootBlock == null)
			rootBlock = new Block(css, 0, css.length(), 0, null);
		Block block = rootBlock;
		while (block != null) {
			block.findEnd();
			block = block.next;
		}
		return rootBlock;
	}

	private final TreeMap<Integer, CSSRule> rules;

	public NaiveCSSParser() {
		rules = new TreeMap<Integer, CSSRule>();
	}

	public Collection<CSSRule> parseStyleSheet(String css) throws IOException,
			SearchLibException {
		css = removeComments(css);
		Block rootBlock = parseBlocks(css);
		Block block = rootBlock;
		while (block != null)
			block = block.analyze();
		return rules.values();
	}

	public CSSStyleRule parseStyleAttribute(String style) {
		return new CSSStyleRule(style);
	}

	public void write(PrintWriter pw) {
		for (CSSRule rule : rules.values())
			rule.write(pw);
	}

	private final static String[] tests = {
			"@charset UTF-8; \n"
					+ "@import url(\"import1.css\");"
					+ "html { color: #00000f } \n"
					+ "body { background: rgb(255, 255, 255) }input[type=\"submit\"]{cursor:pointer} "
					+ "@charset UTF-8; \n"
					+ ".test {background-image:url(\"http://cache.20minutes.fr/images/homepage/skins/play.png\"),-webkit-linear-gradient(top,rgba(255,255,255,0.1)}\n"
					+ "/* test comment */"
					+ "html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, input, button, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video { margin: 0; padding: 0 }\n "
					+ "@media all and (orientation:portrait) {}\n"
					+ "@media all and (orientation:landscape) {}\n"
					+ "@media print {* {background:transparent !important;color:black !important;text-shadow:none !important;filter:none !important;-ms-filter:none !important}\n"
					+ "tr,img {page-break-inside:avoid}}\n"
					+ "table { border-collapse: collapse; border-spacing: 0 }/*test2 comment*/\n"
					+ "article, aside, footer, header, hgroup, nav, section, figure, figcaption, embed, video, audio, details { display: block }",
			".social .scoopit{margin-right:-26px;z-index:999;}#divgauche .barre-sociale .social .pinterest .at_PinItButton{display:block;width:30px;height:26px; line-height:26px;padding:0;margin:0;background-image:url(/Images/Commun/pictos/picto_pinterest.gif);background-repeat:no-repeat;background-position:0 0;font:11px Arial,Helvetica,sans-serif;text-indent:-9999em;font-size:.01em;color:#CD1F1F;}"
					+ "#divgauche .barre-sociale .social .pinterest .at_PinItButton:hover{background-position:-30 0;}#divgauche .footer .addthis_toolbox.addthis_default_style span{line-height:15px;}#divgauche .footer .social li{display:inline;float:left;}"
					+ "#divgauche .dossier.sommaire .contenu>h3{text-transform:uppercase;color:#EB834F;font-size:20px;display:inline-block;margin-bottom:10px;font-weight:normal;background:none;padding:0;}#divgauche .dossier.sommaire .contenu>ul li{clear:both;}#divgauche .dossier.sommaire .chapo,#divgauche .dossier.sommaire .chapo+p{line-height:20px;}",
			".gfk:hover {text-decoration	:	underline; color		:	#e95e0f;background	:	transparent;}\n"
					+ "body\n{\n\nmargin: 0;\n}"

	};

	public static void test(String cssContent) throws IOException,
			SearchLibException {
		NaiveCSSParser parser = new NaiveCSSParser();
		parser.parseStyleSheet(cssContent);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		parser.write(pw);
	}

	public static void main(String[] args) throws IOException,
			SearchLibException {
		for (String test : tests)
			test(test);

		NaiveCSSParser parser = new NaiveCSSParser();
		CSSStyleRule rule = parser
				.parseStyleAttribute("background-image:transparent url(\"http://cache.20minutes.fr/images/homepage/skins/play.png\")}");
		for (CSSProperty property : rule.getProperties()) {
			String value = property.getValue();
			Matcher matcher = NaiveCSSParser.findUrl(value);
			if (matcher.find())
				property.setValue(replaceUrl(value, matcher, "newurl.png"));
		}
		System.out.println(rule.getPropertyString());
	}
}
