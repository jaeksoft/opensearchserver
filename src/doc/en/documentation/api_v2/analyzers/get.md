## Retrieving an analyzer

This API returns the configuration of an analyzer.

**Requirement:** OpenSearchServer v1.5.8

### Call parameters

**URL:** ```/services/rest/index/{index_name}/analyzer/{analyzer_name}/lang/{lang}```

**Method:** ```GET```

**URL parameters:**

- **_index\_name_** (required): The name of the index.
- **_template\_name_** (required): The name of the analyzer.
- **_lang_** (required): The lang of the analyzer.

**Header** (optional returned type):

- Accept: ```application/json``` or ```application/xml```

### Success response
The analyzer is returned either in JSON or in XML format

**HTTP code:**
200

**Content (application/json):**


    {
      successful: true,
      analyzer: {
        queryTokenizer: {
          name: "WhitespaceTokenizer"
        },
        indexTokenizer: {
          name: "LetterOrDigitTokenizerFactory",
        properties: {
          charArrayToken: ""
        }
      },
      filters: [
        {
          name: "ShingleFilter",
          properties: {
            max_shingle_size: "3",
            token_separator: " ",
            min_shingle_size: "1"
          },
          scope: "QUERY"
        },
        {
          name: "RemoveTokenTypeFilter",
          properties: {
            token_type: "shingle"
          },
          scope: "QUERY"
        },
        {
          name: "LowerCaseFilter",
          scope: "QUERY_INDEX"
        },
        {
          name: "ISOLatin1AccentFilter",
          scope: "QUERY_INDEX"
        }
      ]
      }
    }

### Error response

The analyzer was not returned. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)

**Content (text/plain):**
    
    Template not found: my_search
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/analyzer/TextAnalyzer/lang/FRENCH
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/analyzer/TextAnalyzer/lang/FRENCH
    }).done(function (data) {
       console.log(data);
    });
    
