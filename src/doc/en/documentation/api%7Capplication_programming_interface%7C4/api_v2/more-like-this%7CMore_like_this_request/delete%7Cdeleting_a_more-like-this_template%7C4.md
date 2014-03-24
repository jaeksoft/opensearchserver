Use this API to delete a MoreLikeThis template.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis/{template_name}```

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
        "info": "Template deleted: my_mlt"
    }
    

### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

    Template not found: my_mlt
    

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:9090/services/rest/index/my_index/morelikethis/my_mlt
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:9090/services/rest/index/my_index/morelikethis/my_mlt
    }).done(function (data) {
        console.log(data);
    });
    