package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;

import com.github.jankroken.commandline.CommandLineParser;
import com.github.jankroken.commandline.OptionStyle;
import com.github.jankroken.commandline.annotations.LongSwitch;
import com.github.jankroken.commandline.annotations.Option;
import com.github.jankroken.commandline.annotations.ShortSwitch;
import com.github.jankroken.commandline.annotations.SingleArgument;

public class Server {

	private final Tomcat tomcat;

	private Server(Arguments arguments) throws CmdLineException {
		File baseDir = new File(arguments.extractDirectory == null ? "server" : arguments.extractDirectory);
		if (!baseDir.exists())
			baseDir.mkdir();
		tomcat = new Tomcat();
		tomcat.setPort(arguments.httpPort == null ? 9090 : arguments.httpPort);
		tomcat.setBaseDir(baseDir.getAbsolutePath());
		tomcat.getHost().setAppBase(baseDir.getAbsolutePath());
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setDeployOnStartup(true);
		tomcat.getConnector().setURIEncoding(arguments.uriEncoding == null ? "UTF-8" : arguments.uriEncoding);
	}

	public static class Arguments {

		private String extractDirectory = null;
		private Integer httpPort = null;
		private String uriEncoding = null;

		@Option
		@LongSwitch("extractDirectory")
		@ShortSwitch("e")
		@SingleArgument
		public void setExtractDirectory(String extractDirectory) {
			this.extractDirectory = extractDirectory;
		}

		@Option
		@LongSwitch("httpPort")
		@ShortSwitch("p")
		@SingleArgument
		public void setHttpPort(String port) {
			this.httpPort = port == null ? null : Integer.parseInt(port);
		}

		@Option
		@LongSwitch("extractDirectory")
		@ShortSwitch("e")
		@SingleArgument
		public void setUriEncoding(String uriEncoding) {
			this.uriEncoding = uriEncoding;
		}

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

	public static void main(String[] args) throws IllegalAccessException, InstantiationException,
			InvocationTargetException, IOException, URISyntaxException, CmdLineException {
		Arguments arguments = CommandLineParser.parse(Arguments.class, args, OptionStyle.SIMPLE);
		new Server(arguments).start();
	}
}
