Documents can easily be sorted on one field, for instance price:

    {
      "query" : "phone",
      "start" : 0,
      "rows"  : 10,
      "sorts": [
        {
          "field": "price",
          "direction": "ASC"
        }
      ]
    }

But documents with the same `price` values must then seemed to be sorted randomly.

A second sort can be added, on `score`. `score` is not a real field of the documents but it is an information that can be used at query time to sort documents.

    {
      "query" : "phone",
      "start" : 0,
      "rows"  : 10,
      "sorts": [
        {
          "field": "price",
          "direction": "ASC"
        },
        {
          "field": "score",
          "direction": "DESC"
        }
      ]
    }