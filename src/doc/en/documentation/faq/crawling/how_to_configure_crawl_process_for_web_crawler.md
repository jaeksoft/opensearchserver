## How to configure the crawl process of the Web crawler

When opening first the tab `Crawl process` in the Web Crawler, you can feel a bit lost. This page will explain the main parameters.

### Understanding how the web crawler works

#### The crawl session

One main concept you need to properly understand is the "crawl session". When started, the crawler **runs an unlimited number of "crawl sessions"**. 

During each session it will crawl a particular number of pages, and will follow the rules you gave it. Those rules will for example **limit the number of websites it visits in parallel**, the **number of pages it accesses for one website**, etc.

When the number of URL is reached, the crawler "flushes" its "buffer": this is where documents are really indexed. A new session is automatically started afterwards.

#### The database of URLs (URL Browser)

During the crawl sessions **the crawler automatically discovers new URLs**. When reading a page the crawler will add to its "database of URL" every link found on the page. This database will grow quickly, and the crawler will "loop" through this database to find new URL to fetch. 

When fetching an URL the crawler will **update several information for this URL** in this database: current time, HTTP answer code (200, 404, ...), etc.

This database can be searched with the tab "URL Browser".

### Parameters

Global parameters:

* **`User-Agent`**: [UserAgent](http://en.wikipedia.org/wiki/User_agent) that the crawler will use when fetching the URLs. 
* **`Fetch interval between re-fetches`**: delay that OpenSearchServer will wait before fetching again URL that have already been fetched.
* **`Delay between each successive access, in seconds`**: for one website, time to wait between each crawl _(on SaaS server this value has a minimum of `1`)_. 
* **`Indexation buffer`**: number of documents to keep in buffer before indexing. If this number is lower than _Number of URLs to crawl_ then documents can  be indexed several times during one crawl session. If lots of index are crawling on one OpenSearchServer instance you may want to use a small value for this parameter (like 5 or 10) to avoid "Out of memory" issues.

Following parameters are all related to one crawl session:

* **`Number of URLs to crawl`**: maximum global number of URLs to crawl during one crawl session _(on SaaS servers this value can not exceed 10000)_. Once this number of URL has been fetched the crawl session ends, documents are indexed, and optionnaly a job of scheduler is run. Then a new session starts if crawler runs in "forever" mode.
* **`Maximum number of URLs per host`**: for one host (one website), maximum number of URLs to crawl during one crawl session. 
* **`Number of simultaneous threads`**: during one crawl session, maximum number of websites that can be crawled in parallel.

### Tuning settings 

Choosing right values for these settings depends on several inputs:

* **memory allocated** to OpenSearchServer: settings will not be the same with 1GB or with 30GB.
* **refresh frequency** wanted
* **number of websites** to crawl and **number of pages** by website

You need to configure the settings in a way that they "work" for these inputs. For example you will probably not be able to crawl each day 100000 page with one crawler on one server only.

On the other hand, if you have few URLs to crawl, or if you do not want a high re-crawl frequency, you will have to spread the crawling so **that it does not use lots of memory**. For example, if you wish to crawl 4 websites, each with 10000 URLs, each month, you can use these setting:

* Number of simultaneous threads: `4`
* Delay between each successive access, in seconds: `240`

The crawler will crawl the 4 websites simultaneously. By waiting 240 seconds between each access, for each website, it will take 240 * 10000 = 2400000 seconds to complete crawling of the 10000 URLs. This is equal to 28 days. Use of memory is minimized here.

Of course it is often hard to know the exact number of URL that the crawler will have to crawl. You may need to adjust these settings from time to time.


