## Getting the list of crawl process

Use this API to retrieve the list of existing database crawl process for one index.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/database```

**Method:** ```GET```

**Header** (optional returned type):

- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**

- _**index_name**_ (required): The name of the index.

### Success response
List of existing database crawl process.

**HTTP code:**
200

**Content (application/json):**

    {  
      "successful":true,
      "info":"2 item(s) found",
      "items":[  
        "crawl_full",
        "crawl_incremental"
      ]
    }
    

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/database
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/database"
    }).done(function (data) {
       console.log(data);
    });
    
