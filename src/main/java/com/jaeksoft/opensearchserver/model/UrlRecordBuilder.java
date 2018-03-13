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

package com.jaeksoft.opensearchserver.model;

import com.qwazr.utils.HtmlUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class UrlRecordBuilder<R extends UrlRecord, T extends UrlRecordBuilder<R, T>> {

	private final Class<T> builderClass;

	final URI url;

	String urlStore;
	String host;

	String title;
	String titleEn;
	String titleDe;
	String titleFr;
	String titleIt;
	String description;
	String descriptionEn;
	String descriptionDe;
	String descriptionFr;
	String descriptionIt;
	List<String> content;
	List<String> contentEn;
	List<String> contentDe;
	List<String> contentFr;
	List<String> contentIt;
	Language lang;
	Integer backlinks;
	Integer depth;
	Set<String> links;
	Integer httpStatus;
	String httpContentType;
	String httpContentEncoding;
	Long lastModificationTime;
	UUID storeUuid;
	UUID crawlUuid;
	Long taskCreationTime;
	CrawlStatus crawlStatus;

	UrlRecordBuilder(final Class<T> builderClass, final URI url) {
		this.builderClass = builderClass;
		this.url = url;

	}

	T me() {
		return builderClass.cast(this);
	}

	public T hostAndUrlStore(String host) {
		this.host = host;
		this.urlStore = url.toString();
		return me();
	}

	/**
	 * Remove HTML tags and trim
	 *
	 * @param value
	 * @return
	 */
	private String cleanHtml(String value) {
		if (value == null)
			return null;
		value = HtmlUtils.removeTag(value.trim()).trim();
		return value.isEmpty() ? null : value;
	}

	public T title(String title, final Language language) {
		title = cleanHtml(title);
		if (title == null)
			return me();
		this.title = title;
		if (language != null) {
			switch (language) {
			case en:
				titleEn = title;
				break;
			case de:
				titleDe = title;
				break;
			case fr:
				titleFr = title;
				break;
			case it:
				titleIt = title;
				break;
			}
		}
		return me();
	}

	public T title(final String title) {
		return title(title, lang);
	}

	public T title(final Object title, final Language language) {
		return title != null ? title(title.toString(), language) : me();
	}

	public T description(String description, final Language language) {
		description = cleanHtml(description);
		if (description == null)
			return me();
		this.description = description;
		if (language != null) {
			switch (language) {
			case en:
				descriptionEn = description;
				break;
			case de:
				descriptionDe = description;
				break;
			case fr:
				descriptionFr = description;
				break;
			case it:
				descriptionIt = description;
				break;
			}
		}
		return me();
	}

	public T description(final String description) {
		return description(description, lang);
	}

	public T description(final Object description, final Language language) {
		return description != null ? description(description.toString(), language) : me();
	}

	public T description(final UrlRecordBuilder<?, ?> builder) {
		description = builder.description;
		descriptionEn = builder.descriptionEn;
		descriptionDe = builder.descriptionDe;
		descriptionFr = builder.descriptionFr;
		descriptionIt = builder.descriptionIt;
		return me();
	}

	public T contentObject(final Object content, final Language language) {
		if (content == null)
			return me();
		if (content instanceof Collection)
			return contentCollection((Collection) content, language);
		else if (content instanceof String)
			return content((String) content, language);
		return me();
	}

	public T contentCollection(final Collection<?> collection, final Language language) {
		if (collection == null)
			return me();
		collection.forEach(content -> contentObject(content, language));
		return me();
	}

	public T content(String content, final Language language) {
		content = cleanHtml(content);
		if (content == null)
			return me();
		if (content.equals(title))
			return me();
		if (this.content == null)
			this.content = new ArrayList<>();
		this.content.add(content);
		if (language != null) {
			switch (language) {
			case en:
				if (this.contentEn == null)
					this.contentEn = new ArrayList<>();
				this.contentEn.add(content);
				break;
			case de:
				if (this.contentDe == null)
					this.contentDe = new ArrayList<>();
				this.contentDe.add(content);
				break;
			case fr:
				if (this.contentFr == null)
					this.contentFr = new ArrayList<>();
				this.contentFr.add(content);
				break;
			case it:
				if (this.contentIt == null)
					this.contentIt = new ArrayList<>();
				this.contentIt.add(content);
				break;
			}
		}
		return me();
	}

	public T content(final String content) {
		return content(content, lang);
	}

	public T content(final UrlRecordBuilder<?, ?> builder) {
		content = builder.content == null ? null : new ArrayList<>(builder.content);
		contentEn = builder.contentEn == null ? null : new ArrayList<>(builder.contentEn);
		contentDe = builder.contentDe == null ? null : new ArrayList<>(builder.contentDe);
		contentFr = builder.contentFr == null ? null : new ArrayList<>(builder.contentFr);
		contentIt = builder.contentIt == null ? null : new ArrayList<>(builder.contentIt);
		return me();
	}

	public T lang(final Language language) {
		this.lang = language;
		return me();
	}

	public T lang(final UrlRecordBuilder<?, ?> builder) {
		this.lang = builder.lang;
		return me();
	}

	public T backlinks(final Integer backlinks) {
		this.backlinks = backlinks;
		return me();
	}

	public T depth(final Integer depth) {
		this.depth = depth;
		return me();
	}

	public T link(final String link) {
		if (links == null)
			links = new LinkedHashSet<>();
		links.add(link);
		return me();
	}

	public T httpStatus(final Integer httpStatus) {
		this.httpStatus = httpStatus;
		return me();
	}

	public T httpContentType(final String contentType) {
		this.httpContentType = contentType;
		return me();
	}

	public T httpContentEncoding(final String contentEncoding) {
		this.httpContentEncoding = contentEncoding;
		return me();
	}

	public T lastModificationTime(final Long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
		return me();
	}

	public T storeUuid(final UUID storeUuid) {
		this.storeUuid = storeUuid;
		return me();
	}

	public T crawlUuid(final UUID crawlUuid) {
		this.crawlUuid = crawlUuid;
		return me();
	}

	public T taskCreationTime(final Long taskCreationTime) {
		this.taskCreationTime = taskCreationTime;
		return me();
	}

	public T crawlStatus(final CrawlStatus crawlStatus) {
		this.crawlStatus = crawlStatus;
		return me();
	}

	public abstract R build();

}