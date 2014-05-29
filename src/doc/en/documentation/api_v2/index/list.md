## Getting the list of indexes

This API returns a list of indexes.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The index list is returned in JSON or XML format.

**HTTP code:**
200

**Content (application/json):**

    {
      "result": {
        "@successful": "true",
        "info": "6 index(es)",
        "indexList": [
            "dbpedia_abstract",
            "nodes",
            "smb",
            "test",
            "test_oss_rb",
            "web"
        ]
      }
    }

### Error response

The index list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**

    An internal error occurred

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index"
    }).done(function (data) {
       console.log(data);
    });