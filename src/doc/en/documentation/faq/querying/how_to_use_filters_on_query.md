## How to use filters on query

OpenSearchServer's filtering feature allows for several types of filtering. Here are some common use cases.

### How can I get documents having not null value in a specific field?

This can be done using a `Query filter`. Use value `<field>:[* TO *]`. For example `product_price:[* TO *]`.
 
### How can I filter on some range of values?

Ranges are not expressed with signs `>` and `<` but rather with `[` and `]`. 

For example to filter on documents whose title begins by the letter `d` use a `Query filter` with value `title:[d TO *]`.

**Be careful**: this filter needs to be applied to a field that is not multivalued. This means for example that it cannot be applied to a field that uses analyzer `StandardAnalyzer` since this analyzer **tokenizes** values, and thus results in several values being indexed in the field. Have a look at the [How to use analyzer page](../indexing/how_to_use_analyzers.md) to understand it better. Range filters on text values should be applied to fields using a `KeywordLikeAnalyzer`: this analyzer converts all the text in lowercase and does not tokenize it. 

### How can I filter on numerical values?

Numerical values need to be indexed in a field that uses a specific analyzer, for example `DecimalAnalyzer`. Please refer to the [How to use analyzer page](../indexing/how_to_use_analyzers.md) to understand it better.

Filters on numerical values need to use same format of number than the one given by the DecimalAnalyzer used on the field. For example use a `Query filter` with value `price:[>0000000045 TO *]` to get documents whose price is greater than 45. 

### How can I filter on several values for one field?

Use a `Query filter` and repeat the filter with different values, for example: `store_code:s32 store_code:s10 store_code:45`.

### How can I filter on several values for one field but at the same time excluding documents having a particular value in that field?

One could use a `Query filter` to do this, for instance:  `store_code:[* TO *] -store_code:45` would give documents where `store_code` is not null but without those whose `store_code` is `s45`.

Two `Query filter` could also be used here:

* the first one to get all documents whose `store_code` is not null: `store_code:[* TO *]`
* the second one to exclude some documents. Use for example a `Query filter` with value `store_code:45` and check the `Negative` checkbox (this is the same than using a `Query filter` with value `-store_code:45`).

### How can I use a "negative" filter?

Just check the `Negative` checkbox in the interface when creating the filter in a query template, or write `"negative": true,` in the "filters" part of the JSON sent to the API.

    "filters": [
      {
        "type": "QueryFilter",
        "negative": true,
        "query": "product_price:[* TO *]"
       }
    ]

One could also use a `Query filter` and prefix its value with `-`, for instance `-promo:1`.

### What is the difference between Query Filter and Term Filter

There is kind of no difference between those two. A `Query filter` with value `promo:1` is the same than a `Term filter` with Field = `promo` and Term = `1`.

However when it comes to filtering on several values for one field then you would want to use `Query filter` since `Term filter` can not be used for this purpose. 

### How can I dynamically filter on dates?

When creating a query template it could be useful to be able to filter on a specific range of dates. This range could of course not being written statically in the query template since it has to change every day.

`Relative date filter` can be used for this. Let's say that documents are indexed with the current date in the field `indexedDate`. This date would be for example `20141225130512` for the 25th of december, 2014, at 1:05:12 PM.
This format can  be expressed as `yyyyMMddHHmmss`.

To filter on document indexed in the last two days one would create this `Relative date filter`:

* Field: `indexedDate`
* Date format: `yyyyMMddHHmmss`
* From interval: `2`
* From unit: `Days`
* To interval: `0`
* To unit: `Days`


         "filters":[  
           {  
            "negative":false,
            "type":"RelativeDateFilter",
            "from":{  
               "unit":"days",
               "interval":2
            },
            "to":{  
               "unit":"days",
               "interval":0
            },
            "field":"indexedDate",
            "dateFormat":"yyyyMMddHHmmss"
           }
        ],

### How can I use geolocation in filtering?

Use a `Geo filter`. Please see page [Geolocation](geolocation.md) to learn more about this.