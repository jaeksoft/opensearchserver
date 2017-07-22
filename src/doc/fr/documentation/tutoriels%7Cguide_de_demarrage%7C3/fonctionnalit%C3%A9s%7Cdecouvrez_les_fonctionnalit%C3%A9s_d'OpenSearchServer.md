# Principes généraux

OpenSearchServer est un logiciel de moteur de recherche qui se déploie sur un serveur sous Windows, Linux ou Solaris.

Il dispose d'une **interface utilisateur** accessible avec un **navigateur Internet** supportant AJAX (Internet Explorer, Firefox, Safari, Chrome). Cette interface permet de piloter l'intégralité des fonctionnalités décrites ci-après.

Il dispose également d'un jeu **d'API** de type **REST** permettant l'intégration aisée dans toute application.

Des librairies clientes en PHP, en PERL et en ASP.NET facilitent l'intégration dans les environnements PHP et Microsoft.

Le module **Drupal** et le plugin **Wordpress** dédiés à OpenSearchServer permettent une intégration sans développement dans ces CMS. Ils peuvent ensuite être facilement personnalisés par un intégrateur ou développeur familier de ces environnements.

# Indexation
Pour procéder à l'indexation, OpenSearchServer dispose de plusieurs composants clés : les crawleurs, les parseurs, les analyseurs, les learners et les classifieurs.
-	Le rôle des crawleurs est d'aller rapatrier les données à indexer en fonction des différents protocoles.
-	Les parseurs, quant à eux, s'attachent à extraire les données à indexer (full-text) des documents rapatriés par les crawleurs.
-	Les analyseurs appliquent les règles sémantiques et linguistiques aux données indexées.
-	Les classifieurs permettent d'intégrer des informations tierces aux documents indexés.
- Les learners "apprennent" à partir des documents déjà indexés et catégorisent automatiquement les nouvelles données.

## Les crawlers
OpenSearchServer dispose de quatre types de crawleurs:
-	Le crawleur WEB
-	Le crawleur de système de fichiers
-	Le crawleur de base de données
-	Le crawleur par fichier XML

### Le crawleur WEB
Ce crawleur prend en charge les protocoles HTTP / HTTPS. Il a vocation à indexer les sites Internet, Intranet ou Extranet. Voici la liste des fonctionnalités:
-	**Liste des sites Internet** : Il s'agit d'une liste d'URL pouvant contenir des jokers (astérisque). Cela permet d'indexer intégralement ou partiellement un site Web.
-	**Liste d'exclusion** : Une liste d'URL intégrant des jokers, permet d'exclure des pages ou parties de sites du périmètre d'indexation.
-	**Le filtrage des paramètres d'URL** : Il est possible d'ignorer automatiquement les paramètres non discriminant dans une URL (par exemple, les paramètres de session).
-	**L'URL Browser** : Cette interface permet d'accéder à la liste intégrale des URLs connues et de contrôler leur statut (indexé ou non, erreur 404, etc.)
-	**Gestion du crawl** : Contrôle du processus d'indexation avec gestion du nombre de thread simultané, de la vitesse de crawl par site, du nombre de page par session, etc.
-	**Crawl manuel** : Il est possible de contrôler qu'une URL spécifique a été indexée.
-	**Proxy** : Utilisation d'un proxy dans le cadre du protocole HTTP ou HTTPS.
-	**Capture d'écran** : Capture d'écran des pages indexées.
-	**Authentification** : Accès aux sites Internet sécurisés nécessitant une authentification.
-	**Protocole robots.txt** : OpenSearchServer respecte les directives du protocole robots.txt.
-	**Extraction de données spécifiques** : Type MIME, URLs, etc.
 
![Crawler web](fonctionnalites/crawler_web.png)

