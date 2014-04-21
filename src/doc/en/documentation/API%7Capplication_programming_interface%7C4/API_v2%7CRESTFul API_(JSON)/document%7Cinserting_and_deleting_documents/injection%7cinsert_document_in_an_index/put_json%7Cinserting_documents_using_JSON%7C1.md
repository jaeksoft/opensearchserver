Use this API to create or update documents in the index.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/document```

**Method:** ```PUT```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Raw data (PUT):**
An array of documents.

    [
      {
        "lang": "ENGLISH",
        "fields": [
          { "name": "id", "value": 1 },
          { "name": "content", "value": "Hello world!", "boost": 1 }
        ]
      },
      {
        "lang": "FRENCH",
        "fields": [
          { "name": "id", "value": 2 },
          { "name": "content", "value": "Bonjour le monde!" }
         ]
       },
       {
        "lang": "GERMAN",
        "fields": [
          { "name": "id", "value": 3 },
          { "name": "content", "value": "Hallo Welt!", "boost": 1 }
         ]
       }
    ]
    
- _**lang**_ (optional): The lang of the document.
- _**fields**_ (required): Array of fields.
  - _**name**_ (required): The name of the field.
  - _**value**_ (required): The content to index.
  - _**boost**_ (optional): A float value.

### Success response
The document(s) has been created or updated.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "3 document(s) updated"
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '[{"lang": "ENGLISH","fields": [{ "name": "id", "value": 1 }]}]' \
        http://localhost:8080/services/rest/index/my_index/document
    
