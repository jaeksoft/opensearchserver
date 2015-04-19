## Create or update a search template based on a query pattern

Use this API to create or update a search template based on a query pattern.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/pattern/{template_name}```

**Method:** ```PUT```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the search template.

**Raw data (PUT):**
The search pattern query in either JSON or XML format.

    {
            "start": 0,
            "rows": 10,
            "lang": "ENGLISH",
            "operator": "AND",
            "collapsing": {
                "max": 0,
                "mode": "OFF",
                "type": "OPTIMIZED"
            },
            "returnedFields": [
                "url",
                "backlinkCount"
            ],
            "snippets": [
                {
                    "field": "title",
                    "tag": "b",
                    "separator": "...",
                    "maxSize": 200,
                    "maxNumber": 1,
                    "fragmenter": "NO"
                },
                {
                    "field": "content",
                    "tag": "b",
                    "separator": "...",
                    "maxSize": 200,
                    "maxNumber": 20,
                    "fragmenter": "SENTENCE"
                }
            ],
            "facets": [
                {
                    "field": "lang",
                    "minCount": 1,
                    "multivalued": false,
                    "postCollapsing": false
                }
            ],
            "enableLog": false,
            "patternSearchQuery": "title:($$)^10 OR OR\ntitleExact:($$)^10 OR\ntitlePhonetic:($$)^10 OR url:($$)^5 OR urlSplit:($$)^5 OR urlExact:($$)^5 OR urlPhonetic:($$)^5 OR content:($$) OR contentExact:($$) OR contentPhonetic:($$) OR full:($$)^0.1 OR fullExact:($$)^0.1 OR fullPhonetic:($$)^0.1",
            "patternSnippetQuery": "title:($$) OR content:($$)"

    }
  
See page [List of available parameters for search queries](../search_parameters/README.md) for the full list of parameters.  

### Success response
The search template has been created or updated.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Template updated: my_search"
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '...' \
        http://localhost:8080/services/rest/index/my_index/search/pattern/my_search
    
