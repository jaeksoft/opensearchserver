/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
			boolean putClassPath, boolean setJavaTempDir,
			OutputStream outputStream, OutputStream errorStream, Long timeOut,
			String... arguments) throws ExecuteException, IOException {
		Map<String, String> envMap = null;
		if (putClassPath) {
			envMap = new HashMap<String, String>();
			envMap.put("CLASSPATH", ExecuteUtils.getClassPath());
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
}