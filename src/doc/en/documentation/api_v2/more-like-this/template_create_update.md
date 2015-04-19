## Create or update a _More-Like-This template

Use this API to create or update a search template.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis/template/{template_name}```

**Method:** ```PUT```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the search template.

**Raw data (PUT):**
The MoreLikeThis query either in JSON or XML format.

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
The MoreLikeThis template has been created or updated.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Template updated: my_mlt"
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '...' \
        http://localhost:9090/services/rest/index/my_index/morelikethis/template/my_mlt
    