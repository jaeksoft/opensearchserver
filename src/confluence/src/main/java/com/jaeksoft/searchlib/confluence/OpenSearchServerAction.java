package com.jaeksoft.searchlib.confluence;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;

/**
 * Action for OpenSearchServer to index
 * */
public class OpenSearchServerAction extends ConfluenceActionSupport {

	@Override
	public String execute() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		String action = request.getParameter("action");
		String serverurl = request.getParameter("serverurl");
		String indexname = request.getParameter("indexname");
		String username = request.getParameter("username");
		String key = request.getParameter("key");

		if (action != null && serverurl != null && indexname != null
				&& action.equalsIgnoreCase("index")) {
			SpaceManager spaceManager = (SpaceManager) ContainerManager
					.getComponent("spaceManager");
			PageManager pageManager = (PageManager) ContainerManager
					.getComponent("pageManager");
			SettingsManager settingsManager = (SettingsManager) ContainerManager
					.getComponent("settingsManager");
			OpenSearchServerConfluencePluginManager manager = new OpenSearchServerConfluencePluginManager(
					spaceManager, pageManager, settingsManager);
			manager.createIndexFile();
			manager.update(serverurl, indexname, username, key);

		}
		return Action.SUCCESS;
	}
}