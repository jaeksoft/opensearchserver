/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.util.IOUtils;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class HadoopManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private File propFile;

	private Properties properties;

	private final static String CONFIGURATION_FILE = "hadoop.xml";

	public HadoopManager(File dataDir) throws InvalidPropertiesFormatException,
			IOException {
		propFile = new File(dataDir, CONFIGURATION_FILE);
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propFile);
				properties.loadFromXML(inputStream);
			} finally {
				if (inputStream != null)
					IOUtils.closeQuietly(inputStream);
			}
		}
	}

	public void check() throws IOException {
		rwl.r.lock();
		FileSystem fs = null;
		try {
			Configuration conf = new Configuration();
			fs = FileSystem.get(conf);
			System.out.println("CHECK HADOOP " + fs.getHomeDirectory());
			write(fs, "OSS_FILE_TEST", "OSS STRING TEST", true);
			System.out.println(read(fs, "OSS_FILE_TEST"));
		} finally {
			rwl.r.unlock();
			if (fs != null)
				IOUtils.closeQuietly(fs);
		}
	}

	private String read(FileSystem fs, String path) throws IOException {
		Path fsPath = new Path(path);
		if (!fs.exists(fsPath))
			throw new IOException("Input file not found: " + path);
		FSDataInputStream in = fs.open(fsPath);
		try {
			return in.readUTF();
		} finally {
			if (in != null)
				IOUtils.closeQuietly(in);
		}
	}

	private void write(FileSystem fs, String path, String content,
			boolean replace) throws IOException {
		Path fsPath = new Path(path);
		if (fs.exists(fsPath)) {
			if (!replace)
				throw new IOException("Output already exists: " + path);
			fs.delete(fsPath, false);
		}
		FSDataOutputStream out = fs.create(fsPath);
		try {
			out.writeUTF(content);
		} finally {
			if (out != null)
				IOUtils.closeQuietly(out);
		}
	}

	public void save() throws IOException {
		rwl.w.lock();
		try {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(propFile);
				properties.storeToXML(fos, "");
			} finally {
				if (fos != null)
					IOUtils.closeQuietly(fos);
			}
		} finally {
			rwl.w.unlock();
		}
	}
}
