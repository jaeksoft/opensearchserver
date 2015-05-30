/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;

import com.jaeksoft.searchlib.web.StartStopListener;

public class ExecuteUtils {

	final public static String getClassPath() {
		List<String> classPathes = new ArrayList<String>(2);
		if (StartStopListener.REALPATH_WEBINF_CLASSES != null)
			classPathes.add(StartStopListener.REALPATH_WEBINF_CLASSES);
		if (StartStopListener.REALPATH_WEBINF_LIB != null)
			classPathes.add(StartStopListener.REALPATH_WEBINF_LIB + "/*");
		return StringUtils.join(classPathes, File.pathSeparator);
	}

	final public static int command(File workingDirectory, String cmd,
			String classpath, boolean setJavaTempDir,
			OutputStream outputStream, OutputStream errorStream, Long timeOut,
			String... arguments) throws ExecuteException, IOException {
		Map<String, String> envMap = null;
		if (classpath != null) {
			envMap = new HashMap<String, String>();
			envMap.put("CLASSPATH", classpath);
		}
		CommandLine commandLine = new CommandLine(cmd);
		if (setJavaTempDir)
			if (!StringUtils.isEmpty(SystemUtils.JAVA_IO_TMPDIR))
				commandLine
						.addArgument(StringUtils
								.fastConcat("-Djava.io.tmpdir=",
										SystemUtils.JAVA_IO_TMPDIR), false);
		if (arguments != null)
			for (String argument : arguments)
				commandLine.addArgument(argument);
		DefaultExecutor executor = new DefaultExecutor();
		if (workingDirectory != null)
			executor.setWorkingDirectory(workingDirectory);
		if (outputStream != null) {
			PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(
					outputStream, errorStream);
			executor.setStreamHandler(pumpStreamHandler);
		}
		if (timeOut != null) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut);
			executor.setWatchdog(watchdog);
		}
		return envMap != null ? executor.execute(commandLine, envMap)
				: executor.execute(commandLine);
	}

	final public static int run(List<String> args, int secTimeOut,
			StringBuilder returnedText, int... expectedExitValues)
			throws InterruptedException, IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		if (!waitFor(process, secTimeOut))
			throw new ExecutionException("TimeOut reached", null,
					processBuilder, null);

		returnedText = IOUtils.copy(process.getInputStream(), returnedText,
				"UTF-8", true);
		int exitValue = process.exitValue();
		if (expectedExitValues != null) {
			boolean found = false;
			for (int expectedExitValue : expectedExitValues)
				if (expectedExitValue == exitValue) {
					found = true;
					break;
				}
			if (!found)
				throw new ExecutionException("Wrong exit value: " + exitValue,
						exitValue, processBuilder, returnedText);
		}
		return exitValue;
	}

	/**
	 * Backport from Java 1.8 Wait for the completion of the process
	 * 
	 * @param process
	 *            the process to wait for
	 * @param secTimeOut
	 *            the maximum number of seconds to wait for
	 * @return true if the process returned an exit code
	 * @throws InterruptedException
	 */
	public static boolean waitFor(Process process, int secTimeOut)
			throws InterruptedException {
		long last = System.currentTimeMillis() + secTimeOut * 1000;
		do {
			try {
				process.exitValue();
				return true;
			} catch (IllegalThreadStateException ex) {
				Thread.sleep(100);
			}
		} while (System.currentTimeMillis() < last);
		return false;
	}

	public static class ExecutionException extends IOException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9085620726141484988L;

		private final String commandLine;

		private final Integer exitValue;

		private final String returnedText;

		private ExecutionException(String msg, Integer exitValue,
				ProcessBuilder processBuilder, StringBuilder returnedText) {
			super(msg);
			this.exitValue = exitValue;
			this.commandLine = processBuilder == null ? null : StringUtils
					.join(processBuilder.command(), ' ');
			this.returnedText = returnedText == null ? null : returnedText
					.toString();
		}

		public String getReturnedText() {
			return returnedText;
		}

		public String getCommandLine() {
			return commandLine;
		}

		public int getExitValue() {
			return exitValue == null ? -1 : exitValue;
		}
	}
}