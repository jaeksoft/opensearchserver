## Getting the list of analyzers

This API returns the list of analyzers in a given index.

**Requirement:** OpenSearchServer v1.5.8

### Call parameters

**URL:** ```/services/rest/index/{index_name}/analyzer/```

**Method:** ```GET```

**URL parameters:**

- **_index\_name_** (required): The name of the index.

**Header** (optional returned type):

- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The list of analyzers is returned either in JSON or in XML format.

**HTTP code:**
200

**Content (application/json):**


    {
      "successful":true,
      "details": {
        "count":"34"
      },
      "analyzers": [
        {
          "name":"AutoCompletionAnalyzer",
          "lang":"UNDEFINED"
        },
        {
          "name":"DecimalAnalyzer",
          "lang":"UNDEFINED"
        },
        {
	    ...
	    ]
    }

    

### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**
    
    An internal error occurred
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/analyzer
    

**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/analyzer"
    }).done(function (data) {
       console.log(data);
    });
    
