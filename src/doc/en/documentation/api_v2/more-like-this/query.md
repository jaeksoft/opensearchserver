## Running a More-Like-This query without template

Use this API to search documents using a "More Like This" request without a template.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis```

**Method:** ```POST```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Raw data (POST):**
The search field query either in JSON or XML format.

	{
		"likeText": "open search server",
		"lang": "ENGLISH",
		"analyzerName": "StandardAnalyzer",
		"fields": [
			"contentExact",
			"titleExact"
		],
		"minWordLen": 1,
		"maxWordLen": 100,
		"minDocFreq": 1,
		"minTermFreq": 1,
		"maxNumTokensParsed": 5000,
		"maxQueryTerms": 25,
		"boost": true,
		"stopWords": "English stop words",
		"returnedFields": [
			"url",
			"title"
		],
		"filters": [
			{
				"type": "QueryFilter",
				"negative": false,
				"query": "lang:en"
			}
		],
		"start": 0,
		"rows": 10
	}

### Success response
The search result has been returned.

**HTTP code:**
200

**Content (application/json):**

	{
		"documents":[
			{
				"pos":0,
				"score":0.19919153,
				"collapseCount":0,
				"fields":[
					{
						"fieldName":"title",
						"values":[
							"www.open-search-server.com - OpenSearchServer documentation",
							"Checking the environment variables on Windows - OpenSearchServer documentation - OpenSearchServer documentation"
						]
					},
					{
						"fieldName":"url",
						"values":[
							"http://www.open-search-server.com/confluence/display/EN/Checking+the+environment+variables+on+Windows"
						]
					}
				]
			}
		...
		]
	}
	
### Error response

The search failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPOST -H "Content-Type: application/json" \
        -d '...' \
        http://localhost:9090/services/rest/index/my_index/morelikethis
        