## Starting the file crawler

Use this API to launch the file crawler and leave it running endlessly.

**Requirement:** OpenSearchServer v1.5

**The URLs and HTTP methods used by this API are different from other OSS APIs, please give this document a careful read.**

### Call parameters

**URL:** `/services/rest/crawler/file/run/forever/{index_name}/{result_type}`

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).

### Success response
The crawler is starting.

**HTTP code:**
200

**Content (json):**

    {
      "successful":true,
      "info":"STARTING"
    }

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XGET -H \
         http://localhost:8080/services/rest/crawler/file/run/forever/my_index/json
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/run/forever/my_index/json"
    }).done(function (data) {
       console.log(data);
    });
    
