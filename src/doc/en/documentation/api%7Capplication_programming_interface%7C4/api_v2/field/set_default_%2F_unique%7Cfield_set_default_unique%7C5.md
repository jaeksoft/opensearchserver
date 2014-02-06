Use this API to set the default field and the unique field in the schema

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/field?default={default_field}&unique={unique_field}```

**Method:** ```POST```

**Header** (optional returned type):
- Accept: ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Query parameters:**
- _**default**_ (optional): The name of the default field. To reset the field, just pass an empty string.
- _**unique**_ (optional): The name of the default field. To reset the field, pass an empty string.

### Success response
The default and/or unique field has been updated.

**HTTP code:**
200

**Content (application/json):**

    {
      "successful": true,
      "info": "Default field set to 'content'. Unique field set to 'url'."
    }


### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
404
    
    Field not found: my_field
    

### Sample call

**Using CURL:**

    curl -XPOST http://localhost:8080/services/rest/index/my_index/field?default=content&unique=url


**Using jQuery:**
   
    $.ajax({ 
      type: "POST",
      dataType: "json",
      url: "http://localhost:8080/services/rest/index/my_index/field?default=content&unique=url"
    }).done(function (data) {
      console.log(data);
    });
