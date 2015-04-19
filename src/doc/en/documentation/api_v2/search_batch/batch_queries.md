## Batch queries

Use this API to run multiple queries in a single HTTP call.

Queries are given as a JSON array, and results are returned for each query.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/batch```

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.

**Header** (optional returned type):

- Accept: ```application/json``` or ```application/xml```

**Body of the request**:

JSON values:

* **mode**: 
    * `first`: as soon as a query returns at least 1 result, the batch stops then return its results up to and including this query.
    * `all` (default value): results for all queries get returned.
* **queries**: must be an array of queries as JSON objects. Queries must have a type:
    * `SearchFieldTemplate` or `SearchPatternTemplate`: the query uses an existing search template (see [creating a search template based on fields](http://www.opensearchserver.com/documentation/api_v2/searching_using_fields/template_create_update.md) and [creating a search template based on a query pattern](http://www.opensearchserver.com/documentation/api_v2/searching_using_patterns/template_create_update.md)).
    * `SearchField` or `SearchPattern`: the query is fully detailed directly (see [searching using fields](http://www.opensearchserver.com/documentation/api_v2/searching_using_fields/search.md) and [searching using patterns](http://www.opensearchserver.com/documentation/api_v2/searching_using_patterns/search.md)).

Here is an example of a batch of queries where the first and third queries did not return any result.

```json
    { 
	  "mode": "first",
	  "queries": [
	    { 
	      "type": "SearchFieldTemplate",
	      "template": "search",
	      "query": "xxxxxxxxxxx"
	    },
	    { 
	      "type": "SearchPattern",
	      "query": "connor",
	      "start": 0,
	      "rows": 10,
	      "patternSearchQuery": "author:($$)",
	      "returnedFields": [
	            "title",
	            "author"
	        ]
	    },
	    { 
	      "type": "SearchFieldTemplate",
	      "template": "search",
	      "query": "yyyyyyyyy"
	    }
	  ]
    }
```

### Success response
Results are returned.

**HTTP code:**
200

**Content (application/json):**

```json    
	[  
	   {  
	      "successful":true,
	      "facets":[  
	         ...
	      ],
	      "query":"(+title:xxxxxxxxxxx^20.0) (+content:xxxxxxxxxxx^15.0) (+titleStandard:xxxxxxxxxxx^18.0) (+contentStandard:xxxxxxxxxxx^12.0) (+category:xxxxxxxxxxx^5.0) (+author:xxxxxxxxxxx)",
	      "rows":10,
	      "start":0,
	      "numFound":0,
	      "time":4,
	      "collapsedDocCount":0,
	      "maxScore":0.0
	   },
	   {  
	      "successful":true,
	      "documents":[  
	         {  
	            "pos":0,
	            "score":1.1976817,
	            "collapseCount":0,
	            "fields":[  
	               {  
	                  "fieldName":"author",
	                  "values":[  
	                     "Connor Sarah"
	                  ]
	               },
	               ...
	            ]
	         }
	      ],
	      "query":"author:connor",
	      "rows":10,
	      "start":0,
	      "numFound":1,
	      "time":2,
	      "collapsedDocCount":0,
	      "maxScore":1.1976817
	   }
	]
```
   
The third query was not used since the second query returned results. Had the `mode` been `all`, the results would have been:
	
```json
	[  
	   {  
	      "successful":true,
	      ...
	      "numFound":0,
	      "time":60,
	      "collapsedDocCount":0,
	      "maxScore":0.0
	   },
	   {  
	      "successful":true,
	      "documents":[  
	         {  
	            "pos":0,
	            "score":1.1976817,
	            ...
	         }
	      ],
	      "query":"author:connor",
	      "rows":10,
	      "start":0,
	      "numFound":1,
	      "time":3,
	      "collapsedDocCount":0,
	      "maxScore":1.1976817
	   },
	   {  
	      "successful":true,
	      ...
	      "numFound":0,
	      "time":4,
	      "collapsedDocCount":0,
	      "maxScore":0.0
	   }
	]
```

### Error response

An error occured. The reason is provided in the content.

**HTTP code:**
500, 404, 406 (other than 200)
    

### Sample call

**Using CURL:**

    curl -XPOST \ 
		-d '...' \ 
		http://localhost:8080/services/rest/index/my_index/search/batch
    