### Le crawleur de système de fichiers
Ce crawleur a vocation à indexer les systèmes de fichiers locaux ou distants. Voici la liste des fonctionnalités:
-	Indexation des **fichiers locaux** : Parcours du système de fichier local, points de montages (NFS, CIFS).
-	Indexation des **fichiers distants** : Support des protocoles CIFS/SMB, FTP, FTPS.
-	Le **File Browser** : Cette interface permet de parcourir la liste des fichiers et répertoires parcourus et de prendre connaissance d'informations utiles (indexé ou non, taille fichiers, type de document, etc.).

![Crawler de système de fichiers - configuration d'un nouvel emplacement de crawl FTP](fonctionnalites/crawler_files.png)

### Le crawleur de base de données
Ce crawleur a vocation à indexer les données structurées issues des tables de base de données. Voici la liste des fonctionnalités:
-	Support de la plupart des bases de données (JDBC) : MySQL, SQLServer, PostgreSQL, Oracle, Sybase, DB2, etc.
-	Indexation par **requêtes SQL** avec gestion des jointures par reconnaissance des clés primaires.
-	Support de **l'indexation des pièces jointes** dont le chemin local ou l'URL sont indiqués dans le schéma.
-	**Suppression des tags** HTML avant indexation.
-	Conversion des **entités HTML**.
-	Possibilité d'appliquer des **expressions régulières** avant indexation.
 
![Crawler de base de données - configuration du processus de crawl](fonctionnalites/crawler_db.png)

### L'indexation par fichier XML
Il est possible d'entrer des données sous forme de fichier XML. OpenSearchServer dispose de son propre format XML natif. Ce format à la fois simple et puissant pourra être généré très aisément, par exemple, par export puis application d'une transformation XSLT.
Le format natif permet notamment:
-	L'indexation des données structurées en indiquant chaque champ individuellement.
-	L'indexation des **fichiers joints embarqués** dans le fichier XML (encodés au format base64)
-	L'indexation des **fichiers locaux** indiqués par un chemin.
-	L'indexation des **fichiers joints distants** indiqués par une URL.
-	Le retrait automatique des tags HTML.
-	La conversion des **entités HTML**.
-	L'application d'une **expression régulière** par champ avant indexation.

Exemple de fichier au format XML natif :

![Exemple de fichier XML](fonctionnalites/example_xml.png)

## Les parseurs
Le rôle d'un parseur est **d'extraire les données** à indexer des documents rapatriés par les crawleurs présentés dans le chapitre précédent. Le parseur est **sélectionné automatiquement** en fonction soit du type MIME (quand le crawleur le fournit) soit en utilisant l'extension présente dans le nom du fichier. Pour chaque parseur, il est possible de limiter la taille maximum des fichiers à indexer.

Voici la liste des formats supportés par défaut:
-	Microsoft Word: Extension .doc (Word 6, Word 95, Word 97-2007) et .docx
-	Microsoft Excel: Extension .xls (Excel 97-2007) et .xlsx
-	Microsoft PowerPoint: Extension .ppt (PowerPoint 97-2007) et plus récents .pptx.
-	HTML/XHTML
-	OpenOffice: Extensions .odt, ods, odf.
-	Adobe PDF.
-	Fichier RTF (Rich Text Format).
-	Fichiers Audio. Indexation des méta-data: Extension .wav, .mp3, .aif, .off
-	Les fichiers textes avec détection automatique du jeu de caractères: Extension .txt.

La solution OpenSearchServer permet l'intégration de nouveaux formats par l'intégration de plugins. La communauté a mis à disposition un certain nombre de plugins additionnels intégrés à la demande. De nouveaux plugins peuvent également être développés sur demande.

Quelques exemples de formats additionnels:
-	Torrent: Indexation des métadata.
-	Microsoft Publisher
-	Microsoft VISIO
-	Microsoft Outlook

![Liste partielle des parsers disponibles](fonctionnalites/parsers_list.png)

Chaque parseur fournit un certain nombre d'informations qui sont ensuite réparties dans l'index. Par exemple, les parseurs Microsoft Office, OpenOffice, et PDF fournissent séparément le titre du document, son auteur et son contenu. Le parseur HTML/XHTML identifiera individuellement les liens présents, le titre de la page, etc.

Un système simple de `Field mapping` (assignation de champs) permet d'enregistrer dans des champs spécifiques chaque valeur fournie par le parser.

Les valeurs fournies par le parser peuvent être **retravaillées par des expressions régulières**. Cette fonctionnalité puissante permet par exemple d'extraire des informations très précises depuis une page HTML.

Enfin il est possible d'assigner plusieurs valeurs à un même champ, ce qui aura pour effet de le **multivaluer**.

![Configuration du parser HTML](fonctionnalites/parsers_html.png)
![Field mapping dans le parser HTML](fonctionnalites/parsers_htmlmapping.png)

## Les analyseurs
Les analyseurs ont vocation à traiter les informations textuelles issues des parseurs. Ils vont appliquer un certain nombre de traitements sémantiques et linguistiques permettant une amélioration significative de la pertinence des recherches, par exemple en autorisant la recherche avec des orthographes approximatives. Il est possible de créer un nombre illimité d'analyseur qui pourront ensuite être activés sur des champs spécifiques de l'index.

![Analyseurs](fonctionnalites/analyzers.png)

Les analyseurs se composent d'un tokenizer, chargé de découper les textes en `tokens`, et d'une succession de filtres configurable, qui effectuent différents traitement sur les textes découpés.

Voici la description de quelques une de ces fonctionnalités.

### Lemmatisation
La lemmatisation consiste à identifier le radical des mots en retirant les désinences typiques de la langue spécifiée. Ainsi, le mot "consommation" sera également indexé en tant que "consomm" (avec un niveau de pertinence moindre). 
OpenSearchServer sait lemmatiser les mots issues de **17 langues** : allemand, anglais, arabe, chinois, danois, espagnol, finlandais, français, hollandais, hongrois, italien, norvégien, portugais, roumain, russe, suédois, turque.

### N-Gramme
Cela consiste à découper les mots en sous séquences de syllabes. Par exemple en utilisant des n-gramme de taille 2, le mot "consommation" sera également indexé en tant que: "co", "on", "ns", "so",  "om",  "mm", etc. Ainsi, nous pourrons reconnaître des mots mal orthographiés comme "conssomattion".

### Shingle
Cela consiste à regrouper plusieurs mots ensemble pour n'en faire qu'un seul. Ainsi, le sigle "I.B.M", ou "I B M", qu'il soit séparé par des points, des tirets, ou des espaces, sera également indexé "IBM".

### Signe diacritique
Ce filtre consiste à remplacer toutes les lettres accompagnées d'un signe (lettres accentuées) par son équivalent sans signe. Ainsi le mot "côté" deviendra "cote".

### Mots ignorés (stop words)
Ce filtre permet d'ignorer une liste de mots. Il s'applique sur des mots considérés comme peu intéressants dans le corpus indexé. La liste des mots interdits est paramétrable pour chaque langue.

### Synonymes
Ce filtre permet de trouver un document sur la base de son synonyme. La liste des synonymes est paramétrable. Ainsi la recherche du mot "formulaire" pourra également renvoyer des pages contenant "codex" ou "questionnaire".

### Sensibilité à la casse
Ce filtre permet de ne pas tenir compte des majuscules et minuscules. Le mot "Les" sera également indexé en "les".

### Remplacement par expressions régulières
Ce filtre permet d'appliquer un processus de "capture et remplace" sur un texte donnée en entrée. 
Il est ainsi facile de transformer une date au format JJ/MM/AAAA en une date au format AAAAMMJJ par exemple, en utilisant l'expression de capture `^([0-9]*)/([0-9]*)/([0-9]*)$` et le texte de remplacement `$3$2$1`.

### Autres filtres
De nombreux autres filtres sont disponibles, pour par exemple convertir des degrés en radians, formatter des nombres, dédupliquer des mots, extraire le nom de domaine d'une URL, normaliser des URL, rechercher des informations sur Youtube à partir d'une URL, etc.

## Les classifieurs
Ce module permet d'intégrer des données complémentaires à un document indexé. Le document indexé est soumis à une liste de requêtes. Chaque fois qu'une requête aboutie, **un mot clé déterminé est associé au document** dans un champ de son choix.
Fonctionnellement ce module est utilisé pour:
-	Classer automatiquement des documents indexés (rattachement à un groupe de site, à une catégorie).
-	Associer des mots-clés à des documents
-	Maintenir des liens sponsorisés

![Classifieur](fonctionnalites/classifiers2.png)

## Les learners
Ce module se compose de deux parties. La première partie concerne **l'apprentissage**, à partir d'un corpus de nombreux documents déjà catégorisés. Ce corpus peut par exemple prendre la forme d'une base de données d'articles rattachés chacun à une catégorie. 

La seconde partie du module est ensuite utilisée typiquement au moment de l'indexation d'un nouveau document : le document est analysé par le learner, confronté  à l'apprentissage réalisé précédemment et une classification est proposée. Cela pourrait par exemple consister en la proposition automatique d'une catégorie pour un nouvel article.

# Requêtes

Le module de requêtes permet la création d'un nombre illimité de modèles de requêtes. Chaque modèle de requêtes dispose de ses propres paramètres décrits ci-après. 

![Liste des requêtes configurées sur un index](fonctionnalites/queries_list.png)

## Deux types de requêtes

### Requête de type "Field"
Les requêtes de types "Search (field)" simplifient la définition des modèles de requête. Dans ce mode il suffit de choisir les différents champs du schéma dans lesquels la recherche full-text doit être effectuée et le **poids à accorder** à chaque champ dans le calcul de la pertinence du document.

Chaque champ peut être configuré de 4 manières :
- Pattern : aucun traitement n'est appliqué aux mots clés recherchés, la recherche se rapproche alors d'une requête de type "Pattern" décrite ci-après.
- Term : les mots clés saisis sont nettoyés afin de supprimer les caractères superflus (guillemets, signes, etc.) et la recherche s'effectue sur chacun des termes saisis. Par exemple la recherche de `lorem ipsum` consistera en la recherche de `lorem` puis en la recherche de `ipsum`.
- Phrase : les mots clés sont ici aussi nettoyés mais la recherche s'effectue en *mode "phrase"*.Par exemple la recherche de `lorem ipsum` se traduira par la recherche de `"lorem ipsum"`.
- Term & Phrase : ce mode combine les deux modes précédent. Ainsi une recherche de `lorep ipsum` recherchera indépendemment `lorem` puis `ipsum` et également la phrase `"lorem ipsum"`.

![Configuration d'une requête de type Search - fields](fonctionnalites/query_searched_fields.png)

### Requête de type "Pattern"
Dans le mode "Search (pattern)" c'est un langage de requête spécifique qui doit être utilisé afin d'indiquer quels champs seront à utiliser pour la recherche full-text.

La syntaxe de cette requête permet d'influer sur la pertinence de la recherche. En voici les éléments:
-	**Opérateur booléen** : AND OR NOT  +  - . Par exemple, la requête +cerfa -2031 signifie que l'on veut tous les documents qui contiennent le mot "cerfa", mais pas le mot "2031".
-	**Recherche de proximité** : L'utilisation des guillemets permet de chercher une phrase plutôt que des mots séparément. Il est possible d'autoriser une tolérance de distance entre les mots.
-	**Recherche par champs** : Il est possible de limiter la recherche à un ou plusieurs champs. Par exemple chercher uniquement dans le titre du document.
-	**Poids dans la recherche** : On peut promouvoir indépendamment les composants de la recherche pour promouvoir un champ ou un mot. Il est courant d'appliquer un poids plus élevé au titre du document.
-	**Fourchette de recherche** : Une recherche peut être circonscrite entre deux dates pivots, entre deux valeurs distinctes.

Voici un exemple de modèle de requête suivi de sa description:
```
title:($$)^10 OR title:("$$")^10 OR
url:($$)^5 OR url:("$$")^5  OR
content:($$) OR content:("$$")
```

-	Les caractères $$ seront remplacés par les mots clés recherchés par l'utilisateur final.
-	Le poids des mots clés trouvés dans le titre et l'URL est plus élevé.
-	Les mots clés seront recherchés indépendamment, mais également en tant que phrase (recherche de proximité).

## Facettes et filtres

Ces deux fonctions sont présentées ensemble car elles fonctionnent souvent de concert.

### Facettes
Il s'agit du **comptage du nombre de résultats par entité**. Les entités peuvent être des catégories, des noms de domaine, des nombres de liens, des dates, etc. Plus globalement, la facette peut s'appliquer sur toute valeur indexée dans un champ.

On pourra ainsi indiquer dans la page de résultats le nombre de résultats par site Internet, ou le nombre par concept, ou par période de temps, etc.
Les facettes peuvent être calculées avant ou après regroupements (décrits ci-après).

![Facettes](fonctionnalites/moellon.png)
![Définition des facettes](fonctionnalites/facets.png)

### Filtres
Cette fonction est souvent le corollaire des facettes. On peut filtrer une recherche en utilisant une sous-requête exprimée dans le langage de requêtes décrit plus haut.

Par exemple, on peut donc filtrer une recherche sur un nom de domaine, sur une fourchette de temps, etc.

![Configuration d'un filtre de date relatif](fonctionnalites/query_filter_date_relative.png)

## Regroupements

Les documents peuvent être regroupés pour favoriser la diversité et la lisibilité des documents présentés dans le résultat de recherche.
On pourra par exemple ne présenter que 3 résultats consécutifs appartenant au même site Internet.

Les différents types de regroupements possibles sont:
-	**Regroupement consécutif optimisé** : Limite le nombre de résultats consécutifs, appliqués au nombre de résultats affichés (optimisé pour les gros volumes de donnée).
-	**Regroupement consécutif complet** : Limite le nombre de résultats consécutifs appliqués à l'ensemble des résultats (et pas seulement à ceux de la page affichée).
-	**Regroupement simple** : Limite de nombre de résultats consécutifs ou non sur l'ensemble des résultats.

## Extraits de texte

Pour construire le résultat de recherche, OpenSearchServer peut soit restituer le champ indexé tel quel, soit construire un extrait de texte intelligent.

L'extrait de texte répond aux exigences suivantes:
-	**Nombre de caractères maximum** : l'extrait de texte ne peut pas dépasser le nombre de caractère indiqué.
-	**Mots recherchés en surbrillance** : L'extrait de texte contient les mots clés recherchés mis en évidence grâce à un tag paramétrable.
-	**Pertinence de l'extrait** : Le moteur s'attache à présenter la phrase la plus pertinente par rapport aux mots recherchés, la plus proche possible de la taille souhaitée.
-	**Détection de phrase** : Le moteur s'applique à présenter un extrait de texte correspondant à un début de phrase (quand c'est possible).

![Définition des snippets lors de la création d'une requête](fonctionnalites/snippets.png)
![Exemple de résultats de recherche présentés dans l'interface, avec des extraits de texte](fonctionnalites/snippets_2.png)

## Ordonnancement

Dans un moteur de recherche, l'ordonnancement par défaut correspond au score généré par l'algorithme de recherche (Vector Space Model). Nous avons vu qu'il est possible d'influer ce score en utilisant le langage de requête.

Il est également possible d'ordonnancer sur la base d'un champ indexé en particulier (comme sur une base de données). Par exemple, on pourra ainsi classer par date décroissante.
On peut enfin mixer score et champs. Ainsi, à score égal, on pourra décider d'afficher les documents les plus récents en premier.

## Géolocalisation

A partir du moment où le schéma de l'index est prévu pour stocker des informations de localisation (latitude et longitude) il est facile de configurer des **filtres de géolocalisation** dans les requêtes envoyées au moteur.

Seuls les documents dont les coordonnées sont comprises dans le rectangle exprimé dans la requête seront retournés. La distance entre chaque document et le point central sera également calculée et retournée.

Les coordonnées peuvent être exprimées en degrés ou en radians et les distances en kilomètres ou en miles.

## Sous-requête de promotion

Les sous-requêtes de promotion permettent d'influer sur le score des documents retournés et d'impacter ainsi la pertinence en fonction de paramètres métiers autres que la recherche full-text.
 
![Configuration des sous-requêtes de promotions lors de la création d'une requête de recherche](fonctionnalites/boosting.png)

On peut simplement utiliser une sous-requête pour promouvoir ou dégrader des documents. Ainsi on pourra promouvoir les résultats d'un site Internet par rapport aux autres ou dégrader les documents appartenant à une catégorie.
Il est possible de configurer plusieurs sous-requêtes pour mixer les règles de pertinence.

## Jointures

Une requête de recherche peut effectuer une jointure sur un ou plusieurs autres index afin de retourner plus d'informations. Les jointures se configurent finement et il est possible de filtrer ou trier précisément les données à joindre au moment de la requête.

## Documents similaires

Cette fonction permet de retourner des documents similaires au document indiqué en référence. La similarité est basée sur la présence d'un corpus de mots ou de concept en commun.

Ce module offre un certain nombre de paramétrages fins:
-	Taille minimum et maximum des mots pris en compte.
-	Utilisation d'une liste de mots interdits.
-	Fréquence minimum des mots pris en compte.


## Correction orthographique

Le module de correction orthographique permet de suggérer des orthographes alternatives. En fonction du corpus documentaire et du résultat recherché, plusieurs algorithmes sont mis à disposition:
-	Distance de Jaro Winkler
-	Distance de Levenshtein
-	Distance N-Gramme

## Extraction d'entités nommées
Un type de requête spécifique permet de configurer facilement une extraction d'entités nommées à partir d'un index contenant les entités. Cette requête peut ensuite facilement être utilisée dans un autre index à l'aide d'un filtre d'analyseur.

Il est ainsi relativement aisé de mettre en place un système d'extraction automatique d'entités nommées à appliqué à des textes indexés. On peut par exemple imaginer 2 index, l'un stockant des entités nommées (par exemple des noms de villes ou de personnes) et l'autre stockant des articles de journal. Le deuxième index utiliserait, pour chaque nouvel article indexé, une requête d'extraction d'entités nommées configurée dans le premier index afin d'enregistrer dans un champ spécifique du schéma la liste des villes ou des personnes mentionnées dans le texte de l'article.

## Auto-complétion
L'auto-complétion se configure facilement en quelques clics. Il est possible de choisir les champs à partir desquels proposer des suggestions de recherche.

L'analyseur d'auto-complétion pré-configuré permet d'appliquer un traitement optimal aux données indexées pour offrir les suggestions les plus pertinentes.

Plusieurs index d'auto-complétion peuvent même être configurés, par exemple pour proposer aux utilisateurs à différents endroits des suggestions issues d'un même index mais de champs différents.

![Auto-complétion](fonctionnalites/autocompletion.png)

## Page de recherche
Le module Renderer intégré dans OpenSearchServer permet d'accéder facilement et très rapidement à **une page de recherche complète**. La page peut-être personnalisée facilement via l'écriture de feuilles de styles (CSS). Les informations à afficher pour chaque résultat peuvent être choisies précisément. Il est possible d'intégrer l'affichage d'images ou de liens.

Le code prêt à l'emploi permet d'embarquer facilement la page de recherche dans une autre application via l'utilisation d'une iFrame.

![Renderer](fonctionnalites/renderer.png)

## Authentification
Il est possible d'activer l'authentification sur les résultats de recherche. Ce module permet de filtrer les documents retournés par chaque recherche selon le profil des utilisateurs (username / groups), en confrontant ces informations aux valeurs enregistrées dans des champs spécifiques du schéma lors de l'indexation des documents. 

Le module Renderer décrit ci-dessus intègre cette fonctionnalité et propose un formulaire de connexion automatiquement.

# Outils annexes

Les outils décrits dans ce chapitre participent à l'industrialisation des processus. Ils permettent la supervision du moteur de recherche, l'automatisation des tâches, la production des statistiques d'utilisation.

## Automatisation

Le module Scheduler permet d'automatiser des processus complexes. Il est couramment utilisé pour mettre en œuvre la stratégie d'indexation, assurer la réplication des index sur plusieurs serveurs. **Les tâches peuvent être planifiées** pour une exécution régulière à heure fixe (via une syntaxe `crontab`). 
 
Voici la liste des tâches susceptibles d'être déclenchées par ce module:
-	Lancement d'un crawl de base de données,
-	Lancement d'un crawl XML natif,
-	Démarrage/arrêt d'un crawl WEB,
-	Démarrage/arrêt d'un crawl de système de fichier,
-	Optimisation d'un index,
-	Réplication d'un index,
-	Exécution d'une requête de suppression,
-	Chargement d'un journal d'activité
- Suppression totale des données de l’index
- Récupération d’un fichier XML sur serveur FTP et retraitement par XSL pour indexation
- Fusion d'index
- **Exécution d'une tâche sur un autre index** (local à l'instance ou distant)
- Récupération de documents d’un autre index (local ou distant) et retraitement pour indexation 
- Ré indexation des termes des index d’auto-complétion

![Création d'une tâche programmée](fonctionnalites/scheduler2.png)

## Réplication et sauvegarde

Le module de réplication prend en charge la copie de l'index sur le même serveur ou sur un serveur distant. La réplication peut être déclenchée manuellement ou automatiquement par le module Scheduler.
 
La réplication est habituellement utilisée dans deux cas de figure:
-	**La distribution** d'un index sur une grappe de serveurs. Les index sont mis à jour sur les serveurs de destination sans arrêt de service.
-	**La sauvegarde** d'un index localement ou sur sur un serveur distant.

![Réplication](fonctionnalites/replication.png)

## Supervision

Le module de supervision met à disposition des données utiles au contrôle du bon fonctionnement du moteur de recherche comme l'occupation mémoire, l'espace disque disponible. Ces données sont visibles dans l'interface utilisateur. Une API XML/HTTP peut être interrogé pour intégrer un service d'alerte dans un outil de supervision standard.
 
![Supervision](fonctionnalites/runtime_system.png)

## Statistiques

L'outil de statistiques est composé de deux éléments. Le journal de logs quotidien et l'interface de visualisation.
Journal de logs quotidien

Le journal de logs quotidien contient pour chaque recherche les informations suivantes:
-	La date et l'heure
-	Les mots clés recherchés
-	Le nombre de documents trouvés
 
En conjonction avec le front-office, on peut bénéficier d'informations complémentaires comme:
-	L'identification de session
-	L'adresse IP distante
-	Le nom d'utilisateur
Ces fichiers de logs peuvent être importés dans des outils de gestion des statistiques.

### Interface de visualisation
 
L'interface de visualisation permet d'interroger les statistiques avec les fonctionnalités suivantes:
-	Quels sont les mots-clés les plus fréquents.
-	Quelles sont les recherches ne renvoyant aucun résultat.
-	Limiter la recherche dans une fenêtre de temps.
-	Exporter le rapport au format CSV.
