Use this API to list all synonyms lists

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/synonyms/```

**Method:** ```GET```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

### Success response
All existing lists of synonyms are returned.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "2 item(s) found",
        "items": [
            "mylist",
            "testlist"
        ]
    }
    

### Error response

The command failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/synonyms/
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/synonyms/"
    }).done(function (data) {
       console.log(data);
    });
    