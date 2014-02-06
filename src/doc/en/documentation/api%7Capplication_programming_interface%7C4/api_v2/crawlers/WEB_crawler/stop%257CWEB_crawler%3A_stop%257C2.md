The WEB crawler index web site based on wildcards patterns (exclusion and inclusion).

Use this API to stop the crawler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/run```

**Method:** ```DELETE```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

### Success response
The stop request has been received.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "STOPPING"
    }
    

### Error response

The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
    
    curl -XDELETE http://localhost:8080/services/rest/index/my_index/crawler/web/run
    

**Using CURL:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/run"
    }).done(function (data) {
       console.log(data);
    });
    