## Deleting document by field content

Use this API to delete documents that contain the values listed in a specific field. These values are passed as parameters.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/document/{field_name}/{value1}/{value2}/...```

**Method:** ```DELETE```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` OR ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**field_name**_ (required): The name of the field used to identify the documents to delete.
- _**value**_ (required): A list of values.

### Success response
The document has been deleted.

**HTTP code:**
200

**Content (application/json):**
    json
{
    "successful": true,
    "info": "2 document(s) deleted by my_field"
}
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

    Field not found: my_field
    

### Sample call

**Using CURL:**
    shell
curl -XDELETE \  
    http://localhost:8080/services/rest/index/my_index/document/my_field/3/4
    
