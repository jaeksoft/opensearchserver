package com.jaeksoft.searchlib.crawler.process;

public class CrawlStatistics {

	private String name;
	private CrawlStatistics parent;
	private long startTime;
	private float fetchRate;
	private long fetchedCount;
	private long deletedCount;
	private long indexedCount;
	private long ignoredCount;
	private long hostCount;
	private long extractionTime;
	private long urlCount;

	public CrawlStatistics(String name, CrawlStatistics parent) {
		this.parent = parent;
		this.name = name;
		reset();
	}

	public CrawlStatistics(String name) {
		this(name, null);
	}

	public void reset() {
		synchronized (this) {
			startTime = System.currentTimeMillis();
			fetchedCount = 0;
			deletedCount = 0;
			indexedCount = 0;
			ignoredCount = 0;
			fetchRate = 0;
			hostCount = 0;
			extractionTime = 0;
			urlCount = 0;
		}
	}

	protected void incDeletedCount() {
		synchronized (this) {
			deletedCount++;
		}
	}

	protected void incFetchedCount() {
		synchronized (this) {
			fetchedCount++;
			fetchRate = (float) fetchedCount
					/ ((float) (System.currentTimeMillis() - startTime) / 60000);
			if (parent != null)
				parent.incFetchedCount();
		}
	}

	protected void incIndexedCount() {
		synchronized (this) {
			indexedCount++;
			if (parent != null)
				parent.incIndexedCount();
		}
	}

	protected void incIgnoredCount() {
		synchronized (this) {
			ignoredCount++;
			if (parent != null)
				parent.incIgnoredCount();
		}
	}

	protected void addHostCount(long hostCount) {
		synchronized (this) {
			this.hostCount += hostCount;
			if (parent != null)
				parent.addHostCount(hostCount);
		}
	}

	protected void addExtractionTime(long extractionTime) {
		synchronized (this) {
			this.extractionTime += extractionTime;
			if (parent != null)
				parent.addExtractionTime(extractionTime);
		}
	}

	public void addUrlCount(long urlCount) {
		synchronized (this) {
			this.urlCount += urlCount;
			if (parent != null)
				parent.addUrlCount(urlCount);
		}
	}

	public String getName() {
		return name;
	}

	public long getFetchedCount() {
		return fetchedCount;
	}

	public double getFetchRate() {
		return fetchRate;
	}

	public long getDeletedCount() {
		return deletedCount;
	}

	public long getIndexedCount() {
		return indexedCount;
	}

	public long getIgnoredCount() {
		return ignoredCount;
	}

	public long getHostCount() {
		return hostCount;
	}

	public float getExtractionTime() {
		return (float) extractionTime / 1000;
	}

	public long getUrlCount() {
		return urlCount;
	}
}
