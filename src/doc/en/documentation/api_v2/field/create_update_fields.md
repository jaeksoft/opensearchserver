## Creating or updating fields

Use this API to create fields -- or update existing fields.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/field```

**Method:** ```PUT```

**Header**:
- Content-Type (required): ```application/json```
- Accept (optional returned type): ```application/json```
- Accept (optional returned type): ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Raw data (PUT):**

    [
      {
        "name": "title",
        "analyzer": "TextAnalyzer",
        "indexed": "YES",
        "stored": "YES",
        "termVector": "POSITIONS_OFFSETS"
      },
      {
        "name": "content",
        "analyzer": "TextAnalyzer",
        "indexed": "YES"
        "stored": "YES",
       	"termVector": "POSITIONS_OFFSETS"
      },
      {
        "name": "autocomplete",
        "analyzer": "AutoCompletionAnalyzer",
        "indexed": "YES",
        "copyOf": [
          "content",
          "title"
        ]
      }
    ] 

- name: The name of the field.
- analyzer: The name of the analyzer.
- indexed: YES or NO.
- stored: YES, NO or COMPRESS.
- termVector: YES, NO or POSITIONS_OFFSETS.
- copyOf: An array of fields.

### Success response
The field has been created or updated.

**HTTP code:**
200

**Content (application/json):**
    
    {
        "successful": true,
        "info": "Added Fields"
    }


### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '[{"name":"my_field","analyzer":"TextAnalyzer", "indexed":"YES"}]' \
        http://localhost:8080/services/rest/index/my_index/field
