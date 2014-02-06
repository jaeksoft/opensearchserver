The WEB crawler index web site based on wildcards patterns (exclusion and inclusion).

Use this API to obtain the status of crawler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/run```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

### Success response
The status has been returned.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "STOPPED"
    }
    

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/web/run
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/run"
    }).done(function (data) {
       console.log(data);
    });
    