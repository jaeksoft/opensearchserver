## How to use URL filter feature

In OpenSearchServer's web crawler, tabs `Pattern list` and `Exclusion list` are quite easy to understand. However tab `URL filter` can be a little bit more tricky. Here are some hints to understand this feature better.

Let's take this set of URL filter rules as example:

![URL filter](urlfilter.png)

One should keep in mind that:

* Exclusion rules must be written as [regular expression](http://www.regular-expressions.info/). If no `hostname` is provided then rule will be applied for every hostname.
* Regular expression tests **are applied to every parameter found in the query string, one by one**. OpenSearchServer found query string after character `?` or character `;` (which is often used for parameter `jsessionid`).
  * Regular expression are thus **not applied to the whole URL**. Parameters are first extracted from URL and then tested against each regular expression.
  * For instance with the above example, text `test1=house` would be removed from URL `http://something.com/stuff?test1=house&userid=45` but not from URL `http://something.com/stuff/test1=house/` since in this last case `test1` is not a parameter from the query string.
* It is very important to understand that URLs are tested against those URL filter rules **when they are discovered in pages by web crawler**.
  * Thus parameters that match those regular expression are deleted from URL before insertion of URL in `URL Browser`.
  * This way, crawler will always crawl cleaned URL in next sessions.
  * **If URL with some forbidden parameters are directly inserted** in `URL Browser` (_via_ tab `URL pattern` for instance) then those parameters **will not be deleted before crawling**. This behaviour will be the same **when crawling an URL using `Manual crawl` tab**: forbidden parameters are not filtered out at this time.
  
### When to use URL Filter

URL filter is a great way to delete some useless parameters from URLs when they are causing duplications. A common case is when a website does not define any `canonical URL` (that would help to avoid duplications) and embed a `jsessionid` parameter in every URL. In this case the web crawler may encounters several different links to the same page, only differing by this `jessionid` parameter. Filtering it out would allow clean management of URL database and will considerably speed up crawling process.