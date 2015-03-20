## Deleting an analyzer

This API deletes an analyzer.

**Requirement:** OpenSearchServer v1.5.12

### Call parameters

**URL:** ```/services/rest/index/{index_name}/analyzer/{analyzer_name}/lang/{lang}```

**Method:** ```DELETE```

**URL parameters:**

- **_index\_name_** (required): The name of the index.
- **_analyzer\_name_** (required): The name of the analyzer.
- **_lang_** (required): The language of the analyzer.

**Header** (optional returned type):

- Accept: ```application/json``` or ```application/xml```

### Success response
Returns 200 if analyzer was successfully deleted.

**HTTP code:**
200

### Error response

The analyzer was not deleted. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)
    

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index/analyzer/TextAnalyzer/lang/FRENCH
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/analyzer/TextAnalyzer/lang/FRENCH
    }).done(function (data) {
       console.log(data);
    });
    
