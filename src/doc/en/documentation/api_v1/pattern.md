## Pattern API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/pattern

The Pattern API is the interface to insert/update patterns in the OpenSearchServer web crawler.
    
**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**deleteAll**_: The command to perform: create, stop, start.
- _**type**_: The type parameter is used to select the type of the pattern. There are two types:
  - exclusion
  - inclusion

If no type is specified, it is assumed to be an inclusion.

### Posting a text file

One easy way to manage the pattern list is to upload a text file using a post or a put http request. Here is a typical example (one pattern per line) :

* http://www.open-search-server.com - if you only want to crawl the home page
* http://www.open-search-server.com/* -  if you want to crawl all the content
* http://www.open-search-server.com/*wiki* - if you only wish to crawl URLs containing the word "wiki" within the open-search-server.com domain.
 
```
www.open-search-server.com/*
www.open-search-server.fr/*
```

The next step is posting the file using a post or a put HTTP request.
In our example we'll use CURL:

    curl -o log.out -T patternlist.txt "http://localhost:9090/pattern?use=indexname&deleteAll=yes"

### Using PHP

The PHP client classes can be found in the SVN directory: http://opensearchserve.svn.sourceforge.net/viewvc/opensearchserve/trunk/src/php

```php
$oss = new OssApi('http://localhost:9090', 'index1');
```

Pushing a single pattern:

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

Replacing the crawler patterns with the ones stored in a text file (one per line):

```php
$oss->pattern(file_get_contents('patternlist.txt'), true);
```
