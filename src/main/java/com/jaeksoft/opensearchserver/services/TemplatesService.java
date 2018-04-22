/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.utils.IOUtils;
import freemarker.cache.TemplateLoader;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TemplatesService implements Closeable {

	private final static String TEMPLATES_DIRECTORY = "templates";

	private final StoreServiceInterface storeService;

	private final ConcurrentHashMap<Pair<UUID, String>, Tool> tools;

	public TemplatesService(final StoreServiceInterface storeService) {
		this.storeService = storeService;
		tools = new ConcurrentHashMap<>();
	}

	private Tool getTool(final UUID accountId, final String indexName) {
		return tools.computeIfAbsent(Pair.of(accountId, indexName), key -> new Tool(accountId, indexName));
	}

	public FreeMarkerTool getFreeMarkerTool(final UUID accountId, final String indexName) {
		return getTool(accountId, indexName).freemarkerTool;
	}

	public String getTemplateSource(final UUID accountId, final String indexName, final String templatePath)
			throws IOException {
		return getTool(accountId, indexName).getTemplateSource(templatePath);
	}

	public void setTemplateSource(final UUID accountId, final String indexName, final String templatePath,
			final String content) throws IOException {
		getTool(accountId, indexName).loader.saveTemplateSource(templatePath, content);
	}

	public void deleteTemplateSource(final UUID accountId, final String indexName, final String templatePath) {
		getTool(accountId, indexName).loader.deleteTemplateSource(templatePath);
	}

	@Override
	public synchronized void close() {
		tools.forEachValue(0, Tool::close);
		tools.clear();
	}

	/**
	 * Remove expired service (not used since 5 minutes)
	 *
	 * @return the number of evicted services
	 */
	public synchronized int removeExpired() {
		final List<Pair<UUID, String>> expired = new ArrayList<>();
		final long refTime = TimeUnit.MINUTES.toMillis(5);
		tools.forEach((k, v) -> {
			if (v.loader.hasExpired(refTime))
				expired.add(k);
		});
		expired.forEach(key -> {
			final Tool tool = tools.remove(key);
			if (tool != null)
				tool.close();
		});
		return expired.size();
	}

	class Tool implements Closeable {

		private final FreeMarkerTool freemarkerTool;
		private final Loader loader;

		Tool(final UUID accountId, final String indexName) {
			loader = new TemplatesService.Loader(accountId, indexName);
			freemarkerTool = FreeMarkerTool.of()
					.defaultContentType("text/html")
					.defaultEncoding("UTF-8")
					.templateLoader(loader)
					.build();
			freemarkerTool.load();
		}

		@Override
		public synchronized void close() {
			freemarkerTool.close();
		}

		String getTemplateSource(String templatePath) throws IOException {
			final TemplateSource templateSource = loader.findTemplateSource(templatePath);
			if (templateSource == null)
				return null;
			try (final Reader reader = loader.getReader(templateSource, "UTF-8")) {
				return IOUtils.toString(reader);
			}
		}

	}

	class Loader extends UsableService implements TemplateLoader {

		private final String accountSchema;
		private final String templatesPath;

		Loader(final UUID accountId, final String indexName) {
			this.accountSchema = accountId.toString();
			this.templatesPath = TEMPLATES_DIRECTORY + '/' + indexName + '/';
		}

		@Override
		public TemplateSource findTemplateSource(final String name) throws IOException {
			try {
				final String templatePath = templatesPath + name + ".gz";
				final Response response =
						ErrorWrapper.bypass(() -> storeService.headFile(accountSchema, templatePath), 404);
				if (response == null)
					return DEFAULT_TEMPLATE_CONTENT.get(name);
				final String type = response.getHeaderString("X-QWAZR-Store-Type");
				if (!"FILE".equals(type))
					return null;
				return new TemplateSource(templatePath, response.getLastModified(), null);
			} catch (WebApplicationException e) {
				throw new IOException(e);
			}
		}

		void saveTemplateSource(final String name, final String content) throws IOException {
			final String templatePath = templatesPath + name + ".gz";
			try (final InputStream input = new ByteArrayInputStream(compress(content))) {
				storeService.putFile(accountSchema, templatePath, input, System.currentTimeMillis());
			}
		}

		void deleteTemplateSource(final String name) {
			final String templatePath = templatesPath + name + ".gz";
			ErrorWrapper.bypass(() -> storeService.deleteFile(accountSchema, templatePath), 404);
		}

		@Override
		public long getLastModified(final Object templateSource) {
			return ((TemplateSource) templateSource).lastModified;
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException {
			try {
				final TemplateSource template = (TemplateSource) templateSource;
				if (template.defaultContent != null)
					return new StringReader(template.defaultContent);
				return new InputStreamReader(new GZIPInputStream(
						new BufferedInputStream(storeService.getFile(accountSchema, template.templatePath))), encoding);
			} catch (WebApplicationException e) {
				throw new IOException(e);
			}
		}

		@Override
		public void closeTemplateSource(Object templateSource) {
		}

	}

	private final static String[] TEMPLATES_NAMES = { "form", "result" };
	private final static Map<String, String> TEMPLATES;
	private final static Map<String, TemplateSource> DEFAULT_TEMPLATE_CONTENT;

	static {
		final Map<String, String> namesMap = new LinkedHashMap<>();
		final Map<String, TemplateSource> templatesMap = new HashMap<>();
		try {
			for (final String templateName : TEMPLATES_NAMES) {
				final String templatePath = templateName + ".ftl";
				namesMap.put(templatePath, templateName);
				try (final InputStream input = TemplatesService.class.getResourceAsStream(
						"/com/jaeksoft/opensearchserver/front/templates/search/" + templatePath)) {
					templatesMap.put(templatePath,
							new TemplateSource(null, null, IOUtils.toString(input, StandardCharsets.UTF_8)));
				}
			}
			TEMPLATES = Collections.unmodifiableMap(namesMap);
			DEFAULT_TEMPLATE_CONTENT = Collections.unmodifiableMap(templatesMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> getTemplateNames() {
		return TEMPLATES;
	}

	static private class TemplateSource {

		private final String templatePath;
		private final long lastModified;
		private final String defaultContent;

		TemplateSource(final String templatePath, final Date lastModified, final String defaultContent) {
			this.templatePath = templatePath;
			this.lastModified = lastModified == null ? 0 : lastModified.getTime();
			this.defaultContent = defaultContent;
		}
	}

	static byte[] compress(final String data) throws IOException {
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length())) {
			try (final GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
				gzip.write(data.getBytes());
			}
			return bos.toByteArray();
		}
	}
}
