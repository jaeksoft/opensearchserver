## Make an auto-completion query

Use this API to query an auto-completion index.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/autocompletion/{autocompletion_name}}?prefix={prefix}&rows{rows}```

**Method:** ```GET```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- ***index_name*** (required): The name of the index.
- ***autocompletion\_name*** (required): The name of the auto-completion item.

**QUERY parameters:**
- _**prefix**_ (required): The characters used to find expressions in the auto-completion index.
- _**rows**_ (optional): The number of terms to return.

### Success response
The auto-completion item has been populated.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": null,
        "terms": [
            "OpenSearchServer",
            "OpenSearchServer Search",
            "OpenSearchServer Search Engine",
            "OpenSearchServer Search Engine API",
            "OpenSearchServer OpenSearchServer",
            "OpenSearchServer documentation",
            "OpenSearchServer 1.3",
         ]
    }
    

### Error response

The build failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions?prefix=open&rows=10
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions?prefix=open&rows=10"
    }).done(function (data) {
       console.log(data);
    });
    
