Use this API to search documents using a "search pattern" request.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/search/pattern```

**Method:** ```POST```

**Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Raw data (POST):**
The search pattern query either in JSON or XML format.

    {
        "query": "open search server",
        "start": 0,
        "rows": 10,
        "lang": "ENGLISH",
        "operator": "AND",
        "collapsing": {
            "max": 0,
            "mode": "OFF",
            "type": "OPTIMIZED"
        },
            "returnedFields": [
            "url",
            "backlinkCount"
        ],
        "snippets": [
            {
                "field": "title",
                "tag": "b",
                "separator": "...",
                "maxSize": 200,
                "maxNumber": 1,
                "fragmenter": "NO"
            },
            {
                "field": "content",
                "tag": "b",
                "separator": "...",
                "maxSize": 200,
                "maxNumber": 20,
                "fragmenter": "SENTENCE"
            }
        ],
        "facets": [
            {
                "field": "lang",
                "minCount": 1,
                "multivalued": false,
                "postCollapsing": false
            }
        ],
        "joins": [
            {
               "indexName": "types",
               "queryTemplate": "search",
               "queryString": "type:\"Comics\"",
               "localField": "url",
               "foreignField": "url",
               "type": "INNER",
               "returnFields": true,
               "returnScores": false,
               "returnFacets": false
            }
         ],
        "enableLog": false,
        "patternSearchQuery": "title:($$)^10 OR titleExact:($$)^10 OR titlePhonetic:($$)^10 OR url:($$)^5 OR urlSplit:($$)^5 OR urlExact:($$)^5 OR urlPhonetic:($$)^5 OR content:($$) OR contentExact:($$) OR contentPhonetic:($$) OR full:($$)^0.1 OR fullExact:($$)^0.1 OR fullPhonetic:($$)^0.1",
        "patternSnippetQuery": "title:($$) OR content:($$)"
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
                "score": 0.067802265,
                "collapseCount": 0,
                "fields": [
                    {
                        "fieldName": "backlinkCount",
                        "values": [
                            ">0000000000"
                        ]
                    },
                    {
                        "fieldName": "url",
                        "values": [
                            "http://www.open-search-server.com/features/"
                        ]
                    }
                ],
                "snippets": [
                    {
                        "fieldName": "content",
                        "values": [
                            "Tweets concernant \"#opensearchserve OR opensearchserver OR \"<b>open</b> <b>search</b> <b>server</b>\" OR jaeksoft\"...Follow @OpenSearchServe...© 2013 OpenSearchServer...Responsive Theme powered by...",
                            "OpenSearchServer is an open source search engine and crawler software based on the best open source technologies. ...Search functions...Advanced full-text search features...Phonetic search...Advanced...",
                            "Filter search using sub-requests (including negative filters)...Geolocation...Spell-checking...Relevance customization using algebraic functions...Search suggestion facility (auto-completion)...Parsers",
                            "The database crawler for all JDBC databases (MySQL, PostgreSQL, Oracle, SQL Server, …)...Each crawler offers a list of parameters allowing developers to customize its behavior. ...Filter inclusion or...",
                            "Support Center...SaaS Center...Monitoring Center...EN...FR...Features...Support...SaaS...Documentation...Download...Community...About us...Contact...Newsletter...References...Features...Clustered results...",
                            "Parsers recognize and analyze the MIME type of crawled documents or file extensions and then automatically extract information necessary for indexing (Title, Text, Author, hypertext links, etc.). ...HTML...",
                            "Supported formats are:...MS Office documents (Word, Excel, Powerpoint)...OpenOffice documents...Adobe PDF (with OCR)...RTF, Plaintext...Audio files metadata (wav, mp3, AIFF, Ogg)...Torrent files...OCR...",
                            "General...REST API (XML and JSON)...SOAP Web Service...Monitoring module...Index replication...Scheduler for management of periodic tasks...WordPress plugin and Drupal module...Indexation...Supports 17...",
                            "Fields schema with analyzers in each language...Several filters: n-gram, lemmatization, shingle, stripping diacritic from words,…...Automatic language recognition...Named entity recognition...Word...",
                            "Export indexed terms with frequencies...Crawlers...This is the module which creates the index that will process the queries and return answers. ...OpenSearchServer is equipped with several crawlers that...",
                            "...t:...The web crawler for internet, extranet and intranet...The file systems crawler for local and remote files (NFS, SMB/CIFS, FTP, FTPS)...Session parameters  removal...SQL join and linked files support",
                            "... remote files (NFS, SMB/CIFS, FTP, FTPS)...Session parameters  removal...SQL join and linked files support...Screenshot capture...Sitemap import...About us...Contact...Newsletter...References...Follow us"
                        ],
                        "highlighted": false
                    },
                    {
                        "fieldName": "title",
                        "values": [
                            "www.<b>open</b>-<b>search</b>-<b>server</b>.com - Features...Features | OpenSearchServer"
                        ],
                        "highlighted": false
                    }
                ]
            },
            {
                "pos": 1,
                "score": 0.06748175,
                "collapseCount": 0,
                "fields": [
                    {
                        "fieldName": "backlinkCount",
                        "values": [
                            ">0000000000"
                        ]
                    },
                    {
                        "fieldName": "url",
                        "values": [
                            "http://www.open-search-server.com/contact/"
                        ]
                    }
                ],
                "snippets": [
                    {
                        "fieldName": "content",
                        "values": [
                            "Tweets concernant \"#opensearchserve OR opensearchserver OR \"<b>open</b> <b>search</b> <b>server</b>\" OR jaeksoft\"...Follow @OpenSearchServe...© 2013 OpenSearchServer...Responsive Theme powered by...",
                            "Support Center...SaaS Center...Monitoring Center...EN...FR...Features...Support...SaaS...Documentation...Download...Community...About us...Contact...Newsletter...References...Contact...You want to know...",
                            "Understand setup and installation...Choose the right configuration...Get more information about pricing...Send us your details and fill in the form below, we will contact you shortly. ...Your name...",
                            "... and fill in the form below, we will contact you shortly. ...Your name (required)...Your email (required)...Subject...Your message (required)...About us...Contact...Newsletter...References...Follow us"
                        ],
                        "highlighted": false
                    },
                    {
                        "fieldName": "title",
                        "values": [
                            "www.<b>open</b>-<b>search</b>-<b>server</b>.com - Contact...Contact | OpenSearchServer"
                        ],
                        "highlighted": false
                        }
                ]
            },
    ...
        ],
        "facets": [
            {
                "fieldName": "lang",
                "terms": [
                    {
                        "term": "en",
                        "count": 49
                    },
                    {
                        "term": "fr",
                        "count": 25
                    },
                    {
                        "term": "hu",
                        "count": 3
                    }
                ]
            }
        ],
        "query": "((+title:open +title:search +title:server)^10.0) ((+titleExact:open +titleExact:search +titleExact:server)^10.0) ((+titlePhonetic:opn +(+titlePhonetic:sDrtS +titlePhonetic:sDrts +titlePhonetic:sDrx +titlePhonetic:sDrz +titlePhonetic:sartS +titlePhonetic:sarts +titlePhonetic:sarx +titlePhonetic:sarz +titlePhonetic:siarx +titlePhonetic:siorx +titlePhonetic:sirtS +titlePhonetic:sirts +titlePhonetic:sirx +titlePhonetic:sirz +titlePhonetic:zDrx +titlePhonetic:zarx +titlePhonetic:zirx) +(+titlePhonetic:sirbir +titlePhonetic:sirvi +titlePhonetic:sirvir +titlePhonetic:zirvir))^10.0) ((+url:open +url:search +url:server)^5.0) ((+urlSplit:open +urlSplit:search +urlSplit:server)^5.0) ((+urlExact:open +urlExact:search +urlExact:server)^5.0) ((+urlPhonetic:opn +(+urlPhonetic:sDrtS +urlPhonetic:sDrts +urlPhonetic:sDrx +urlPhonetic:sDrz +urlPhonetic:sartS +urlPhonetic:sarts +urlPhonetic:sarx +urlPhonetic:sarz +urlPhonetic:siarx +urlPhonetic:siorx +urlPhonetic:sirtS +urlPhonetic:sirts +urlPhonetic:sirx +urlPhonetic:sirz +urlPhonetic:zDrx +urlPhonetic:zarx +urlPhonetic:zirx) +(+urlPhonetic:sirbir +urlPhonetic:sirvi +urlPhonetic:sirvir +urlPhonetic:zirvir))^5.0) (+content:open +content:search +content:server) (+contentExact:open +contentExact:search +contentExact:server) (+contentPhonetic:opn +(+contentPhonetic:sDrtS +contentPhonetic:sDrts +contentPhonetic:sDrx +contentPhonetic:sDrz +contentPhonetic:sartS +contentPhonetic:sarts +contentPhonetic:sarx +contentPhonetic:sarz +contentPhonetic:siarx +contentPhonetic:siorx +contentPhonetic:sirtS +contentPhonetic:sirts +contentPhonetic:sirx +contentPhonetic:sirz +contentPhonetic:zDrx +contentPhonetic:zarx +contentPhonetic:zirx) +(+contentPhonetic:sirbir +contentPhonetic:sirvi +contentPhonetic:sirvir +contentPhonetic:zirvir)) ((+full:open +full:search +full:server)^0.1) ((+fullExact:open +fullExact:search +fullExact:server)^0.1) ((+fullPhonetic:opn +(+fullPhonetic:sDrtS +fullPhonetic:sDrts +fullPhonetic:sDrx +fullPhonetic:sDrz +fullPhonetic:sartS +fullPhonetic:sarts +fullPhonetic:sarx +fullPhonetic:sarz +fullPhonetic:siarx +fullPhonetic:siorx +fullPhonetic:sirtS +fullPhonetic:sirts +fullPhonetic:sirx +fullPhonetic:sirz +fullPhonetic:zDrx +fullPhonetic:zarx +fullPhonetic:zirx) +(+fullPhonetic:sirbir +fullPhonetic:sirvi +fullPhonetic:sirvir +fullPhonetic:zirvir))^0.1)",
        "rows": 10,
        "start": 0,
        "numFound": 79,
        "time": 28,
        "collapsedDocCount": 0,
        "maxScore": 0.067802265
    }
    

### Error response

The search failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPOST -H "Content-Type: application/json" \
        -d '...' \
        http://localhost:8080/services/rest/index/{my_index}/search/pattern
    