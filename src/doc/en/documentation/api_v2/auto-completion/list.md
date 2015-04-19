## Listing all auto-completion items

Use this API to list the auto-completion items.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/autocompletion/```

**Method:** ```GET```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

### Success response
The list of auto-completion items.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "3 item(s) found",
        "items": [
            "my_products",
            "my_categories",
        ]
    }
    

### Error response

The build failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/autocompletion/
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/autocompletion/"
    }).done(function (data) {
       console.log(data);
    });
    