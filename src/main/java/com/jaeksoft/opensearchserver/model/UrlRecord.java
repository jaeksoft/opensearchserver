package com.jaeksoft.opensearchserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.InternetDomainName;
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.LinkUtils;
import org.apache.lucene.index.IndexOptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Index(schema = "", name = "")
public class UrlRecord {

	@JsonProperty(FieldDefinition.ID_FIELD)
	@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField, stored = true)
	@Copy(to = { @Copy.To(order = 0, field = "full"), @Copy.To(order = 0, field = "urlLike") })
	final public String url;

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

	@IndexField(template = FieldDefinition.Template.SortedIntDocValuesField)
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
	final public Integer httpStatus;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField)
	final public String httpContentType;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField)
	final public String httpContentEncoding;

	@IndexField(template = FieldDefinition.Template.SortedLongDocValuesField)
	final public Long leastUuid;

	@IndexField(template = FieldDefinition.Template.SortedLongDocValuesField)
	final public Long mostUuid;

	UrlRecord() {
		url = null;
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
		leastUuid = null;
		mostUuid = null;
	}

	@JsonIgnore
	public String getUrl() {
		return url;
	}

	@JsonIgnore
	public String getReducedUrl() {
		return LinkUtils.urlHostPathWrapReduce(url, 70);
	}

	UrlRecord(UrlRecordBuilder<?, ?> builder) {
		this.url = builder.url.toString();
		this.host = builder.host == null ? builder.url.getHost() : builder.host;

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

		this.httpStatus = builder.httpStatus;
		this.httpContentType = builder.httpContentType;
		this.httpContentEncoding = builder.httpContentEncoding;
		if (builder.uuid != null) {
			this.leastUuid = builder.uuid.getLeastSignificantBits();
			this.mostUuid = builder.uuid.getMostSignificantBits();
		} else {
			this.leastUuid = null;
			this.mostUuid = null;
		}
	}

	public static class Builder extends UrlRecordBuilder<UrlRecord, Builder> {

		Builder(final URI url) {
			super(Builder.class, url, url.getHost());
		}

		@Override
		public UrlRecord build() {
			return new UrlRecord(this);
		}
	}

	public Builder of(final URI url) {
		return new Builder(url);
	}
}


