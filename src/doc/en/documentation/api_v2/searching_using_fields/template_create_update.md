## Create or update a search template based on fields

Use this API to create or update a search template.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/field/{template_name}```

**Method:** ```PUT```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the search template.

**Raw data (PUT):**
The search field query either in JSON or XML format.

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
        http://localhost:8080/services/rest/index/my_index/search/field/my_search
    