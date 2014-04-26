This API returns the content of a MoreLikeThis template.

**Requirement:** OpenSearchServer v1.5.3

### Call parameters

**URL:** ```/services/rest/index/{index_name}/morelikethis/template/{template_name}```

**Method:** ```GET```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**template_name**_ (required): The name of the MoreLikeThis template.

**Header** (optional returned type):
- Accept: ```application/json``` or ```application/xml```

### Success response
The field is returned either in JSON format or in XML format.

**HTTP code:**
200

**Content (application/json):**
    
	{
		"successful":true,
		"query":{
			"likeText":"open search server",
			"lang":"ENGLISH",
			"analyzerName":"StandardAnalyzer",
			"fields":[
				"contentExact",
				"titleExact"
				],
			"minWordLen":1,
			"maxWordLen":100,
			"minDocFreq":5,
			"minTermFreq":2,
			"maxNumTokensParsed":5000,
			"maxQueryTerms":25,
			"boost":true,
			"stopWords":
			"English stop words",
			"returnedFields":[
				"contentExact",
				"titleExact"
				],
			"filters":[
				{
					"type":"QueryFilter",
					"negative":false,
					"type":"QueryFilter",
					"query":"lang:en"
				}
			],
			"start":0,
			"rows":10
		}
	}    

### Error response

The MoreLikeThis template was not returned. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)

**Content (text/plain):**
    
    Template not found: my_mlt
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:9090/services/rest/index/my_index/morelikethis/template/my_mlt
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:9090/services/rest/index/my_index/morelikethis/template/my_mlt"
    }).done(function (data) {
       console.log(data);
    });
    
