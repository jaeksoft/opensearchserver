package com.jaeksoft.searchlib.confluence;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;

/**
 * Action for OpenSearchServer to index
 * */
@SuppressWarnings("serial")
public class OpenSearchServerAction extends ConfluenceActionSupport {

	protected String indexname = null;
	protected String serverurl = null;
	protected String username = null;
	protected String key = null;

	OpenSearchServerSettings settings = new OpenSearchServerSettings();
	ContentPropertyManager contentPropertyManager = (ContentPropertyManager) ContainerManager
			.getComponent("contentPropertyManager");
	PageManager pageManager = (PageManager) ContainerManager
			.getComponent("pageManager");
	SpaceManager spaceManager = (SpaceManager) ContainerManager
			.getComponent("spaceManager");
	SettingsManager settingsManager = (SettingsManager) ContainerManager
			.getComponent("settingsManager");
	private final static String OPENSEARCHSERVER_CONFLUENCE_SETTINGS_SERVER_KEY = "com.jaeksoft.opensearchserver.confluence.serverurl";
	private final static String OPENSEARCHSERVER_CONFLUENCE_SETTINGS_USERNAME_KEY = "com.jaeksoft.opensearchserver.confluence.username";
	private final static String OPENSEARCHSERVER_CONFLUENCE_SETTINGS_INDEXNAME_KEY = "com.jaeksoft.opensearchserver.confluence.indexname";
	private final static String OPENSEARCHSERVER_CONFLUENCE_SETTINGS_LOGINKEY = "com.jaeksoft.opensearchserver.confluence.key";
	Page page = pageManager.getPages(spaceManager.getAllSpaces().get(0), true)
			.get(0);

	@Override
	public String execute() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		OpenSearchServerSettings settings = new OpenSearchServerSettings(
				request);
		if (settings.getAction() != null && settings.getServerurl() != null
				&& settings.getIndexname() != null
				&& settings.getAction().equalsIgnoreCase("index")) {
			OpenSearchServerConfluencePluginManager manager = new OpenSearchServerConfluencePluginManager(
					spaceManager, pageManager, settingsManager);
			manager.createIndexFile();
			manager.update(settings);
		}
		return Action.SUCCESS;
	}

	public void setIndexname(final String indexname) throws IOException,
			ParserConfigurationException, TransformerException {
		contentPropertyManager.setStringProperty(page,
				OPENSEARCHSERVER_CONFLUENCE_SETTINGS_INDEXNAME_KEY, indexname);
		this.indexname = indexname;
	}

	public String getIndexname() {
		if (indexname == null) {
			return contentPropertyManager.getStringProperty(page,
					OPENSEARCHSERVER_CONFLUENCE_SETTINGS_INDEXNAME_KEY);
		} else {
			return indexname;
		}
	}

	public String getServerurl() {
		if (serverurl == null) {
			return contentPropertyManager.getStringProperty(page,
					OPENSEARCHSERVER_CONFLUENCE_SETTINGS_SERVER_KEY);
		} else {
			return serverurl;
		}
	}

	public void setServerurl(String serverurl) {
		contentPropertyManager.setStringProperty(page,
				OPENSEARCHSERVER_CONFLUENCE_SETTINGS_SERVER_KEY, serverurl);
		this.serverurl = serverurl;
	}

	public String getUsername() {
		if (username == null) {
			return contentPropertyManager.getStringProperty(page,
					OPENSEARCHSERVER_CONFLUENCE_SETTINGS_USERNAME_KEY);
		} else {
			return username;
		}
	}

	public void setUsername(String username) {
		contentPropertyManager.setStringProperty(page,
				OPENSEARCHSERVER_CONFLUENCE_SETTINGS_USERNAME_KEY, username);
		this.username = username;
	}

	public String getKey() {
		if (key == null) {
			return contentPropertyManager.getStringProperty(page,
					OPENSEARCHSERVER_CONFLUENCE_SETTINGS_LOGINKEY);
		} else {
			return key;
		}
	}

	public void setKey(String key) {
		contentPropertyManager.setStringProperty(page,
				OPENSEARCHSERVER_CONFLUENCE_SETTINGS_LOGINKEY, key);
		this.key = key;
	}
}