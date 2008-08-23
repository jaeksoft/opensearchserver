package com.jaeksoft.searchlib.util;

import java.io.File;

public class FileUtils {

	public static String locatePath(File homeDir, String path) {
		String rootPath = homeDir == null ? System.getProperty("user.dir")
				: homeDir.getAbsolutePath();
		return path.replace("${root}", rootPath);
	}

}
