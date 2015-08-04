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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class Event implements Runnable {

	static Logger log = Logger.getLogger("LogTest");

	private final Path rootPath;
	private final WatchService watcher;

	private final HashMap<WatchKey, Path> keys;

	private class PathFifoMap {

		private final ReadWriteLock rwl;
		private final LinkedHashMap<Path, Long> hashMap;

		private PathFifoMap() {
			rwl = new ReadWriteLock();
			hashMap = new LinkedHashMap<Path, Long>() {

				private static final long serialVersionUID = 2288939087853531613L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<Path, Long> eldest) {
					long t = System.currentTimeMillis();
					return t - t % 60000 != eldest.getValue();
				}
			};
		}

		public Long get(Path dirPath) {
			rwl.r.lock();
			try {
				return hashMap.get(dirPath);
			} finally {
				rwl.r.unlock();
			}
		}

		public void put(Path dirPath, long now) {
			rwl.w.lock();
			try {
				hashMap.put(dirPath, now);
			} finally {
				rwl.w.unlock();
			}
		}
	}

	private final PathFifoMap history;

	/**
	 * An Event class is running thread listening for events in the file system.
	 * 
	 * @param filePath
	 *            The path of the monitored directory
	 * @throws IOException
	 */
	public Event(String filePath) throws IOException {
		rootPath = FileSystems.getDefault().getPath(filePath);
		watcher = FileSystems.getDefault().newWatchService();
		keys = new HashMap<WatchKey, Path>();
		history = new PathFifoMap();
		new Register(rootPath);
		System.out.println("Watch " + rootPath + " " + keys.size());
		new Thread(this).start();
	}

	private class Register extends SimpleFileVisitor<Path> {

		private Register(Path path) {
			try {
				Files.walkFileTree(path, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		final public FileVisitResult preVisitDirectory(Path file,
				BasicFileAttributes attrs) throws IOException {
			if (attrs.isDirectory()) {
				keys.put(file.register(watcher,
						StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY), file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	@Override
	public void run() {
		try {

			// Infinite loop.
			for (;;) {
				WatchKey key = watcher.take();
				Path dir = keys.get(key);
				if (dir != null) {

					for (WatchEvent<?> watchEvent : key.pollEvents()) {
						Kind<?> kind = watchEvent.kind();
						if (kind == StandardWatchEventKinds.OVERFLOW)
							continue;
						Object o = watchEvent.context();
						Path file = (o instanceof Path) ? (Path) o : null;
						if (file == null)
							continue;

						Path child = dir.resolve(file);
						// If this is a new directory, we have to register it
						if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
							if (kind == StandardWatchEventKinds.ENTRY_CREATE)
								new Register(child);
					}

					long t = System.currentTimeMillis();
					long now = t - t % 60000;

					Path dirPath = dir.toAbsolutePath();
					Long time = history.get(dirPath);
					if (time == null || time != now) {
						log.info(dirPath);
						history.put(dirPath, now);
					}
				}
				if (!key.reset()) {
					keys.remove(key);
					if (keys.isEmpty())
						break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Unwatch " + rootPath);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		for (String arg : args) {
			new Event(arg);
		}

	}
}
