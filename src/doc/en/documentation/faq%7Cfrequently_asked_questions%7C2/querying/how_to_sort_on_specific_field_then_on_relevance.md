Documents can easily be sorted based on one field, for instance price:

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

But documents with the same `price` value aren't displayed in any specific order.

A second tier of sorting can be added, using `score`. `score` is not a real field within the index for these documents -- it is information that can be used while querrying to sort documents.

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
