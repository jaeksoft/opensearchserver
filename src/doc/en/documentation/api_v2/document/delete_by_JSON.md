## Deleting document using JSON

Use this API to delete documents using a JSON body.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/document/{field_name}```

**Method:** ```DELETE```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```

**URL parameters:**
- _**index-name**_ (required): The name of the index.
- _**field-name**_ (required): The name of the field used to identify the document to delete.

**RAW data**: an array of values

    ["2","34","65"]


### Success response
The documents have been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "3 document(s) deleted by my_field"
    }
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

    
    Field not found: my_field
    

### Sample call

**Using CURL:**

    curl -XDELETE -H "Content-Type: application/json" \  
        http://localhost:8080/services/rest/index/my_index/document/my_field
        -d '["2","34","65"]'
    
