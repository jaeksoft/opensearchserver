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

However, documents with the same `price` value aren't displayed in any specific order.

A trick to better order such results is to use the relevance score. `score` is not a real field within the index for these documents -- it is a relevance rank calculated during the querrying. However, it can be used as a second tier of sorting while running the querry.

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
