Use this API to delete a list of synonyms.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/synonyms/{list_name}```

**Method:** ```DELETE```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**list_name**_ (required): The name of the list to delete.

### Success response
The list of synonyms has been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "The item testlist has been deleted"
    }
    

### Error response

The command failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index/synonyms/my_list
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/synonyms/my_list"
    }).done(function (data) {
       console.log(data);
    });
    
