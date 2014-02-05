This API creates a new index.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/template/{template_name}```

**Method:** ```POST```

**Header** (returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index
- _**template_name**_ (optional): The name of the template: EMPTY_INDEX, WEB_CRAWLER or FILE_CRAWLER.

### Success response
The index has been created.

**HTTP code:**: 200

**Content (application/json):**

    {
      result: {
        @successful: "true",
        info: "Created Index my_index"
      }
    }

### Error response

The index creation failed. The reason is provided in the content.

**HTTP code:**: 500

**Content (text/plain):**

    directory my_index already exists

### Sample call

**Using CURL:**

    curl -XPOST http://localhost:8080/services/rest/index/my_index/template/WEB_CRAWLER

**Using jQuery:**

    $.ajax({ 
      type: "POST",
      dataType: "json",
      url: "http://localhost:8080/services/rest/index/my_index/template/WEB_CRAWLER"
      }).done(function (data) {
      console.log(data);
    });