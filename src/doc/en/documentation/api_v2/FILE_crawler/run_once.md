## Running the file crawler once

Use this API to run a single crawling session with the file crawler.

**Requirement:** OpenSearchServer v1.5

**The URLs and HTTP methods used by this API are different from other OSS APIs, please give this document a careful read.**

### Call parameters

**URL:** `/services/rest/crawler/file/run/once/{index_name}/{result_type}`

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): The type of returned result (`json` or `xml`).

### Success response
The crawl session has started.

**HTTP code:**
200

**Content (json):**

    {
      "successful":true,
      "info":"STARTING"
    }

### Error response

The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XGET -H \
         http://localhost:8080/services/rest/crawler/file/run/once/my_index/json
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/run/once/my_index/json"
    }).done(function (data) {
       console.log(data);
    });
    
