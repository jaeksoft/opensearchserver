## Getting status of the file crawler

Use this API to get current status of the file crawler.

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/status/{index_name}/{result_type}`

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).

### Success response
The current status of the file crawler.

**HTTP code:**
200

**Content (json):**

    {
      "successful":true,
      "info":"STOPPED"
    }

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XGET -H \
         http://localhost:8080/services/rest/crawler/file/status/my_index/json
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/status/my_index/json"
    }).done(function (data) {
       console.log(data);
    });
    
