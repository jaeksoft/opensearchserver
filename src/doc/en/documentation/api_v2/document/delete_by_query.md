## Deleting document by query

Use this API to delete documents matching a given query or search template.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/document/```

**Method:** ```DELETE```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` OR ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Query parameters:**
- _**template**_ : The name of a search request.
- _**query**_ : A query pattern.

### Success response
The document has been deleted.

**HTTP code:**
200

**Content (application/json):**

```json
{
    "successful": true,
    "info": "2 document(s) deleted"
}
```
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500    

### Sample call

**Using CURL:**

```shell
curl -XDELETE \  
    http://localhost:8080/services/rest/index/my_index/document?template=my_search
```
    
