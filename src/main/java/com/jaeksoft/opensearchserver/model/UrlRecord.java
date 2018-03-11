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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.net.InternetDomainName;
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.index.IndexOptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Index(schema = "", name = "")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class UrlRecord {

	@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField)
	final public String url;

	@IndexField(template = FieldDefinition.Template.StoredField)
	@Copy(to = { @Copy.To(order = 0, field = "full"), @Copy.To(order = 0, field = "urlLike") })
	final public String urlStore;

	@IndexField(template = FieldDefinition.Template.StringField)
	final public String lang;

	@IndexField(template = FieldDefinition.Template.SortedDocValuesField)
	final public String host;

	@IndexField(template = FieldDefinition.Template.StringField)
	final public List<String> subHosts;

	@IndexField(template = FieldDefinition.Template.SortedDocValuesField)
	final public String registrySuffix;

	@IndexField(template = FieldDefinition.Template.SortedIntDocValuesField)
	final public Integer backlinks;

	@IndexField(template = FieldDefinition.Template.IntDocValuesField)
	final public Integer depth;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
	final public String urlLike = null;

	@IndexField(template = FieldDefinition.Template.TextField,
			stored = true,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
	@Copy(to = { @Copy.To(order = 1, field = "full") })
	final public String title;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
	@Copy(to = { @Copy.To(order = 1, field = "fullEn") })
	final public String titleEn;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.GermanIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.GermanQuery.class)
	@Copy(to = { @Copy.To(order = 1, field = "fullDe") })
	final public String titleDe;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.ItalianIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.ItalianQuery.class)
	@Copy(to = { @Copy.To(order = 1, field = "fullIt") })
	final public String titleIt;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.FrenchIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.FrenchQuery.class)
	@Copy(to = { @Copy.To(order = 1, field = "fullFr") })
	final public String titleFr;

	@IndexField(template = FieldDefinition.Template.TextField,
			stored = true,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
	@Copy(to = { @Copy.To(order = 2, field = "full") })
	final public String description;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
	@Copy(to = { @Copy.To(order = 2, field = "fullEn") })
	final public String descriptionEn;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.GermanIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.GermanQuery.class)
	@Copy(to = { @Copy.To(order = 2, field = "fullDe") })
	final public String descriptionDe;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.ItalianIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.ItalianQuery.class)
	@Copy(to = { @Copy.To(order = 2, field = "fullIt") })
	final public String descriptionIt;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.FrenchIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.FrenchQuery.class)
	@Copy(to = { @Copy.To(order = 2, field = "fullFr") })
	final public String descriptionFr;

	@IndexField(template = FieldDefinition.Template.TextField,
			stored = true,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
	@Copy(to = { @Copy.To(order = 3, field = "full") })
	final public List<String> content;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
	@Copy(to = { @Copy.To(order = 3, field = "fullEn") })
	final public List<String> contentEn;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.GermanIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.GermanQuery.class)
	@Copy(to = { @Copy.To(order = 3, field = "fullDe") })
	final public List<String> contentDe;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.ItalianIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.ItalianQuery.class)
	@Copy(to = { @Copy.To(order = 3, field = "fullIt") })
	final public List<String> contentIt;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.FrenchIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.FrenchQuery.class)
	@Copy(to = { @Copy.To(order = 3, field = "fullFr") })
	final public List<String> contentFr;

	@IndexField(template = FieldDefinition.Template.TextField,
			indexOptions = IndexOptions.DOCS,
			omitNorms = true,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
	final public List<String> full = null;

	@IndexField(template = FieldDefinition.Template.TextField,
			indexOptions = IndexOptions.DOCS,
			omitNorms = true,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
	final public List<String> fullEn = null;

	@IndexField(template = FieldDefinition.Template.TextField,
			indexOptions = IndexOptions.DOCS,
			omitNorms = true,
			analyzerClass = SmartAnalyzerSet.GermanIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.GermanQuery.class)
	final public List<String> fullDe = null;

	@IndexField(template = FieldDefinition.Template.TextField,
			indexOptions = IndexOptions.DOCS,
			omitNorms = true,
			analyzerClass = SmartAnalyzerSet.ItalianIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.ItalianQuery.class)
	final public List<String> fullIt = null;

	@IndexField(template = FieldDefinition.Template.TextField,
			indexOptions = IndexOptions.DOCS,
			omitNorms = true,
			analyzerClass = SmartAnalyzerSet.FrenchIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.FrenchQuery.class)
	final public List<String> fullFr = null;

	@IndexField(template = FieldDefinition.Template.StringField)
	final public Set<String> links;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField)
	final public String httpStatus;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField)
	final public String httpContentType;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField)
	final public String httpContentEncoding;

	@IndexField(template = FieldDefinition.Template.SortedLongDocValuesField)
	final public Long lastModificationTime;

	@IndexField(template = FieldDefinition.Template.SortedDocValuesField)
	final public String storeUuid;

	@IndexField(template = FieldDefinition.Template.LongDocValuesField)
	final public Long crawlUuidMost;

	@IndexField(template = FieldDefinition.Template.LongDocValuesField)
	final public Long crawlUuidLeast;

	@IndexField(template = FieldDefinition.Template.LongDocValuesField)
	final public Long taskCreationTime;

	@IndexField(template = FieldDefinition.Template.IntDocValuesField)
	final public Integer crawlStatus;

	public UrlRecord() {
		url = null;
		urlStore = null;
		host = null;
		subHosts = null;
		registrySuffix = null;
		title = null;
		titleFr = null;
		titleDe = null;
		titleEn = null;
		titleIt = null;
		description = null;
		descriptionFr = null;
		descriptionDe = null;
		descriptionEn = null;
		descriptionIt = null;
		content = null;
		contentFr = null;
		contentDe = null;
		contentIt = null;
		contentEn = null;
		lang = null;
		backlinks = null;
		depth = null;
		links = null;
		httpStatus = null;
		httpContentType = null;
		httpContentEncoding = null;
		lastModificationTime = null;
		storeUuid = null;
		crawlUuidMost = null;
		crawlUuidLeast = null;
		taskCreationTime = null;
		crawlStatus = null;
	}

	public String getUrl() {
		return urlStore;
	}

	public Long getLastModificationTime() {
		return lastModificationTime;
	}

	public String getReducedUrl() {
		return LinkUtils.urlHostPathWrapReduce(urlStore, 70);
	}

	public UUID getStoreUuid() {
		return StringUtils.isBlank(storeUuid) ? null : HashUtils.fromBase64(storeUuid);
	}

	public UUID getCrawlUuid() {
		return crawlUuidMost == null || crawlUuidLeast == null ? null : new UUID(crawlUuidMost, crawlUuidLeast);
	}

	public Long getTaskCreationTime() {
		return taskCreationTime;
	}

	UrlRecord(UrlRecordBuilder<?, ?> builder) {
		this.url = builder.url.toString();
		this.urlStore = builder.urlStore;
		this.host = builder.host;

		if (this.host != null) {
			final InternetDomainName registrySuffix = InternetDomainName.from(this.host).registrySuffix();
			this.registrySuffix = registrySuffix == null ? null : registrySuffix.toString();
			this.subHosts = new ArrayList<>();
			int i = this.host.indexOf('.');
			String subHost = this.host;
			this.subHosts.add(subHost);
			while (i != -1) {
				subHost = subHost.substring(i + 1);
				this.subHosts.add(subHost);
				i = subHost.indexOf('.');
			}
		} else {
			this.registrySuffix = null;
			this.subHosts = null;
		}

		this.title = builder.title;
		this.titleEn = builder.titleEn;
		this.titleDe = builder.titleDe;
		this.titleFr = builder.titleFr;
		this.titleIt = builder.titleIt;

		this.description = builder.description;
		this.descriptionEn = builder.descriptionEn;
		this.descriptionDe = builder.descriptionDe;
		this.descriptionFr = builder.descriptionFr;
		this.descriptionIt = builder.descriptionIt;

		this.content = builder.content;
		this.contentEn = builder.contentEn;
		this.contentDe = builder.contentDe;
		this.contentFr = builder.contentFr;
		this.contentIt = builder.contentIt;

		this.lang = builder.lang == null ? null : builder.lang.name();
		this.backlinks = builder.backlinks;
		this.depth = builder.depth;
		this.links = builder.links;

		this.httpStatus = builder.httpStatus == null ? null : builder.httpStatus.toString();
		this.httpContentType = builder.httpContentType;
		this.httpContentEncoding = builder.httpContentEncoding;

		this.lastModificationTime = builder.lastModificationTime;

		this.storeUuid = builder.storeUuid == null ? null : HashUtils.toBase64(builder.storeUuid);
		if (builder.crawlUuid == null) {
			this.crawlUuidMost = null;
			this.crawlUuidLeast = null;
		} else {
			this.crawlUuidMost = builder.crawlUuid.getMostSignificantBits();
			this.crawlUuidLeast = builder.crawlUuid.getLeastSignificantBits();
		}
		this.taskCreationTime = builder.taskCreationTime;
		this.crawlStatus = builder.crawlStatus == null ? CrawlStatus.UNKNOWN.code : builder.crawlStatus.code;
	}

	public static class Builder extends UrlRecordBuilder<UrlRecord, Builder> {

		Builder(final URI url) {
			super(Builder.class, url.normalize());
		}

		@Override
		public UrlRecord build() {
			return new UrlRecord(this);
		}
	}

	public static Builder of(final URI url) {
		return new Builder(url);
	}
}


