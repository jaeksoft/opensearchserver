Use this API to delete a search template.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/template/{template_name}```

**Method:** ```DELETE```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` OR ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the search template.

### Success response
The search template has been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Template deleted: my_search"
    }
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

    Template not found: my_search
    

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index/search/template/my_search
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/search/template/my_search"
    }).done(function (data) {
        console.log(data);
    });
    