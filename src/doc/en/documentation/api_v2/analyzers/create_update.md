## Create or update an analyzer

Use this API to create or update an analyzer.

**Requirement:** OpenSearchServer v1.5.8

### Call parameters

**URL:** ```/services/rest/index/{index_name}/analyzer/{analyzer_name}/lang/{lang}```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**

- **_index\_name_** (required): The name of the index.
- **_analyzer\_name_** (required): The name of the analyzer.
- **_lang_** (required): The language of the analyzer.

**Raw data (PUT):**

The configuration of the analyzer in JSON or XML format.

    {
      "queryTokenizer":{"name":"StandardTokenizer"},
      "indexTokenizer":{"name":"StandardTokenizer"},
      "filters":[
        {
          "name":"ShingleFilter",
          "properties":{
            "max_shingle_size":"5",
            "token_separator":" ",
            "min_shingle_size":"1"
          },
          "scope":"QUERY_INDEX"
        },
        {
          "name":"PrefixSuffixStopFilter",
          "properties":{
            "prefixList":"English stop words",
            "ignore_case":"true",
            "token_separator":" ",
            "suffixList":"English stop words"
          },
          "scope":"QUERY_INDEX"
        } 
      ]
    }
    

### Success response
The analyzer has been created or updated.

**HTTP code:**
200

**Content (application/json):**


    {
      "successful":true,
      "details":{
        "transaction":"created"
      }
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '...' \
        http://localhost:8080/services/rest/index/my_index/analyzer/MyAnalyzer/lang/UNDEFINED
    
