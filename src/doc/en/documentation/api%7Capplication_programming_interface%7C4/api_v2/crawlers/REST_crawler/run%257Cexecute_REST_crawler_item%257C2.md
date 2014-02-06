The REST crawler is able to index data exposed by a REST API.
Use this API to execute the REST crawler item.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/rest/{crawl_name}/run```

**Method:** ```PUT```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**crawl_name**_ (required): The name of the REST crawler item.

### Success response
The crawl has been executed.

**HTTP code:**
200

**Content (application/json):**

    {
      "successful": true,
      "info": "50 document(s) indexed"
    }
    

### Error response

The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT http://localhost:8080/services/rest/index/my_index/crawler/rest/my_crawl/run
    

You can pass variables to the REST crawler item.
The variable is replaced in "URL" or "Path to documents" parameters using curly brackets.
Ex.: {path}

    curl -XPUT -H "Content-Type: application/json" -d '{"path": "/my_path"}' \
        http://localhost:8080/services/rest/index/my_index/crawler/rest/my_crawl/run
    
**Using jQuery:**
    
    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/rest/my_crawl/run"
    }).done(function (data) {
       console.log(data);
    });
    