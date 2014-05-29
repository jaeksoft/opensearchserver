## Running a More-Like-This query

Use this API to search documents using a "MoreLikeThis" template request.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis/template/{template_name}```

**Method:** ```POST```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the template.

**Raw data (POST):**
The overridden search parameters either in JSON or XML format.

	{
		"likeText": "open search server",
		"start": 0,
		"rows": 10
	}    

### Success response
The search result has been returned.

**HTTP code:**
200

**Content (application/json):**

	{
		"documents": [
			{
				"pos": 0,
				"score": 0.19919153,
				"collapseCount": 0,
				"fields": [
					{
						"fieldName": "title",
						"values": [
							"www.open-search-server.com - OpenSearchServer documentation",
							"Checking the environment variables on Windows - OpenSearchServer documentation - OpenSearchServer documentation"
						]
					},
					{
						"fieldName": "url",
						"values": [
							"http://www.open-search-server.com/confluence/display/EN/Checking+the+environment+variables+on+Windows"
						]
					}
				]
			},
			...
			{
				"pos": 9,
				"score": 0.006719038,
				"collapseCount": 0,
				"fields": [
					{
						"fieldName": "title",
						"values": [
							"www.open-search-server.com - Query filters",
							"Query filters - OpenSearchServer Wiki"
						]
					},
					{
						"fieldName": "url",
						"values": [
							"http://www.open-search-server.com/wiki/en/index.php/Query_filters"
						]
					}
				]
			}
		]
	}
    

### Error response

The search failed. The reason is provided in the content.

**HTTP code:**
404, 500

    Template not found: test3
    

### Sample call

**Using CURL:**
 
    curl -XPOST -H "Content-Type: application/json" \
        -d '{"likeText":"open search server","start":0,"rows":10}' \
        http://localhost:9090/services/rest/index/my_index/morelikethis/template/my_mlt
    