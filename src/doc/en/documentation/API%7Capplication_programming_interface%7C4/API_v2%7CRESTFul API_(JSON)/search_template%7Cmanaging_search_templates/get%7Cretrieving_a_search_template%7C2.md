This API returns the content of a search template.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/template/{template_name}```

**Method:** ```GET```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the search template.

**Header** (optional returned type):
- Accept: ```application/json``` or ```application/xml```

### Success response
The field is returned either in JSON or in XML format

**HTTP code:**
200

**Content (application/json):**
    
    {
        "successful": true,
        "query": {
            "start": 0,
            "rows": 10,
            "lang": "ENGLISH",
            "operator": "AND",
            "collapsing": {
                "max": 2,
                "mode": "OFF",
                "type": "OPTIMIZED"
            },
            "returnedFields": [
                "url"
            ],
            "snippets": [
                {
                    "field": "title",
                    "tag": "em",
                    "separator": "...",
                    "maxSize": 200,
                    "maxNumber": 1,
                    "fragmenter": "NO"
                },
                {
                    "field": "content",
                    "tag": "em",
                    "separator": "...",
                    "maxSize": 200,
                    "maxNumber": 1,
                    "fragmenter": "SENTENCE"
                }
            ],
            "enableLog": false,
            "searchFields": [
                {
                    "field": "title",
                    "phrase": true,
                    "boost": 10
                },
                {
                    "field": "content",
                    "phrase": true,
                    "boost": 1
                },
                {
                    "field": "titleExact",
                    "phrase": true,
                    "boost": 10
                },
                {
                    "field": "contentExact",
                    "phrase": true,
                    "boost": 1
                }
            ]
        }
    }
    

### Error response

The search template was not returned. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)

**Content (text/plain):**
    
    Template not found: my_search
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/search/template/my_search
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/search/template/my_search"
    }).done(function (data) {
       console.log(data);
    });
    
