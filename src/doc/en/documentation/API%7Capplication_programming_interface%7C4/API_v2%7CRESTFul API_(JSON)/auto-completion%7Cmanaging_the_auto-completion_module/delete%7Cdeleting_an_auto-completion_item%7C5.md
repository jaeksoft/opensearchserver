Use this API to remove an auto-completion item.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/autocompletion/{autocompletion_name}```

**Method:** ```DELETE```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**autocompletion_name**_ (required): The name of the auto-completion item.

### Success response
The auto-completion item has been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Autocompletion item my_expressions deleted"
    }
    

### Error response

The build failed. The reason is provided in the content.

**HTTP code:**
404, 500

    
Autocompletion item not found: my_expressions
    

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions"
    }).done(function (data) {
       console.log(data);
    });
    
