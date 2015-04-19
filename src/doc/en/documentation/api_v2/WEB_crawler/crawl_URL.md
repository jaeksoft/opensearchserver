## Crawling one URL

Use this API to crawl a page by passing its URL.

The URL must match the pattern list. 

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/crawl?url={url}&returnData=true```

**Method:** ```GET```

**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index
- _**url**_ (required): The URL to crawl
- _**returnData**_ (optionnal): If set to true will return a JSON array with the extracted data

### Success response
The page has been crawled.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "Result: Fetched - Parsed - Indexed",
	    "details":{  
			"ContentBaseType":"text/html",
			"ContentLength":"-1",
			"ContentTypeCharset":"UTF-8",
			"FetchStatus":"Fetched",
			"HttpResponseCode":"200",
			"IndexStatus":"Indexed",
			"ParserStatus":"Parsed",
			"RobotsTxtStatus":"Allow",
			"URL":"http://www.loremipsum.dolor/"
		},
		"items":[  
		[  
			{  
				"fieldName":"title",
				"values":[  
				   "Lorem ipsum dolor sit amet"
				]
			},
			{  
				"fieldName":"content",
				"values":[  
				   "Vivamus consectetur lorem at metus lobortis, a ullamcorper sapien ornare. Donec et ornare mauris, at",
				   "interdum libero. Fusce tempor purus laoreet, eleifend mi in, elementum velit. Nunc aliquet vulputate urna"
				}
			}
		]
    }


### Error response

The index has not been found.

**HTTP code:**
404

**Content (text/plain):**

    The index my_index has not been found


### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/web/crawl?url=http://www.example.org/&returnData=true


**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/crawl?url=http://www.example.org/&returnData=true"
    }).done(function (data) {
       console.log(data);
    });
