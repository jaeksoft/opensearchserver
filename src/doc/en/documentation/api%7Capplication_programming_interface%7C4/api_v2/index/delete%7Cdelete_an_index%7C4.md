This API deletes an existing index.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}```

**Method:** ```DELETE```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index

### Success response
The index has been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
      "result": {
      "@successful": "true",
      "info": "Index deleted: my_index"
      }
    }

### Error response

The index creation failed. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**

    Index "my_index" not found. The index directory does not exist

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index

**Using jQuery:**

    $.ajax({ 
      type: "DELETE",
      dataType: "json",
      url: "http://localhost:8080/services/rest/index/my_index"
    }).done(function (data) {
      console.log(data);
    });