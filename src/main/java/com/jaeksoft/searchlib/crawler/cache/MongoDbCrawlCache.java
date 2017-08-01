/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.cache;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDbCrawlCache extends CrawlCacheProvider {

	private final static String DEFAULT_DATABASE = "oss_crawl_cache";
	private final static String META_COLLECTION = "meta";
	private final static String INDEXED_COLLECTION = "indexed";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private MongoClient mongoClient = null;
	private MongoCollection<Document> metaCollection = null;
	private MongoCollection<Document> indexedCollection = null;
	private GridFSBucket contentGrid = null;

	public MongoDbCrawlCache() {
		super(CrawlCacheProviderEnum.LOCAL_FILE);
	}

	private void closeNoLock() {
		if (mongoClient != null) {
			mongoClient.close();
			mongoClient = null;
			metaCollection = null;
			indexedCollection = null;
			contentGrid = null;
		}
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			closeNoLock();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public String getInfos() throws IOException {
		rwl.r.lock();
		try {
			return mongoClient == null ? null : mongoClient.getConnectPoint();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void init(String configString) throws IOException {
		rwl.w.lock();
		try {
			closeNoLock();
			final MongoClientURI connectionString = new MongoClientURI(configString);
			mongoClient = new MongoClient(connectionString);
			final MongoDatabase database = mongoClient.getDatabase(
					connectionString.getDatabase() == null ? DEFAULT_DATABASE : connectionString.getDatabase());
			metaCollection = database.getCollection(META_COLLECTION);
			metaCollection.createIndex(Document.parse("{\"uri\":1}"));
			indexedCollection = database.getCollection(INDEXED_COLLECTION);
			indexedCollection.createIndex(Document.parse("{\"uri\":1}"));
			contentGrid = GridFSBuckets.create(database);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public long flush(long expiration) throws IOException {
		rwl.r.lock();
		try {
			final Bson filter = expiration == 0 ? Filters.exists("uri") : Filters.lt("_id",
					new ObjectId(new Date(expiration)));
			indexedCollection.deleteMany(filter);
			for (GridFSFile f : contentGrid.find(filter))
				contentGrid.delete(f.getObjectId());
			long l = metaCollection.deleteMany(filter).getDeletedCount();
			return l;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String getConfigurationInformation() {
		return "Please provide the connection URL to the MongoDB instance (Eg.: mongodb://localhost:27017/oss_crawl_cache)";
	}

	@Override
	public Item getItem(URI uri, long expirationTime) throws UnsupportedEncodingException {
		return new MongoDbItem(uri, expirationTime);
	}

	private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

	public class MongoDbItem extends Item {

		private final URI uri;
		private final String uriString;
		private final int expirationTime;

		private MongoDbItem(final URI uri, long expirationTime) throws UnsupportedEncodingException {
			this.uri = uri;
			this.uriString = uri.toString();
			this.expirationTime = (int) (expirationTime / 1000);
		}

		@Override
		public InputStream store(DownloadItem downloadItem) throws IOException, JSONException {
			rwl.r.lock();
			try {
				final URI uri = downloadItem.getUri();
				if (!uri.equals(this.uri))
					throw new IOException("The URI does not match: " + uri + " / " + this.uri);

				final Document newDocument = Document.parse(downloadItem.getMetaAsJson());
				newDocument.put("uri", uriString);

				final BsonValue id = metaCollection.replaceOne(eq("uri", uriString), newDocument, UPSERT)
						.getUpsertedId();

				final GridFSUploadOptions options = new GridFSUploadOptions().metadata(new Document("_id", id));

				contentGrid.uploadFromStream(id, uriString, downloadItem.getContentInputStream(), options);

				return contentGrid.openDownloadStream(id);
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public DownloadItem load() throws IOException, JSONException, URISyntaxException {
			rwl.r.lock();
			try {
				final Document foundDocument = metaCollection.find(eq("uri", uri.toString())).first();
				if (foundDocument == null)
					return null;
				final ObjectId objectId = foundDocument.getObjectId("_id");
				if (expirationTime != 0)
					if (objectId.getTimestamp() < expirationTime)
						return null;
				final DownloadItem downloadItem = new DownloadItem(uri, foundDocument);
				downloadItem.setContentInputStream(contentGrid.openDownloadStream(objectId));
				return downloadItem;
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public boolean flush() throws IOException {
			rwl.r.lock();
			try {
				boolean deleted = false;
				final Bson filter = eq("uri", uriString);
				final Document document = metaCollection.find(filter).first();
				if (document == null)
					return false;
				contentGrid.delete(document.getObjectId("_id"));
				if (indexedCollection.deleteOne(eq("uri", uriString)).getDeletedCount() > 0)
					deleted = true;
				if (metaCollection.deleteOne(eq("uri", uriString)).getDeletedCount() > 0)
					deleted = true;
				return deleted;
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public void store(List<ParserResultItem> parserResults) throws IOException {
			rwl.r.lock();
			try {
				if (parserResults == null || parserResults.isEmpty())
					return;
				final BsonArray documents = new BsonArray();
				parserResults.forEach(parserResultItem -> {
					final BsonDocument document = new BsonDocument();
					final IndexDocument indexDocument = parserResultItem.getParserDocument();
					indexDocument.forEachFieldValueItem((fieldName, fieldValueItems) -> {
						final LinkedHashSet<String> fieldValues = new LinkedHashSet<>();
						fieldValueItems.forEach(fvi -> fieldValues.add(fvi.value));
						final BsonValue values;
						switch (fieldValues.size()) {
						case 0:
							values = null;
							break;
						case 1:
							values = new BsonString(fieldValues.iterator().next());
							break;
						default:
							final BsonArray array = new BsonArray();
							fieldValues.forEach(value -> array.add(new BsonString(value)));
							values = array;
							break;
						}
						if (values != null)
							document.append(fieldName, values);
					});
					documents.add(document);
				});
				indexedCollection.replaceOne(eq("uri", uriString),
						new Document("uri", uriString).append("documents", documents), UPSERT);
			} finally {
				rwl.r.unlock();
			}
		}
	}
}
