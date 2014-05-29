## Crawling one URL

Use this API to crawl a page by passing its URL.

The URL must match the pattern list. 

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/crawl?url={url}```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index
- _**url**_ (required): The URL to crawl

### Success response
The page has been crawled.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Result: Fetched - Parsed - Indexed"
    }


### Error response

The index has not been found.

**HTTP code:**
404

**Content (text/plain):**

    The index my_index has not been found


### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/web/crawl?url=http://www.example.org/


**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/crawl?url=http://www.example.org/"
    }).done(function (data) {
       console.log(data);
    });
