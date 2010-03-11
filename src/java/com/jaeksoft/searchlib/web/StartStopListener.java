package com.jaeksoft.searchlib.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.jaeksoft.searchlib.ClientCatalog;

public class StartStopListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		System.out.println("OSS SHUTDOWN");
		ClientCatalog.closeAll();
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		System.out.println("OSS IS STARTING");
		ClientCatalog.openAll();
	}

}
