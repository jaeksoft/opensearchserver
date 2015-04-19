## Get the list of the More-Like-This templates

This API returns templates. A template is a query stored with its parameters. It can be easily re-called using its name.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis/template```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The field list is returned either in JSON format or in XML format.

**HTTP code:**
200

**Content (application/json):**

	{
		"successful":true,
		"info":"1 template(s)",
		"templates":[
			{
				"name":"mlt",
				"type":"More like this"
			}
		]
	}    

### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**
    
    An internal error occurred.
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:9090/services/rest/index/my_index/morelikethis/template
    

**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:9090/services/rest/index/my_index/morelikethis/template"
    }).done(function (data) {
       console.log(data);
    });
    
