package com.jaeksoft.searchlib.crawler.database;

import java.io.File;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManager;
import com.jaeksoft.searchlib.crawler.database.url.UrlManager;
import com.jaeksoft.searchlib.util.XPathParser;

public abstract class CrawlDatabase {

	public static CrawlDatabase fromXmlConfig(Node node, File homeDir) {
		String databaseDriver = XPathParser.getAttributeString(node, "driver");
		if (databaseDriver != null) {
			String databaseUrl = XPathParser.getAttributeString(node, "url");
			if (homeDir != null)
				databaseUrl = databaseUrl.replace("${root}", homeDir
						.getAbsolutePath());
			return new CrawlDatabaseJdbc(databaseDriver, databaseUrl);
		}
		return null; // TODO Berkeley
	}

	public abstract UrlManager getUrlManager();

	public abstract PatternUrlManager getPatternUrlManager();

	public abstract PropertyManager getPropertyManager();

}
