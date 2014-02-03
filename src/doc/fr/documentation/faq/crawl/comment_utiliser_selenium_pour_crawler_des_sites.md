**OpenSearchServer 1.5** introduit une nouvelle et puissante fonctionnalité de scripting. Il est désormais possible de piloter un navigateur Internet (Firefox, PhantomJS) à l'aide d'un script.  

La nouvelle fonctionnalité est capable de :

* Ouvrir un navigateur internet,
* Exécuter du code Javascript,
* Extraire des données de la page via des requêtes XPATH des sélecteurs CSS ou l'attribut `id` des éléments, 
* Insérer un document dans un index.

Un ensemble d'API REST a été créé pour configurer ces scripts. Chaque script peut être exécuté/stocké/mis à jour/supprimé en l'appelant par son nom.

Cette nouvelle fonctionnalité sera particulièrement puissante pour crawler les sites internet protégés par des formulaire de login/mot de passe, les sites internet qui utilisent beaucoup de Javascript pour afficher les contenus ou encore quand les valeurs qui doivent être indexées ne sont atteignables que via des sélecteur CSS ou XPATH.

## Script JSON

Chaque commande est une structure JSON avec une valeur `command` et un tableau de paramètres.

    {
      "command": "WEBDRIVER_OPEN",
      "parameters": [ "FIREFOX"]
    }
  
Un script est un tableau de structure JSON `command`. Voici un exemple de script :

    [
     { "command": "WEBDRIVER_OPEN", "parameters": [ "PHANTOMJS"] },
     { "command": "WEBDRIVER_SET_TIMEOUTS", "parameters": [ 60, 60 ] },
     { "command": "WEBDRIVER_RESIZE", "parameters": [ 1024, 768 ] },
     { "command": "WEBDRIVER_GET", "parameters": [ "{url}" ] },
     { "command": "SLEEP", "parameters": [ 1 ] },
     { "command": "INDEX_DOCUMENT_NEW", "parameters": [ "english" ] },
     { "command": "INDEX_DOCUMENT_ADD_VALUE", "parameters": [ "url", "{url}" ] },
     { "command": "INDEX_DOCUMENT_ADD_VALUE", "parameters": [ "title", "Test" ] },
     { "command": "CSS_SELECTOR_INDEX_FIELD", "parameters": [ "content", "div ul li a" ] },
     { "command": "INDEX_DOCUMENT_UPDATE" }
    ]
  
Ce script réalise les actions suivantes :

* ouvre une fenêtre PHANTOMKS
* configure un timeout de 1 minute
* redimensionne la fenêtre à 1024x768 pixels,
* charge une page web dont l'url est donnée dans le paramètre `{url}`,
* attend une seconde,
* crée un document,
* donne une valeur aux champs `url` et `title` du document,
* donne une valeur au champ `content` via l'utilisation d'un sélecteur CSS permettant d'extraire la valeur d'un élément de la page,
* insère le document dans l'index.

## API RESTful

Un ensemble d'API RESTful est disponible.

### Sauvegarder le script

Pour enregistrer le script utilisez l'appel suivant :

* URL: http://localhost:9090/services/rest/index/script/script/{script_name}
* HTTP Method: PUT
* HTTP Header: Content-Type: application/json
* Payload: Le script JSON

### Exécuter le script

Pour exécuter le script utilisez l'appel suivant :

* URL: http://localhost:9090/services/rest/index/script/script/{script_name}/run
* HTTP Method: POST
* HTTP Header: Content-Type: application/json
* Payload: La structure JSON contenant les variables
* Les variables se passent de la manière suivante :

    {
        "url": "http://www.open-search-server.com",
        "name": "John Doe"
    }
  
###Subscript###

Un script peut appeler un "subscript" pour chaque élément trouvé par un sélecteur.

   
    [
     { "command": "WEBDRIVER_OPEN", "parameters": [ "PHANTOMJS" ] },
     { "command": "WEBDRIVER_SET_TIMEOUTS", "parameters": [ 60, 60 ] },
     { "command": "WEBDRIVER_RESIZE", "parameters": [ 1024, 768 ] },
     { "command": "WEBDRIVER_GET", "parameters": [ "http://www.dmoz.org/" ] },
     { "command": "SLEEP", "parameters": [ 3 ] },
     { "command": "CSS_SELECTOR_SUBSCRIPT", "parameters": [ "dmoz_sub", "div#catalogs span a" ] },
     { "command": "WEBDRIVER_CLOSE" }
    ]
  
Ce script extrait toutes les catégories racines de la page d'accueil du site Dmoz, et appelle pour chacune des catégories le subscript `dmoz_sub`.

## Liste complète des commandes

* `WEBDRIVER_OPEN`: ouvre un navigateur web
  * **parameter 1**: nom du driver (du navigateur). Valeurs possibles: `PHANTOMJS`, `FIREFOX`.
