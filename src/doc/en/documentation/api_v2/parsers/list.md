## Getting the list of parsers

This API returns the list of parsers.

**Requirement:** OpenSearchServer v1.5.9

### Call parameters

**URL:** ```/services/rest/parser```

**Method:** ```GET```

**Header** (optional returned type):

- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The list of parsers is returned either in JSON or in XML format.

**HTTP code:**
200

**Content (application/json):**


    {  
	   "successful":true,
	   "info":"24 item(s) found",
	   "items":[  
		  {  
			 "name":"Audio",
			 "link":"/services/rest/parser/audio"
		  },
		  {  
			 "name":"DOC",
			 "link":"/services/rest/parser/doc"
		  },
		  {  
			 "name":"DOCX",
			 "link":"/services/rest/parser/docx"
		  },
		  {  
			 "name":"File system",
			 "link":"/services/rest/parser/filesystem"
		  },
		  {  
			 "name":"HTML",
			 "link":"/services/rest/parser/html"
		  },
		...
    }
    

### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**
    
    An internal error occurred
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/parser
    

**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/parser"
    }).done(function (data) {
       console.log(data);
    });
    
