Les documents peuvent facilement être triés sur un champ spécifique, par exemple le champ `price` :

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

Mais les documents avec la même valeur pour le champ `price` semblent ensuite être triés aléatoirement. 

Un second niveau de tri peut être ajouté, sur le champ `score`. `score` n'est pas un vrai champ du schéma mais c'est une information qui peut être utilisée au moment de la query pour trier les documents selon leur pertinence par rapport à la requête :

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