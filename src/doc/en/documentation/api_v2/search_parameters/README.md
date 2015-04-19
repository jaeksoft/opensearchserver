## List of available parameters for search queries

OpenSearchServer provides two kinds of queries: **Search field** and **Search pattern**. Both share most of the parameters, and a few of them are dedicated to one or the other type.

Each parameter is detailed with an example and a short description.

### Common parameters

#### `query`
    "query": "The Count of Monte-Cristo",

The keywords to search for.

#### `start`
    "start":0,

Starting offset. Will be used for navigating through results.

#### `rows`

    "rows":10,

Number of returned results.

#### `lang`

    "lang":"ENGLISH",

Language of the given keywords. This is used to apply corresponding TextAnalyzer to the fields using it. Do not use this parameter to filter on language, rather use a `filter`.

#### `operator`

    "operator":"AND",

Query operator, can be `OR` or `AND`. This operator is applied to the keywords when there are more than one keyword. For example, using an `AND` when searching "space companies" will match text `History of space companies` and will not match `Space program`. Using an `OR`, `Space program` would have matched.

#### `emptyReturnsAll`

    "emptyReturnsAll":true,

Whether empty query should return all results or none.

#### `enableLog`

    "enableLog":false,

Whether logging should be enabled or not. Logging queries allow for reporting in tab `Report`.

#### `collapsing`

    "collapsing":{  
         "field":"url",
         "max":2,
         "mode":"CLUSTER",
         "type":"FULL"
    },

Collapsing groups some results together: only one result would be returned. Results are grouped if they share the same value for the given `field`.

Collapsing can be in mode `ADJACENT` or `CLUSTER`. In adjacent mode it will only group adjacent results sharing the same value in the given field, whereas in mode cluster it will group all results, whatever their position is.

Type can be `FULL` or `OPTIMIZED`.

#### `geo`

    "geo": {
        "latitudeField": "latitude",
        "longitudeField": "longitude",
        "latitude": 48.85341,
        "longitude": 2.3488,
        "coordUnit": "DEGREES"
    },

Geolocation search. See our [Geolocation page](http://www.opensearchserver.com/documentation/faq/querying/geolocation.md) for all information.



#### `returnedFields`

    "returnedFields":[  
      "id",
      "author",
      "category",
      "date",
      "dateCrawl"
    ],

Fields that must be included for every result.

#### `snippets`

    "snippets":[  
      {  
         "field":"title",
         "tag":"b",
         "separator":"...",
         "maxSize":200,
         "maxNumber":1,
         "fragmenter":"NO"
      },
      {  
         "field":"content",
         "tag":"b",
         "separator":"...",
         "maxSize":700,
         "maxNumber":1,
         "fragmenter":"SENTENCE"
      }
    ],

Snippets are highlighted words matching the query. Words matching the query will be surrounded by the `tag`. Snippet will end with `separator` after `maxSize` characters.

`fagmenter` tells how the snippet is built: it can be built by trying to keep a "phrase" (`SENTENCE`) or not (`NO`).

#### `facets`

    "facets":[  
      {  
         "field":"date_monthyear",
         "minCount":1,
         "multivalued":false,
         "postCollapsing":false
      },
      {  
         "field":"authorFirstLetter",
         "minCount":1,
         "multivalued":false,
         "postCollapsing":false
      },
      {  
         "field":"categoryKeyword",
         "minCount":1,
         "multivalued":true,
         "postCollapsing":false
      }
    ],

Facets are filters with counters. Returned response will have a `facets` section, showing for each field every values + number of documents having each value in that field. 

#### `filters`

    "filters":[  
         {  
            "type":"QueryFilter",
            "negative":true,
            "query":"status:archived"
         },
         {  
            "type":"TermFilter",
            "negative":false,
            "field":"published",
            "term":"1"
         },
         {  
            "type":"RelativeDateFilter",
            "negative":false,
            "from":{  
               "unit":"days",
               "interval":1
            },
            "to":{  
               "unit":"days",
               "interval":0
            },
            "field":"crawl_date",
            "dateFormat":"yyyyMMddHHmmss"
         }
    ],

Filters set on query. See the page [How to use filters on query](http://www.opensearchserver.com/documentation/faq/querying/how_to_use_filters_on_query.md) for all information.

#### `filterOperator`

    "filterOperator":"OR",

Operator used on the different filters. Default is `AND` (each filter must match), but you can set it to `OR`, thus asking for at least one filter to match.

#### `boostingQueries`

    "boostingQueries":[  
        {  
            "patternQuery":"author:john",
            "boost":5.0
        },
        {  
            "patternQuery":"author:will",
            "boost":0.1
        }
    ],

Boosting queries are used to change the full-text score of the results. Score can be increased (boost > 1) or decreased (boost < 1).

#### `sorts`

    "sorts":[  
         {  
            "field":"score",
            "direction":"DESC",
            "empty":"last"
         },
         {  
            "field":"author",
            "direction":"ASC",
            "empty":"last"
         }
    ],

Documents are sorted by default with their **score**. 

It can be totally changed (to sort for instance by date, or by title). It can also be improved: this example shows how documents having the same score will be sorted alphabetically by author afterwards. More details can be found [on this page](http://www.opensearchserver.com/documentation/faq/querying/how_to_sort_on_specific_field_then_on_relevance.md).

#### `scorings`

    "scorings":[  
         {  
            "ascending":false,
            "fieldName":"date",
            "weight":2.0,
            "type":"FIELD_ORDER"
         }
    ],

Scoring always use first the internal "full-text" score, but some levels can be added afterwards. This example shows how field `date` can be used to tune score of the results.


#### `joins`

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

OpenSearchServer provides an useful `join` feature, allowing for joining several index.

### Parameter for query type "Search field"

#### `searchFields`

    "searchFields":[  
         {  
            "field":"title",
            "mode":"PATTERN",
            "boost":20.0,
            "phraseBoost":20.0
         },
         {  
            "field":"content",
            "mode":"PHRASE",
            "boost":15.0,
            "phraseBoost":15.0
         },
         {  
            "field":"titleStandard",
            "mode":"TERM",
            "boost":18.0,
            "phraseBoost":18.0
         },
         {  
            "field":"contentStandard",
            "mode":"TERM_AND_PHRASE",
            "boost":12.0,
            "phraseBoost":12.0
         },
         {  
            "field":"category",
            "mode":"PATTERN",
            "boost":5.0,
            "phraseBoost":5.0
         },
         {  
            "field":"author",
            "mode":"PATTERN",
            "boost":1.0,
            "phraseBoost":1.0
         }
    ]

This parameter is used to tell OpenSearchServer which field must be used for the search. Each field can use a different search `mode`. See the [How to make an exact search](http://www.opensearchserver.com/documentation/faq/querying/how_to_make_exact_search.md) page to learn more about this.


### Parameter for query type "Search pattern"

#### `patternSearchQuery`

```
"patternSearchQuery": "title:($$)^10 OR titleExact:($$)^10 OR titlePhonetic:($$)^10 OR url:($$)^5 OR urlSplit:($$)^5 OR urlExact:($$)^5 OR urlPhonetic:($$)^5 OR content:($$) OR contentExact:($$) OR contentPhonetic:($$) OR full:($$)^0.1 OR fullExact:($$)^0.1 OR fullPhonetic:($$)^0.1",
```

For query of type "Search pattern" parameter `patternSearchQuery` is used in place of `searchFields`. This parameter uses a particular syntax. `$$` stands for "searched keywords", and weight of each field is given by using `^{weight}`.

#### `patternSnippetQuery`

    "patternSnippetQuery": "title:($$) OR content:($$)"