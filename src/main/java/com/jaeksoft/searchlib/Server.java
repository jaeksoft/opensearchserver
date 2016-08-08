package com.jaeksoft.searchlib;

import com.github.jankroken.commandline.CommandLineParser;
import com.github.jankroken.commandline.OptionStyle;
import com.github.jankroken.commandline.annotations.*;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.web.StartStopListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class Server {

	private final Tomcat tomcat;

	private Server(Arguments arguments) {
		File baseDir = new File(arguments.extractDirectory == null ? "server" : arguments.extractDirectory);
		if (baseDir.exists())
			if (arguments.resetExtract && baseDir.isDirectory())
				FileUtils.deleteDirectoryQuietly(baseDir);
		if (!baseDir.exists())
			baseDir.mkdir();
		tomcat = new Tomcat();
		tomcat.setHostname(StringUtils.isEmpty(arguments.address) ? "localhost" : arguments.address);
		tomcat.setPort(arguments.httpPort == null ? 9090 : arguments.httpPort);
		tomcat.setBaseDir(baseDir.getAbsolutePath());
		tomcat.getHost().setAppBase(baseDir.getAbsolutePath());
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setDeployOnStartup(true);
		tomcat.getConnector().setURIEncoding(arguments.uriEncoding == null ? "UTF-8" : arguments.uriEncoding);
	}

	public static class Arguments {

		private String extractDirectory = null;
		private String address = null;
		private Integer httpPort = null;
		private String uriEncoding = null;
		private boolean resetExtract = false;

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
		@LongSwitch("address")
		@ShortSwitch("a")
		@SingleArgument
		public void setAddress(String hostname) {
			this.address = hostname;
		}

		@Option
		@LongSwitch("uriEncoding")
		@ShortSwitch("u")
		@SingleArgument
		public void setUriEncoding(String uriEncoding) {
			this.uriEncoding = uriEncoding;
		}

		@Option
		@LongSwitch("resetExtract")
		@ShortSwitch("r")
		@Toggle(true)
		public void setResetExtract(boolean resetExtract) {
			this.resetExtract = resetExtract;
		}
	}

	private void start(final boolean await) throws IOException, URISyntaxException {
		final File srcFile =
				new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
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
		tomcat.addWebapp(null, StringUtils.EMPTY, webFile.getAbsolutePath());

		ThreadUtils.waitUntil(120, new StartStopListener.StartedWaitInterface());
		if (await)
			tomcat.getServer().await();
	}

	public static void start(String[] args, boolean await)
			throws IllegalAccessException, InstantiationException, InvocationTargetException, IOException,
			URISyntaxException {
		if (args == null)
			args = new String[] {};
		Arguments arguments = CommandLineParser.parse(Arguments.class, args, OptionStyle.SIMPLE);
		Server server = new Server(arguments);
		server.start(await);
	}

	public static void main(String[] args)
			throws IllegalAccessException, InstantiationException, InvocationTargetException, IOException,
			URISyntaxException {
		start(args, true);
	}
}
