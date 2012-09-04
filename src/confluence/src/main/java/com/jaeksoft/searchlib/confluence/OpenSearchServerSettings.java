package com.jaeksoft.searchlib.confluence;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

public class OpenSearchServerSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2796886236708531288L;

	private String action;;
	private String serverurl;
	private String indexname;
	private String username;
	private String key;

	public OpenSearchServerSettings() {
	}

	public OpenSearchServerSettings(HttpServletRequest request) {
		action = request.getParameter("action");
		serverurl = request.getParameter("serverurl");
		indexname = request.getParameter("indexname");
		username = request.getParameter("username");
		key = request.getParameter("key");
	}

	private final static String OPENSEARCHSERVER_CONFLUENCE_SETTINGS_KEY = "com.jaeksoft.opensearchserver.confluence";

	public void saveSettings(SettingsManager settingsManager) {
		settingsManager.updateGlobalSettings(
				OPENSEARCHSERVER_CONFLUENCE_SETTINGS_KEY, this);
	}

	public static OpenSearchServerSettings getSettings(
			SettingsManager settingsManager) {
		return (OpenSearchServerSettings) settingsManager
				.getPluginSettings(OPENSEARCHSERVER_CONFLUENCE_SETTINGS_KEY);
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the serverurl
	 */
	public String getServerurl() {
		return serverurl;
	}

	/**
	 * @param serverurl
	 *            the serverurl to set
	 */
	public void setServerurl(String serverurl) {
		this.serverurl = serverurl;
	}

	/**
	 * @return the indexname
	 */
	public String getIndexname() {
		return indexname;
	}

	/**
	 * @param indexname
	 *            the indexname to set
	 */
	public void setIndexname(String indexname) {
		this.indexname = indexname;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

}
