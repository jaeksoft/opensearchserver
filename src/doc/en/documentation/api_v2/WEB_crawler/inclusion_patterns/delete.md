## Deleting inclusion patterns

Use this API to delete wildcards patterns from the inclusion list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/patterns/inclusion```

**Method:** ```DELETE```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**RAW data**: an array of patterns

    [
        "http://www.open-search-server.com/*",
        "https://github.com/jaeksoft/opensearchserver/*"
    ]
    

### Success response
The patterns have been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "2 patterns deleted"
    }
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

### Sample call

**Using CURL:**

    curl -XDELETE -H "Content-Type: application/json" \  
        http://localhost:8080/services/rest/index/my_index/crawler/web/patterns/inclusion
        -d '["http://www.open-search-server.com/*"]'
    