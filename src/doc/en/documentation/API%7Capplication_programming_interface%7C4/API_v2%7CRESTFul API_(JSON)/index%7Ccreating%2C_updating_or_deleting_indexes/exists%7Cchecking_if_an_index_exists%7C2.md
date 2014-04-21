This API tests whether a given index exists.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

### Success response
The index has been found.

**HTTP code:**
200

**Content (application/json):**

    {
      "result": {
      "@successful": "true",
      "info": true
      }
    }

### Error response

The index has not been found.

**HTTP code:**
404

**Content (text/plain):**

    The index my_index has not been found

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index

**Using jQuery:**

    $.ajax({ 
     type: "GET",
     dataType: "json",
     url: "http://localhost:8080/services/rest/index/my_index"
    }).done(function (data) {
     console.log(data);
    });
