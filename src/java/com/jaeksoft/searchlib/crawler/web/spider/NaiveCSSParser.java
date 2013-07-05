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

import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;

public class NaiveCSSParser {

	private abstract class AbstractParser {

		private final Matcher matcher;
		private final String css;
		private final int offset;

		protected AbstractParser(int offset, final Pattern pattern,
				final String css) throws IOException, SearchLibException {
			this.css = css;
			this.offset = offset;
			synchronized (pattern) {
				matcher = pattern.matcher(css);
			}
		}

		protected void parseOuter() throws IOException, SearchLibException {
			int pos = 0;
			while (matcher.find()) {
				parse(pos + offset, css.substring(pos, matcher.start() - 1));
				pos = matcher.end();
			}
			parse(pos + offset, css.substring(pos, css.length()));

		}

		protected void parseInner() throws IOException, SearchLibException {
			while (matcher.find()) {
				parse(offset + matcher.start(), matcher);
			}
		}

		protected void parse(int pos, String css) throws IOException,
				SearchLibException {

		}

		protected void parse(int pos, Matcher matcher) throws IOException,
				SearchLibException {
		}
	}

	private static Pattern commentLocator = Pattern.compile("(?m)/\\*.*?\\*/");

	private class CommentExtractParser extends AbstractParser {

		protected CommentExtractParser(int offset, String css)
				throws IOException, SearchLibException {
			super(offset, commentLocator, css);
			parseOuter();
		}

		@Override
		protected void parse(int offset, String css) throws IOException,
				SearchLibException {
			new RuleParser(offset, css);
			new AtRuleParser(offset, css);
		}
	}

	private static Pattern ruleLocator = Pattern
			.compile("(?m)\\s*([a-zA-Z0-9,\\*\\-_\\.@#\\s\"=\\[\\]]*)\\s*\\{(.*?)\\}");

	private class RuleParser extends AbstractParser {

		protected RuleParser(int offset, String css) throws IOException,
				SearchLibException {
			super(offset, ruleLocator, css);
			parseInner();
		}

		@Override
		protected void parse(int pos, Matcher matcher) {
			new CSSStyleRule(pos, matcher.group(1), matcher.group(2));
		}
	}

	private static Pattern atRuleLocator = Pattern
			.compile("\\s*[\\};]*\\s*(@[a-zA-Z0-9,\\-_\\.#]*)\\s+([^;]*);");

	private class AtRuleParser extends AbstractParser {

		protected AtRuleParser(int offset, String css) throws IOException,
				SearchLibException {
			super(offset, atRuleLocator, css);
			parseInner();
		}

		@Override
		protected void parse(int offset, Matcher matcher) {
			String atRule = matcher.group(1);
			String atProperty = matcher.group(2);
			if ("@import".equalsIgnoreCase(atRule))
				new CSSImportRule(offset, atRule, atProperty);
			else
				new CSSAtRule(offset, atRule, atProperty);
		}
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

		public void write(StringBuffer sb) {
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
			StringBuffer sb = new StringBuffer();
			for (CSSProperty property : properties)
				property.write(sb);
			return sb.toString();
		}
	}

	public class CSSAtRule extends CSSRule {

		private final String atRule;

		private final String atProperty;

		protected CSSAtRule(int pos, String atRule, String atProperty) {
			super(pos);
			this.atRule = atRule;
			this.atProperty = atProperty;
		}

		@Override
		public void write(PrintWriter pw) {
			pw.print(atRule);
			pw.print(' ');
			pw.print(atProperty);
			pw.println(';');
		}

	}

	final public static Pattern cssUrlPattern = Pattern
			.compile("(?s)[\\s]*url\\([\"']?(.*?)[\"']?\\)");

	public final static Matcher findUrl(String propertyValue) {
		synchronized (cssUrlPattern) {
			return cssUrlPattern.matcher(propertyValue);
		}
	}

	public final static String buildUrl(String url) {
		StringBuffer sb = new StringBuffer("url('");
		sb.append(url);
		sb.append("')");
		return sb.toString();
	}

	public class CSSImportRule extends CSSRule {

		private String href;

		private final String media;

		protected CSSImportRule(int pos, String atRule, String atProperty) {
			super(pos);
			StringUtils.split(atProperty);
			Matcher matcher = findUrl(atProperty);
			if (matcher.find()) {
				href = matcher.group(1);
				media = matcher.replaceFirst("");
			} else {
				href = null;
				media = atProperty;
			}
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
			if (media != null && media.length() > 0) {
				pw.print(" ");
				pw.print(media);
			}
			pw.println(';');
		}
	}

	private final TreeMap<Integer, CSSRule> rules;

	public NaiveCSSParser() {
		rules = new TreeMap<Integer, CSSRule>();
	}

	public Collection<CSSRule> parseStyleSheet(String css) throws IOException,
			SearchLibException {
		new CommentExtractParser(0, css);
		return rules.values();
	}

	public CSSStyleRule parseStyleAttribute(String style) {
		return new CSSStyleRule(style);
	}

	public void write(PrintWriter pw) {
		for (CSSRule rule : rules.values())
			rule.write(pw);
	}

	private final static String test = "@charset UTF-8; \n"
			+ "@import url(\"import1.css\");"
			+ "html { color: #00000f } \n"
			+ "body { background: rgb(255, 255, 255) }input[type=\"submit\"]{cursor:pointer} "
			+ "@charset UTF-8; \n"
			+ ".test {background-image:url(\"http://cache.20minutes.fr/images/homepage/skins/play.png\"),-webkit-linear-gradient(top,rgba(255,255,255,0.1)}\n"
			+ "/* test comment */"
			+ "html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, input, button, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video { margin: 0; padding: 0 } "
			+ "table { border-collapse: collapse; border-spacing: 0 }/*test2 comment*/\n"
			+ "article, aside, footer, header, hgroup, nav, section, figure, figcaption, embed, video, audio, details { display: block }";

	public static void main(String[] args) throws IOException,
			SearchLibException {
		NaiveCSSParser parser = new NaiveCSSParser();
		parser.parseStyleSheet(test);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		parser.write(pw);
		System.out.println(sw);
		CSSStyleRule rule = parser
				.parseStyleAttribute("background-image:url(\"http://cache.20minutes.fr/images/homepage/skins/play.png\")}");
		for (CSSProperty property : rule.getProperties()) {
			Matcher matcher = NaiveCSSParser.findUrl(property.getValue());
			if (matcher.find())
				property.setValue(matcher.replaceFirst(buildUrl("newurl.png")));
		}
		System.out.println(rule.getPropertyString());
	}
}
