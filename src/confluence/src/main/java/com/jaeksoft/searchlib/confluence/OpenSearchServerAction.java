package com.jaeksoft.searchlib.confluence;

import javax.servlet.http.HttpServletRequest;

import com.kenai.jffi.PageManager;

/**
 * Action for OpenSearchServer to index
 * */
public class OpenSearchServerAction extends ConfluenceActionSupport {

	@Override
	public String execute() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();

		OpenSearchServerSettings settings = new OpenSearchServerSettings(
				request);

		if (settings.getAction() != null && settings.getServerurl() != null
				&& settings.getIndexname() != null
				&& settings.getAction().equalsIgnoreCase("index")) {
			SpaceManager spaceManager = (SpaceManager) ContainerManager
					.getComponent("spaceManager");
			PageManager pageManager = (PageManager) ContainerManager
					.getComponent("pageManager");
			SettingsManager settingsManager = (SettingsManager) ContainerManager
					.getComponent("settingsManager");
			settingsManager.updateGlobalSettings(
					"com.jaeksoft.opensearchserver.confluence", settings);
			settingsManager.OpenSearchServerConfluencePluginManager manager = new OpenSearchServerConfluencePluginManager(
					spaceManager, pageManager, settingsManager);
			manager.createIndexFile();
			manager.update(settings);

		}
		return Action.SUCCESS;
	}
}