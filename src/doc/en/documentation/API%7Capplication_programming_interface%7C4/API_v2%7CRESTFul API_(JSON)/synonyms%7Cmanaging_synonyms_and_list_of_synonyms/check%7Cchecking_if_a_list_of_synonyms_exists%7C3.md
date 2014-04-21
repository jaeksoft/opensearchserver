Use this API to check whether a list of synonyms exists.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/synonyms/{list_name}```

**Method:** ```HEAD```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**list_name**_ (required): The name of the list to check.

### Success response
Returns HTTP code 200 if list exist, 404 otherwise.

### Error response

The command failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XHEAD http://localhost:8080/services/rest/index/my_index/synonyms/my_list
    

**Using jQuery:**

    $.ajax({ 
       type: "HEAD",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/synonyms/my_list"
    }).done(function (data) {
       console.log(data);
    });
    
