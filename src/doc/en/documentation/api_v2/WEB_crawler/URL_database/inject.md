## Injecting URL to crawl

Use this API to insert URLs in the URL database.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/urls```

**Method:** ```PUT```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Raw data (PUT):**
An array of patterns.

    [
      "http://www.open-search-server.com/",
      "https://github.com/jaeksoft/opensearchserver/",
      "https://sourceforge.net/projects/opensearchserve/"
    ]
    

### Success response
The URLs has been inserted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Injected: 1 - Already: 2"
    }
    

### Error response

The insertion failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '["http://www.open-search-server.com/"]' \
        http://localhost:8080/services/rest/index/my_index/crawler/web/urls
    
