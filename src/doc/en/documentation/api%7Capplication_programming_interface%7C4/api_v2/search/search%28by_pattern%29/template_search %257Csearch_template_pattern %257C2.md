Use this API to search documents using a "search pattern" template request.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/pattern/{template_name}```

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
        "query": "open search server",
        "start": 0,
        "rows": 10,
        "lang": "ENGLISH"
    }
    

It is possible to add more parameters. In this example we add a filter.

    {
        "query": "open search server",
        "start": 0,
        "rows": 10,
        "lang": "ENGLISH",
        "filters": [
          {
            "type": "QueryFilter",
            "negative": false,
            "query": "lang:en"
           }
        ]
    }
    

### Success response
The search result has been returned.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "documents": [
            {
                "pos": 0,
                "score": 0.31121475,
                "collapseCount": 0,
                "fields": [
                    {
                        "fieldName": "url",
                        "values": [
                            "http://www.open-search-server.com/our-references/"
                        ]
                    }
                ],
                "snippets": [
                    {
                        "fieldName": "content",
                        "values": [
                            "Tweets concernant \"#opensearchserve OR opensearchserver OR \"<em>open</em> <em>search</em> <em>server</em>\" OR jaeksoft\"...Follow @OpenSearchServe...© 2013 OpenSearchServer...Responsive Theme powered by..."
                        ],
                        "highlighted": false
                    },
                    {
                        "fieldName": "title",
                        "values": [
                            "www.<em>open</em>-<em>search</em>-<em>server</em>.com - Our references...Our references | OpenSearchServer"
                        ],
                        "highlighted": false
                    }
                ]
            },
            {
                "pos": 1,
                "score": 0.30595505,
                "collapseCount": 0,
                "fields": [
                    {
                        "fieldName": "url",
                        "values": [
                            "http://www.open-search-server.com/legal-information/"
                        ]
                    }
                ],
                "snippets": [
                    {
                        "fieldName": "content",
                        "values": [
                            "The website www.<em>open</em>-<em>search</em>-<em>server</em>.com is created and published by Jaeksoft SaRL, company registered with the Bobigny Commerce Registry (RCS 520 295 551), with its..."
                        ],
                        "highlighted": false
                    },
                    {
                        "fieldName": "title",
                        "values": [
                            "www.<em>open</em>-<em>search</em>-<em>server</em>.com - Legal information...Legal information | OpenSearchServer"
                        ],
                        "highlighted": false
                    }
                ]
            },
    ...
            {
                "pos": 9,
                "score": 0.30048442,
                "collapseCount": 0,
                "fields": [
                    {
                        "fieldName": "url",
                        "values": [
                            "http://www.open-search-server.com/download/"
                        ]
                    }
                ],
                "snippets": [
                    {
                        "fieldName": "content",
                        "values": [
                            "Tweets concernant \"#opensearchserve OR opensearchserver OR \"<em>open</em> <em>search</em> <em>server</em>\" OR jaeksoft\"...Follow @OpenSearchServe...© 2013 OpenSearchServer...Responsive Theme powered by..."
                        ],
                        "highlighted": false
                    },
                    {
                        "fieldName": "title",
                        "values": [
                            "www.<em>open</em>-<em>search</em>-<em>server</em>.com - Download...Download | OpenSearchServer"
                        ],
                        "highlighted": false
                    }
                ]
            }
        ],
        "facets": [],
        "query": "(+title:open^10.0 +title:search^10.0 +title:server^10.0) (+title:\"open search server\"~10^10.0) (+content:open +content:search +content:server) (+content:\"open search server\"~10) (+titleExact:open^10.0 +titleExact:search^10.0 +titleExact:server^10.0) (+titleExact:\"open search server\"~10^10.0) (+contentExact:open +contentExact:search +contentExact:server) (+contentExact:\"open search server\"~10)",
        "rows": 10,
        "start": 0,
        "numFound": 79,
        "time": 101,
        "collapsedDocCount": 0,
        "maxScore": 0.31121475
    }
    

### Error response

The search failed. The reason is provided in the content.

**HTTP code:**
404, 500

    Template not found: my_search
    

### Sample call

**Using CURL:**

    curl -XPOST -H "Content-Type: application/json" \
        -d '{"query":"open search server","start":0,"rows":10,"lang":"ENGLISH"}' \
        http://localhost:8080/services/rest/index/my_index/search/pattern/my_search
    