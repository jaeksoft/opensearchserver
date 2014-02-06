Use this API to update or create an autocompletion item

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/autocompletion/{autocompletion_name}?field={field_name}&rows={rows}```

**Method:** ```PUT```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**autocompletion_name**_ (required): The name of the auto-completion item.

**Query parameters:**
- _**field**_ (required, one or more): The fields providing the terms populating the auto-completion index.
- _**rows**_ (optional): The number of terms returned by default by the auto-completion query.

### Success response
The auto-completion item has been created or updated.

**HTTP code:**
200

**Content (application/json):**

    {
      "successful": true,
      "info": "Autocompletion item my_expressions updated."
    }

    {
        "successful": true,
        "info": "Autocompletion item my_expressions inserted"
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions?field=titleExact&field=contentExact&rows=8
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/autocompletion/my_expressions?field=titleExact&field=contentExact&rows=8"
    }).done(function (data) {
       console.log(data);
    });
    