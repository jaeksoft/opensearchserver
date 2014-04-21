The WEB crawler index web sites -- based on wildcards patterns for exclusion and inclusion.

Use this API to start the crawler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/run```

**Method:** ```PUT```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**once**_ (optional): Set it to _true_ to make only one crawl session.

### Success response
The crawl has been executed.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "STARTING"
    }
    

### Error response

    The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XPUT http://localhost:8080/services/rest/index/my_index/crawler/web/run
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/run"
    }).done(function (data) {
       console.log(data);
    });
    
