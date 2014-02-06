This API returns the field list

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/field```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The field list is returned either in JSON or XML format

**HTTP code:**
200

**Content (application/json):**
    
    {
     {
        "successful": true,
        "info": "25 field(s)",
        "unique": "url",
        "default": "title",
        "fields": [        {
                "name": "lang",
                "analyzer": null,
                "indexed": "YES",
                "stored": "NO",
                "termVector": "NO"
            },
            {
                "name": "title",
                "analyzer": "TextAnalyzer",
                "indexed": "YES",
                "stored": "YES",
                "termVector": "POSITIONS_OFFSETS"
            },
            {
                "name": "titleExact",
                "analyzer": "StandardAnalyzer",
                "indexed": "YES",
                "stored": "NO",
                "termVector": "YES"
            },
            {
                "name": "titlePhonetic",
                "analyzer": "PhoneticAnalyzer",
                "indexed": "YES",
                "stored": "NO",
                "termVector": "NO"
            },
            {
                "name": "content",
                "analyzer": "TextAnalyzer",
                "indexed": "YES",
                "stored": "COMPRESS",
                "termVector": "POSITIONS_OFFSETS"
            },
            ...
        ]
      }
    }


### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**
    
    An internal error occurred
    

### Sample call

**Using CURL:**
    
curl -XGET http://localhost:8080/services/rest/index/my_index/field


**Using jQuery:**
    
    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/field"
    }).done(function (data) {
       console.log(data);
    });