* `WEBDRIVER_CLOSE`: ferme le navigateur
* `WEBDRIVER_NEW_WINDOW`: ouvre une nouvelle fenêtre et garde la session utilisateur active
* `WEBDRIVER_CLOSE_WINDOW`: ferme la fenêtre courante
* `WEBDRIVER_SET_TIMEOUTS`: défini un délai de timeout après lequel l'exécution du script est stoppée
  * **parameter 1**: délai, en secondes
* `WEBDRIVER_RESIZE`: redimensionne la fenêtre
  * **parameter 1**: largeur
  * **parameter 2**: hauteur
* `WEBDRIVER_GET`: charge une URL
  * **parameter 1**: URL. Des variables peuvent être utilisées dans ce paramètre.
* `SLEEP` : met en pause l'exécution du script
  * **parameter 1**: durée de la pause, en secondes
* `CSS_SELECTOR_SUBSCRIPT`, `XPATH_SELECTOR_SUBSCRIPT`: sélectionne un élément et exécute un subscript. L'élément sélectionné doit être un `<a` ou un `<img`. L'attribut `href` ou `src` sera passé dans la variable `{url}` du subscript.
  * **parameter 1**: nom du subscript  
  * **parameter 2**: selecteur, CSS ou XPATH selon la commande.
* `WEBDRIVER_JAVASCRIPT`: exécute du code Javascript
  * **parameter 1**: code Javascript à exécuter. Des variables peuvent être utilisées dans ce paramètre.
* `SCRIPT`: appelle un autre script
  * **parameter 1**: nom du script
* `CSS_SELECTOR_CLICK_AND_SCRIPT`, `XPATH_SELECTOR_CLICK_AND_SCRIPT`: sélectionne un élément, effectue un clic dessus, attend un moment et exécute un script
  * **parameter 1**: selecteur, CSS or XPATH selon la commande.
  * **parameter 2**: nom du script
  * **parameter 3**: temps d'attente entre le clic sur l'élément et l'éxecution du script
* `INDEX_DOCUMENT_NEW`: crée un nouveau document à indexer
  * **parameter 1**: langue du nouveau document
* `INDEX_DOCUMENT_ADD_VALUE`: ajoute une valeur à l'un des champs du nouveau document
  * **parameter 1**: champ
  * **parameter 2**: valeur. Des variables peuvent être utilisées dans ce paramètre.
* `INDEX_DOCUMENT_UPDATE`: enregistre le nouveau document dans l'index
* `VAR_NEW_REGEX` : crée une nouvelle variable en appliquant une expression régulière sur une autre variable.
  * **parameter 1**: variable sur laquelle appliquer l'expression régulière
  * **parameter 2**: expression régulière. Le groupe de capture (utilisation des parenthèses) donnera sa valeur à la nouvelle variable.
  * **parameter 3**: nom de la nouvelle variable
* `CSS_SELECTOR_DOWNLOAD`, `XPATH_SELECTOR_DOWNLOAD` : sélectionne un élément et télécharge le document pointé par son attribut `href`. L'élément sélectionné doit être un `<a`.
  * **parameter 1**: dossier dans lequel télécharger le document. Le dossier sera créé récurisvement s'il n'existe pas. Des variables peuvent être utilisées dans ce paramètre.
  * **parameter 2**: selecteur, CSS ou XPATH selon la commande.
* `WEBDRIVER_DOWNLOAD`: télécharge la page en cours, indiquée par la valeur de la variable `{url}` courante.
  * **parameter 1**: dossier dans lequel télécharger le document. Le dossier sera créé récurisvement s'il n'existe pas. Des variables peuvent être utilisées dans ce paramètre.
* `PARSER_MERGE`: fusionne tous les documents PDF d'un document en un nouveau document PDF.
  * **parameter 1**: nom du parser PDF. Actuellement seule la valeur "PDF parser" peut être utilisée.
  * **parameter 2**: dossier contenant fichiers PDF à fusionner.
  * **parameter 3**: chemin complet (avec le nom de fichier) du PDF à créer.
* `SEARCH_TEMPLATE_JSON`: exécute une requête de recherche sur l'index et exécute une action en fonction du résultat.
  * **parameter 1**: nom du template de query à utiliser.
  * **parameter 2**: mots clés à utiliser pour la requête de recherche.
  * **parameter 3**: "JSON path" permettant de matcher un champ particulier du JSON renvoyé en résultat.
  * **parameter 4**: action. Les valeurs peuvent être :
    * `EXIT_IF_NOT_FOUND`: quitte le script actuel si le "JSON path" ne renvoie rien.
    * `IF_FOUND`: si le "JSON path" renvoie une valeur :
      * **parameter 5**: doit être `NEXT_COMMAND`
      * **parameter 6**: prochaine `command` à exécuter. On utilisera souvent `WEBDRIVER_CLOSE_WINDOW`.
