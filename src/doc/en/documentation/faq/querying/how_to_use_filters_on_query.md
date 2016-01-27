## How to use filters on a query

OpenSearchServer's filtering feature allows for several types of filtering. Here are some common use cases.

### How can I filter for documents that have a non-null value in a specific field?

This can be done using a `QueryFilter` by specifying a value for the `query` property that follows the syntax `<field>:[* TO *]`. 

Here is an example using a `QueryFilter` to filter for documents whose `product_price` field is non-null:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "product_price:[* TO *]"
       }
    ]
 
### How can I filter on a range of values?

Ranges are not expressed using the usual signs ( `>` and `<` ) but rather using `[` and `]`. 

For example, to filter for documents whose title begins with the letter `d`, use a `QueryFilter` with the filter's `query` value set to `title:[d TO *]`.

**Be careful**: this filter CANNOT be applied to a multivalued field. For instance, it cannot be applied to a field that uses the `StandardAnalyzer` analyzer since it tokenizes values, resulting in several values being indexed in the field. Instead, range filters on text values should be applied to fields using a `KeywordLikeAnalyzer`. This analyzer converts all text to lowercase and does not tokenize anything. 

For more information on analyzer behavior, see the documentation on [How To Use Analyzers](../indexing/how_to_use_analyzers.md).

### How can I filter on numerical values?

Numerical values need to be indexed in a field that uses a specific analyzer, for example `DecimalAnalyzer`. For more information on analyzer behavior, see the documentation on [How To Use Analyzers](../indexing/how_to_use_analyzers.md).

When filtering using numerical values, the numbers must have the same format as the one set by the `DecimalAnalyzer` for this field. For example, use a `QueryFilter` with a `query` value of `price:[>0000000045 TO *]` to get documents whose price is greater than 45, assuming `0000000045` is how the analyzer renders the number 45 for that field.

### How can I filter on several values for one field?

It depends on what one wants to do. If one desires to filter for documents that satisfy a **union** of those values, one can do so by defining a `QueryFilter` with multiple values for the target field, separated by spaces. 

For example, if one wanted to view documents that have `store_code` values of **either** `s32` or `s10`:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "store_code:s32 store_code:s10"
      }
    ]
    
If, on the other hand, one desires to filter for documents whose `store_code` property has both values (e.g. the **intersection** of those values), one can use multiple `QueryFilter`:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "store_code:s32"
      },
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "store_code:s10"
      }
    ]


### How can I filter on several values for one field *but* also exclude documents that have a particular value for that field?

Drawing from the above example regarding filtering on multiple values for one field, one could use a single `QueryFilter` to do this. 

For instance:  

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "store_code:[* TO *] -store_code:s10"
      }
    ]
This `QueryFilter` would cause documents to be returned whose value for the `store_code` facet is not null **and** is not `s10`. Note this makes use of the negation syntax using the `-` prefix, indicating the specified value should be used as a "negative" filter.

Alternatively, two `QueryFilter` could also be used here:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "store_code:[* TO *]"
      },
      {
        "type": "QueryFilter",
        "negative": true,
        "query": "store_code:s10"
      }
    ]
    
The first `QueryFilter` causes the search to return only documents whose value for `store_code` is not null. The second `QueryFilter` has the `negative` property set to `true`, indicating it as a "negative" filter. 

### What are "negative" filters, and how can I use them?

"Negative" filters allow one to filter out a subset of documents that would otherwise be returned in the search response. To mark a filter as "negative," just check the `Negative` checkbox in the interface when creating it in a query template, or alternatively set the `"negative"` property to `true` on a filter being passed in the `filters` array.

For example, to return only documents whose `promo` value is **not** equal to `1`:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": true,
        "query": "promo:1"
      }
    ]

Alternatively, one can also use the `-` prefix to specify a filter value should be treated as a "negative" filter:

    "filters": [
      {
        "type": "QueryFilter",
        "negative": false,
        "query": "-promo:1"
      }
    ]
    
This can be useful when chaining together multiple filter terms, removing the need to specify multiple `QueryFilter` to achieve the same effect. See the section on filtering for multiple values for more examples.

### What is the difference between a QueryFilter and a TermFilter?

There isn't much of one, really. In fact, these two filters are equivalent:

    {
       "type": "QueryFilter",
       "negative": false,
       "query": "promo:1"
    },    
    {
       "type": "TermFilter",
       "negative": false,
       "field": "promo",
       "term": "1"
    }

**Important:** When attempting to filter on several values for a single field, use `QueryFilter`, as `TermFilter` cannot be used for this purpose. See the section on  filtering for multiple values for more examples.

### How can I dynamically filter on dates?

When creating a query template it could be useful to filter using a specific range of dates. But of course, the values in this range will change every day, so they cannot be set in a static manner.

`RelativeDateFilter` can be used for this. Let's say that documents are indexed with the current date in the field `indexedDate`. In our example the date is expressed using the `yyyyMMddHHmmss` format, for instance `20141225130512` stands for the 25th of December, 2014, at 1:05:12 PM.

To filter for documents indexed in the last two days one would create the following `RelativeDateFilter`:


      "filters": [ 
         {  
            "type": "RelativeDateFilter",
            "negative": false,            
            "from": {  
               "unit": "days",
               "interval": 2
            },
            "to": {  
               "unit": "days",
               "interval": 0
            },
            "field": "indexedDate",
            "dateFormat":"yyyyMMddHHmmss"
         }
      ]

### How can I use geolocation in filtering?

Use a `GeoFilter`. To learn more, see the documentation on [Geolocation](geolocation.md).