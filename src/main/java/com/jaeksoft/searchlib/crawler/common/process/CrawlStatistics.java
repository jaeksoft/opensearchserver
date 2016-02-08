/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.process;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class CrawlStatistics {

	private final CrawlStatistics parent;
	private volatile Date startDate;
	private volatile long startTime;
	private volatile float fetchRate;
	private final AtomicLong fetchedCount = new AtomicLong();
	private final AtomicLong fromCacheCount = new AtomicLong();
	private final AtomicLong pendingDeleteCount = new AtomicLong();
	private final AtomicLong deletedCount = new AtomicLong();
	private final AtomicLong parsedCount = new AtomicLong();
	private final AtomicLong pendingUpdatedCount = new AtomicLong();
	private final AtomicLong updatedCount = new AtomicLong();
	private final AtomicLong pendingNewUrlCount = new AtomicLong();
	private final AtomicLong newUrlCount = new AtomicLong();
	private final AtomicLong ignoredCount = new AtomicLong();
	private final AtomicLong urlListSize = new AtomicLong();
	private final AtomicLong urlCount = new AtomicLong();
	private final AtomicLong hostListSize = new AtomicLong();
	private final AtomicLong hostCount = new AtomicLong();

	public CrawlStatistics() {
		this(null);
	}

	public CrawlStatistics(CrawlStatistics parent) {
		this.parent = parent;
		reset();
	}

	public synchronized void reset() {
		startTime = System.currentTimeMillis();
		startDate = new Date(startTime);
		hostListSize.set(0);
		fetchedCount.set(0);
		fromCacheCount.set(0);
		pendingDeleteCount.set(0);
		deletedCount.set(0);
		parsedCount.set(0);
		pendingUpdatedCount.set(0);
		updatedCount.set(0);
		pendingNewUrlCount.set(0);
		newUrlCount.set(0);
		ignoredCount.set(0);
		fetchRate = 0;
		hostCount.set(0);
		urlListSize.set(0);
		urlCount.set(0);
	}

	public synchronized void resetPending() {
		pendingDeleteCount.set(0);
		pendingUpdatedCount.set(0);
		pendingNewUrlCount.set(0);
		if (parent != null)
			parent.resetPending();
	}

	public synchronized void addDeletedCount(long value) {
		this.deletedCount.addAndGet(value);
		if (parent != null)
			parent.addDeletedCount(value);
	}

	public synchronized void incPendingUpdateCount() {
		this.pendingUpdatedCount.incrementAndGet();
		if (parent != null)
			parent.incPendingUpdateCount();
	}

	public synchronized void incPendingDeleteCount() {
		this.pendingDeleteCount.incrementAndGet();
		if (parent != null)
			parent.incPendingDeleteCount();
	}

	public synchronized void addPendingNewUrlCount(long value) {
		this.pendingNewUrlCount.addAndGet(value);
		if (parent != null)
			parent.addPendingNewUrlCount(value);
	}

	public synchronized void addNewUrlCount(long value) {
		this.newUrlCount.addAndGet(value);
		if (parent != null)
			parent.addNewUrlCount(value);
	}

	public synchronized void incFetchedCount() {
		fetchRate = (float) fetchedCount.incrementAndGet() / ((float) (System.currentTimeMillis() - startTime) / 60000);
		if (parent != null)
			parent.incFetchedCount();
	}

	public synchronized void incFromCacheCount() {
		fromCacheCount.incrementAndGet();
		if (parent != null)
			parent.incFromCacheCount();
	}

	public synchronized void incParsedCount() {
		parsedCount.incrementAndGet();
		if (parent != null)
			parent.incParsedCount();
	}

	public synchronized void addUpdatedCount(long value) {
		this.updatedCount.addAndGet(value);
		if (parent != null)
			parent.addUpdatedCount(value);
	}

	public synchronized void addListSize(long value) {
		this.urlListSize.addAndGet(value);
		if (parent != null)
			parent.addListSize(value);
	}

	public synchronized void incIgnoredCount() {
		ignoredCount.incrementAndGet();
		if (parent != null)
			parent.incIgnoredCount();
	}

	public synchronized void incUrlCount() {
		urlCount.incrementAndGet();
		if (parent != null)
			parent.incUrlCount();
	}

	public synchronized void addHostListSize(long value) {
		hostListSize.addAndGet(value);
		if (parent != null)
			parent.addHostListSize(value);
	}

	public synchronized void incHostCount() {
		hostCount.incrementAndGet();
		if (parent != null)
			parent.incHostCount();
	}

	public long getHostListSize() {
		return hostListSize.get();
	}

	public Date getStartDate() {
		return startDate;
	}

	public long getFetchedCount() {
		return fetchedCount.get();
	}

	public long getFromCacheCount() {
		return fromCacheCount.get();
	}

	public double getFetchRate() {
		return fetchRate;
	}

	public long getPendingDeletedCount() {
		return pendingDeleteCount.get();
	}

	public long getDeletedCount() {
		return deletedCount.get();
	}

	public long getParsedCount() {
		return parsedCount.get();
	}

	public long getPendingUpdatedCount() {
		return pendingUpdatedCount.get();
	}

	public long getUpdatedCount() {
		return updatedCount.get();
	}

	public long getPendingNewUrlCount() {
		return pendingNewUrlCount.get();
	}

	public long getNewUrlCount() {
		return newUrlCount.get();
	}

	public long getIgnoredCount() {
		return ignoredCount.get();
	}

	public long getHostCount() {
		return hostCount.get();
	}

	public long getUrlCount() {
		return urlCount.get();
	}

	public long getUrlListSize() {
		return urlListSize.get();
	}

}
