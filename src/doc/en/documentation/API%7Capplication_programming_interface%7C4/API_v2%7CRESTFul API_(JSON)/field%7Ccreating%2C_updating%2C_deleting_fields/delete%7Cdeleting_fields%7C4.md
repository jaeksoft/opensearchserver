Use this API to delete a field from the schema.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/field/{field_name}```

**Method:** ```DELETE```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**field_name**_ (required): The name of the field.

### Success response
The field has been deleted.

**HTTP code:**
200

**Content (application/json):**
    
    {
        "successful": true,
        "info": "Deleted my_field"
    }


### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500
    
    Field not found: my_field
    

### Sample call

**Using CURL:**
    
    curl -XDELETE http://localhost:8080/services/rest/index/my_index/field/my_field


**Using jQuery:**
    
    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/field/my_field"
    }).done(function (data) {
       console.log(data);
    });
