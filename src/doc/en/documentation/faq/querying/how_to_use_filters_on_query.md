## How to use filters on a query

OpenSearchServer's filtering feature allows for several types of filtering. Here are some common use cases.

### How can I get documents that have a non-null value in a specific field?

This can be done using a `Query filter`. Use value `<field>:[* TO *]`. For example `product_price:[* TO *]`.
 
### How can I filter on a range of values?

Ranges are not expressed using the usual signs ( `>` and `<` ) but rather using `[` and `]`. 

For example to filter on documents whose title begins by the letter `d`, use a `Query filter` with the value `title:[d TO *]`.

**Be careful**: this filter CANNOT be applied to a multivalued field. For instance, it cannot be applied to a field that uses the `StandardAnalyzer` analyzer since this one **tokenizes** values, which results in several values being indexed in the field. Have a look at the [How to use analyzer page](../indexing/how_to_use_analyzers.md) to better understand this. Range filters on text values should be applied to fields using a `KeywordLikeAnalyzer`. This analyzer just converts all text to lowercase and does not tokenize anything. 

### How can I filter on numerical values?

Numerical values need to be indexed in a field that uses a specific analyzer, for example `DecimalAnalyzer`. Please refer to the [How to use analyzers page](../indexing/how_to_use_analyzers.md) to better understand this analyser.

When filtering using numerical values, the numbers must have the same format as the one set by the DecimalAnalyzer for this field. For example use a `Query filter` with the value `price:[>0000000045 TO *]` to get documents whose price is greater than 45, if that is how the analyzer renders 45 for that field.

### How can I filter on several values for one field?

Use a `Query filter` and repeat it using different values for each pass. Example: `store_code:s32 store_code:s10 store_code:45`.

### How can I filter on several values for one field *but* also exclude documents that have a particular value in that field?

One could use a `Query filter` to do this, for instance:  `store_code:[* TO *] -store_code:45` would return documents where `store_code` is not null - and where the `store_code` *isn't* `s45`.

Two `Query filter` could also be used here:

* the first one to get all documents whose `store_code` is not null: `store_code:[* TO *]`
* the second one to exclude some documents. Use for example a `Query filter` with the value `store_code:45` and check the `Negative` checkbox (this is the same thing as using a `Query filter` with the value `-store_code:45`).

### How can I use a "negative" filter?

Just check the `Negative` checkbox in the interface when creating the filter in a query template, or alternatively write `"negative": true,` in the "filters" part of the JSON sent to the API.

    "filters": [
      {
        "type": "QueryFilter",
        "negative": true,
        "query": "product_price:[* TO *]"
       }
    ]

One can also use a `Query filter` and prefix its value with a `-` sign, for instance `-promo:1`, to make it a negative filter.

### What is the difference between a Query Filter and a Term Filter

There isn't much of one, really. A `Query filter` with the value `promo:1` is the same than a `Term filter` with Field = `promo` and Term = `1`.

However when it comes to filtering on several values for one field then you'll want to use a `Query filter` since a `Term filter` can not be used for this purpose. 

### How can I dynamically filter on dates?

When creating a query template it could be useful to filter using a specific range of dates. But of course, the values in this range will change every day, so they cannot be set in a static manner.

The `Relative date filter` can be used for this. Let's say that documents are indexed with the current date in the field `indexedDate`. In our example the date is expressed using the `yyyyMMddHHmmss` format - for instance `20141225130512` stands for the 25th of December, 2014, at 1:05:12 PM.

To filter on documents indexed in the last two days one would create the following `Relative date filter`:

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

Use a `Geo filter`. Please see the [Geolocation](geolocation.md) page to learn more about this.
