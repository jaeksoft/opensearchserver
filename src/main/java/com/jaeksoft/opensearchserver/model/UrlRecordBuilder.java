package com.jaeksoft.opensearchserver.model;

import com.qwazr.utils.StringUtils;

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
	final String host;

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
	UUID uuid;

	UrlRecordBuilder(final Class<T> builderClass, final URI url, final String host) {
		this.builderClass = builderClass;
		this.url = url;
		this.host = host;
	}

	T me() {
		return builderClass.cast(this);
	}

	public T title(String title, final Language language) {
		if (StringUtils.isBlank(title))
			return me();
		title = title.trim();
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
		if (StringUtils.isBlank(description))
			return me();
		description = description.trim();
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
		if (StringUtils.isBlank(content))
			return me();
		content = content.trim();
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

	public T uuid(final UUID uuid) {
		this.uuid = uuid;
		return me();
	}

	public abstract R build();

}