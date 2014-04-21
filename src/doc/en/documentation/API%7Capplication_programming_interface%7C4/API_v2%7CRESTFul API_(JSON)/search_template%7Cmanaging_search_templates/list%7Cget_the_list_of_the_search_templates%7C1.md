This API returns the templates. A templates is a query stored with its parameters. It can be easily recalled using its name.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/template```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The field list is returned either in JSON or in XML format.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "2 template(s)",
        "templates": [
            {
                "name": "my_search",
                "type": "Search (pattern)"
            },
            {
                "name": "my_search2",
                "type": "Search (field)"
            }
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

    curl -XGET http://localhost:8080/services/rest/index/my_index/search/template
    

**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/search/template"
    }).done(function (data) {
       console.log(data);
    });
    
