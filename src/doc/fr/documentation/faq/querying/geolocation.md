OpenSearchServer offre la possibilité de réaliser des requêtes géolocalisées. A partir du moment où les coordonnnées (longitude et latitude) ont été enregistrées avec les documents indexés il est possible de requêter les documents se trouvant dans un rectangle géographique donné.

## Indexer les documents en intégrant les informations de géolocalisation
### Préparer le schéma

Il faut commencer par fournir la latitude et la longitude de chaque document.

<<<<<<< HEAD:src/doc/fr/documentation/faq/querying/geolocation.md
<<<<<<< HEAD:src/doc/fr/documentation/faq/querying/geolocation.md
Le sch�ma doit comporter deux champs pour stocker cette information:
- latitude (index�)
- longitude (index�)

=======
Le sch�ma doit comporter deux champs pour stocker cette information :
- latitude (index�)
- longitude (index�)
 
>>>>>>> 4361500... Change on french documentation:src/doc/fr/documentation/faq/requete/geolocalisation.md
L'analyzer correspondant au [syst�me de coordonn�es](http://fr.wikipedia.org/wiki/Coordonn%C3%A9es_g%C3%A9ographiques) appropri� doit �tre choisi:
=======
Le schéma doit comporter deux champs pour stocker cette information :
- latitude (indexé)
- longitude (indexé)
 
L'analyzer correspondant au [système de coordonnées](http://fr.wikipedia.org/wiki/Coordonn%C3%A9es_g%C3%A9ographiques) approprié doit être choisi:
>>>>>>> 1b83dd6... Change on french documentation:src/doc/fr/documentation/faq/requete/geolocalisation.md

- GeoRadianAnalyzer
- GeoDegreesAnalyzer

![Alt text](geo_fields.png)

### Indexer les données

Les coordonnées (latitude et longitude) doivent être exprimés au format décimal. Par exemple :

- Pour des degrés: -52.090904
- Pour des radians: -0.675757575575

#### Utiliser l'API : ajouter deux champs au JSON

L'API [d'indexation JSON](https://github.com/jaeksoft/opensearchserver/wiki/Document-put-JSON) est documentée sur notre [wiki des API](https://github.com/jaeksoft/opensearchserver/wiki/).

    [
      {
        "lang": "ENGLISH",
        "fields": [
          { "name": "city", "value": "New-York" },
          { "name": "latitude", "value": 40.7142700 },
          { "name": "longitude", "value": -74.0059700 }
        ]
      },
      {
        "lang": "FRENCH",
        "fields": [
          { "name": "city", "value": "Paris" },
          { "name": "latitude", "value": 48.8534100 },
          { "name": "longitude", "value": 2.3488000 }
         ]
       },
       {
        "lang": "GERMAN",
        "fields": [
          { "name": "city", "value": "Berlin" },
          { "name": "latitude", "value": 52.5243700 },
          { "name": "longitude", "value": 13.4105300 }
         ]
       }
    ] 

### Requêter les données

Recherchons par exemple les villes situées à moins de 10 kilomètres d'un point donné (appelé "la position centrale").

Les champs latitudeField et longitudeField doivent être mappés aux champs du schéma contenant ces informations.

Le tableau "geo" permet de définir la position centrale.

Le filtre "GeoFilter" applique un filtre géographique selon les paramètres donnés.

La requête est réalisée grâce à [l'API Search(field)](https://github.com/jaeksoft/opensearchserver/wiki/Search-field) décrite sur [notre wiki](https://github.com/jaeksoft/opensearchserver/wiki/) :

    {
        "start": 0,
        "rows": 10,
        "geo": {
            "latitudeField": "latitude",
            "longitudeField": "longitude",
            "latitude": 48.85341,
            "longitude": 2.3488,
            "coordUnit": "DEGREES"
        },
        "emptyReturnsAll": true,
        "filters": [
            {
                "type": "GeoFilter",
                "shape": "SQUARED",
                "negative": false,
                "unit": "KILOMETERS",
                "distance": 10
            }
        ],
        "returnedFields": [ "city", "latitude", "longitude" ]
    }

### Documents retournés

Voici les résultats :

    {
    "successful": true,
    "documents": [
        {
            "pos": 0,
            "score": 1,
            "collapseCount": 0,
            "fields": [
                {
                    "fieldName": "city",
                    "values": [
                        "Paris"
                    ]
                },
                {
                    "fieldName": "latitude",
                    "values": [
                        "P0.8526529"
                    ]
                },
                {
                    "fieldName": "longitude",
                    "values": [
                        "P0.0409943"
                    ]
                }
            ]
        }
    ],
    "facets": [],
    "query": "*:*",
    "rows": 10,
    "start": 0,
    "numFound": 1,
    "time": 126,
    "collapsedDocCount": 0,
    "maxScore": 1
    }

### Ajouter la distance dans les résultats

Pour retourner la distance entre la position centrale et chacun des résultats il est nécessaire d'ajouter un tableau "scorings" dans la requête :

    {
        "start": 0,
        "rows": 10,
        "geo": {
            "latitudeField": "latitude",
            "longitudeField": "longitude",
            "latitude": 48.85341,
            "longitude": 2.3488,
            "coordUnit": "DEGREES"
        },
        "emptyReturnsAll": true,
        "filters": [
            {
                "type": "GeoFilter",
                "shape": "SQUARED",
                "negative": false,
                "unit": "KILOMETERS",
                "distance": 10
            }
        ],
        "returnedFields": [ "city", "latitude", "longitude" ],
        "scorings": [
            {
                "ascending": false,
                "weight": 1,
                "type": "DISTANCE"
            }
        ]
    }


Voici les résultats :

    {
    "successful": true,
    "documents": [
        {
            "pos": 0,
            "score": 1,
            "distance": 0.00033325536,
            "collapseCount": 0,
            "fields": [
                {
                    "fieldName": "city",
                    "values": [
                        "Paris"
                    ]
                },
                {
                    "fieldName": "latitude",
                    "values": [
                        "P0.8526529"
                    ]
                },
                {
                    "fieldName": "longitude",
                    "values": [
                        "P0.0409943"
                    ]
                }
            ]
        }
    ],
    "facets": [],
    "query": "*:*",
    "rows": 10,
    "start": 0,
    "numFound": 1,
    "time": 0,
    "collapsedDocCount": 0,
    "maxScore": 0
    }