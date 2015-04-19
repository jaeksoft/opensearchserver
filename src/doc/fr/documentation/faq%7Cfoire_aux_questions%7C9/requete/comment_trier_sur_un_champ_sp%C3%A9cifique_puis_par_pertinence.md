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

Mais les documents avec la même valeur pour le champ `price` sont présentés sans ordre spécifique.

Une astuce est d'ajouter un second niveau de tri en utilisant le score de pertinence. `score` n'est pas un vrai champ du schéma mais c'est une information qui peut être utilisée au moment de la query pour trier les documents selon leur pertinence par rapport à la requête en cours :

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
