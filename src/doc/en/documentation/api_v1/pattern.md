## Pattern API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/pattern

API Pattern is the interface to insert/update patterns into the web crawler of the OpenSearchServer search engine.
    
**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**deleteAll**_: The cmd to perform: create, stop, start
- _**type**_: The type parameter is used to select the type of the pattern. Pattern has two types:
  - exclusion
  - inclusion

If the type is not specified by default inclusion will be selected.

### Posting a text file

One easy way to manage the pattern list is to upload a text file using a post or a put http request. Typical content of the text file (open pattern per line) :

* http://www.open-search-server.com - if you only want to crawl the home page
* http://www.open-search-server.com/* -  if you want to crawl all the content
* http://www.open-search-server.com/*wiki* - if you only wish to crawl URLs containing the word "wiki" within the open-search-server.com domain.
 
```
www.open-search-server.com/*
www.open-search-server.fr/*
```

Then you have to post the file using a post or a put HTTP request.
In our example we use CURL:

    curl -o log.out -T patternlist.txt "http://localhost:9090/pattern?use=indexname&deleteAll=yes"

### Using PHP

The PHP client classes can be found in the SVN directory: http://opensearchserve.svn.sourceforge.net/viewvc/opensearchserve/trunk/src/php

```php
$oss = new OssApi('http://localhost:9090', 'index1');
```

Push a single pattern:

```php
$oss->pattern('http://www.open-search-server.com/*');
```

Multiple patterns:

```php
$oss->pattern(array(
    'http://www.open-search-server.com/*',
    'http://nkubz.net/*',
    'http://wikipedia.fr/*'
));
```

Replace the crawler patterns with the one's stored in a text file (one per line):

```php
$oss->pattern(file_get_contents('patternlist.txt'), true);
```