package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Server {

	private final Tomcat tomcat;

	@Option(name = "-extractDirectory", usage = "The server directory")
	public String extractDirectory;

	@Option(name = "-httpPort", usage = "The listening TCP port")
	public Integer httpPort;

	@Option(name = "-uriEncoding", usage = "The URI encoding of the TCP connector")
	public String uriEncoding;

	private Server(String[] args) throws CmdLineException {
		CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(args);
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
		final File srcFile = new File(
				Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		if (!srcFile.exists())
			throw new IOException("The jar file or classes directory has not been found: " + srcFile);
		final File webFile;
		if (srcFile.isFile()) {
			webFile = new File(srcFile.getParent(), FilenameUtils.removeExtension(srcFile.getName()) + ".war");
			if (!webFile.exists() || !webFile.isFile())
				throw new IOException("The war file has not been found: " + webFile);
		} else {
			webFile = new File("src/main/webapp");
			if (!webFile.exists() && !webFile.isDirectory())
				throw new IOException("The web content directory has not been found: " + webFile);
		}

		try {
			tomcat.start();
			System.out.println("Tomcat started on " + tomcat.getHost());
		} catch (LifecycleException e) {
			System.err.println("Tomcat could not be started.");
		}
		System.out.println("Loading WebApplication: " + webFile);
		tomcat.addWebapp(tomcat.getHost(), StringUtils.EMPTY, webFile.getAbsolutePath());
		tomcat.getServer().await();
	}

	public static void main(String[] args) throws IOException, URISyntaxException, CmdLineException {
		new Server(args).start();
	}
}
