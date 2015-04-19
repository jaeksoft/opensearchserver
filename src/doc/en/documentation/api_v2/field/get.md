## Get the parameters of a field

This API returns the description of one field.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/field/{field_name}```

**Method:** ```GET```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**field_name**_ (required): The name of the field.

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The field is returned either in JSON or XML format.

**HTTP code:**
200

**Content (application/json):**
    
    {
        "successful": true,
        "info": "Field autocomplete",
        "field": {
            "name": "autocomplete",
            "analyzer": "AutoCompletionAnalyzer",
            "indexed": "YES",
            "stored": "NO",
            "termVector": "NO",
            "copyOf": [
                "content",
                "title"
            ]
        }
    }


### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)

**Content (text/plain):**
    
    Field not found: my_field
    

### Sample call

**Using CURL:**
    
    curl -XGET http://localhost:8080/services/rest/index/my_index/field/my_field


**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/field/my_field"
    }).done(function (data) {
       console.log(data);
    });
