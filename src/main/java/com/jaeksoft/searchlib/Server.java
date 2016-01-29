package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.Option;

public class Server {

	private final Tomcat tomcat;

	@Option(name = "-extractDirectory", usage = "The server directory")
	public String extractDirectory;

	@Option(name = "-httpPort", usage = "The listening TCP port")
	public Integer httpPort;

	@Option(name = "-uriEncoding", usage = "The URI encoding of the TCP connector")
	public String uriEncoding;

	private Server() {
		File baseDir = new File(extractDirectory == null ? "server" : extractDirectory);
		if (!baseDir.exists())
			baseDir.mkdir();
		tomcat = new Tomcat();
		tomcat.setPort(httpPort == null ? 9090 : httpPort);
		tomcat.setBaseDir(baseDir.getAbsolutePath());
		tomcat.getHost().setAppBase(baseDir.getAbsolutePath());
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setDeployOnStartup(true);
		tomcat.getConnector().setURIEncoding(uriEncoding == null ? "UTF-8" : uriEncoding);
	}

	private void start() throws IOException, URISyntaxException {
		File jarFile = new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		if (!jarFile.exists() || !jarFile.isFile())
			throw new IOException("The jar file has not been found: " + jarFile);
		File warFile = new File(jarFile.getParent(), FilenameUtils.removeExtension(jarFile.getName()) + ".war");
		if (!jarFile.exists() || !jarFile.isFile())
			throw new IOException("The war file has not been found: " + warFile);
		try {
			tomcat.start();
			System.out.println("Tomcat started on " + tomcat.getHost());
		} catch (LifecycleException e) {
			System.err.println("Tomcat could not be started.");
		}
		System.out.println("Loading WAR: " + warFile);
		tomcat.addWebapp(tomcat.getHost(), StringUtils.EMPTY, warFile.getAbsolutePath());
		tomcat.getServer().await();
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		new Server().start();
	}
}
