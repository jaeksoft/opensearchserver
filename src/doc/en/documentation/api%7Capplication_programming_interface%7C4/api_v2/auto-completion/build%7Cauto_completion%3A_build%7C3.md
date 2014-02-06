Use this API to populate the auto-completion with the terms extracted from the configured fields.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/autocompletion/{autocompletion_name}```

**Method:** ```PUT```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**autocompletion_name**_ (required): The name of the auto-completion item.

### Success response
The auto-completion item has been populated.

**HTTP code:**
200

**Content (application/json):**

    {
      "successful": true,
      "info": "376 term(s) indexed"
    }
    

### Error response

The build failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
    
    curl -XPUT http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions"
    }).done(function (data) {
       console.log(data);
    });
