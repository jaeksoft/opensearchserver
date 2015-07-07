/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Event implements Runnable {

	static Logger log = Logger.getLogger("LogTest");

	private final Path path;
	private final WatchService watcher;

	public Event(String filePath) throws IOException {
		path = FileSystems.getDefault().getPath(filePath);
		watcher = FileSystems.getDefault().newWatchService();
		new Register(path);
		System.out.println("Watch " + path);
		new Thread(this).start();
	}

	private class Register extends SimpleFileVisitor<Path> {

		private Register(Path path) throws IOException {
			Files.walkFileTree(path, this);
		}

		@Override
		final public FileVisitResult preVisitDirectory(Path file,
				BasicFileAttributes attrs) throws IOException {
			if (attrs.isDirectory()) {
				file.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	@Override
	public void run() {
		try {
			for (;;) {
				WatchKey key = watcher.take();
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					Kind<?> kind = watchEvent.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW)
						continue;
					Object o = watchEvent.context();
					Path path = (o instanceof Path) ? (Path) o : null;
					if (path != null
							&& kind == StandardWatchEventKinds.ENTRY_CREATE) {
						if (Files.isDirectory((Path) o,
								LinkOption.NOFOLLOW_LINKS))
							new Register((Path) o);
					}
					if (path != null)
						log.info(kind + " " + path.toAbsolutePath());
				}
				key.reset();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Unwatch " + path);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		for (String arg : args) {
			new Event(arg);
		}

	}
}
