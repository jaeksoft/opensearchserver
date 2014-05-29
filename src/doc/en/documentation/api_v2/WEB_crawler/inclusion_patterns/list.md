## Listing inclusion patterns

This API returns the inclusion pattern list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/patterns/inclusion```

**Method:** ```GET```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**starts_with**_ (optional): A filter to only get those patterns starting with these characters.

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The pattern list is returned either in JSON or XML format.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "3 item(s) found",
        "items": [
            "https://github.com/jaeksoft/opensearchserver/*",
            "https://sourceforge.net/projects/opensearchserve/*",
            "http://www.open-search-server.com/*"
        ]
    }
    

### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**
    
    An internal error occurred
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/web/patterns/inclusion?starts_with=http://www
    

**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/patterns/inclusion   console.log(data);
    });
    
