## How to configure the crawl process of the Web crawler

When opening the `Crawl process` tab in the Web Crawler for the first time, you can feel a bit lost. This page explains the main parameters.

### Understanding how the web crawler works

#### The crawl session

The main concept you need to grok is the "crawl session". Once started, the crawler **runs an unlimited number of "crawl sessions"**. 

During each session it will crawl a set number of pages and follow the rules you provided. Examples of such rules include **limiting the number of websites visited in parallel**, limiting the **number of pages accessed for one website**, etc.

When this set number of URLs is reached, the crawler flushes its buffer: the documents it crawled can now be properly indexed. A new crawling session then automatically starts.

#### The database of URLs (URL Browser)

During the crawl sessions **the crawler automatically discovers new URLs**. When reading a page the crawler will add to its "database of URLs" every link found on the page. This database will grow quickly, and the crawler will loop through this database to find new URLs to fetch. 

When fetching an URL the crawler will **update its information for this URL** in this database: current time, HTTP answer code (200, 404, ...), etc.

This database can be searched using the "URL Browser" tab.

### Parameters

Global parameters:

* **`User-Agent`**: The [UserAgent](http://en.wikipedia.org/wiki/User_agent) that the crawler will use when fetching the URLs. 
* **`Fetch interval between re-fetches`**: the delay that OpenSearchServer will let pass before re-fetching an URL that has already been fetched.
* **`Delay between each successive access, in seconds`**: for a given website, how long OSS will wait between each crawl _(on OSS SaaS servers this value has a minimum of `1`)_. 
* **`Indexation buffer`**: the number of documents to keep in the buffer before indexing them. If this number is lower than the _Number of URLs to crawl_ then documents can be indexed several times during a single crawl session. If numerous indexes are crawling on a single OpenSearchServer instance you may want to set this parameter to a small value (say, 5 or 10) to avoid "Out of memory" issues.

The following parameters all apply to one crawl session:

* **`Number of URLs to crawl`**: the maximum global number of URLs to crawl during one crawl session _(on OSS SaaS servers this value can not exceed 10000)_. Once this number of URLs has been fetched the crawl session ends, documents are indexed, and optionnaly a scheduler job is run. Then a new session starts, assuming that the crawler is running in "forever" mode.
* **`Maximum number of URLs per host`**: for one host (one website), the maximum number of URLs to crawl during one crawl session. 
* **`Number of simultaneous threads`**: during one crawl session, the maximum number of websites that can be crawled in parallel.

### Tuning the settings 

Choosing proper values for these settings depends on context:

* the **memory allocated** to OpenSearchServer: the settings will not be the same with 1GB or with 30GB.
* the desired **refresh frequency**
* the **number of websites** to crawl and the **number of pages** by website

You need to configure the settings in a way that works in context. For example you will probably not be able to crawl 100,000 pages a day with but a single crawler on a single server.

On the other hand, if you have but a few URLs to crawl, or do not need a high re-crawl frequency, you will have to spread the crawling to **save memory**. For example, if you wish to crawl 4 websites, each with 10,000 URLs, every month, you can use these setting:

* Number of simultaneous threads: `4`
* Delay between each successive access, in seconds: `240`

The crawler will crawl the 4 websites simultaneously. By waiting 240 seconds between each access, for each website, it will take 240 * 10,000 = 2,400,000 seconds to complete crawling of the 10,000 URLs. This is equal to 28 days. Your use of memory is thus minimized.

Of course it is often difficult to know the exact number of URL that the crawler will have to crawl. You may need to adjust these settings from time to time.


