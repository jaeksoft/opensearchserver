Installer OpenSearchServer sur un Mac est très simple. La version courte ci-dessous est habituellement suffisante ; en cas de souci utilisez la version détaillée.

## Version courte

- Assurez-vous d'avoir une Java Virtual Machine dans sa version 6 ou plus récente
- Téléchargez notre dernière version stable sur [tar.gz](http://www.open-search-server.com/download/  "Download")
- Décompressez-la, puis lancez le start.sh qui se trouve dans le dossier OSS
- Via votre navigateur préféré, ouvrez l'URL [http://votreserveur:9090](http://votreserveurr:9090) (remplacez **yourserver** par **localhost** si OSS tourne sur votre machine locale)
- Bonne découverte d'OpenSearchServer !

## Version détaillée
Nous allons maintenant refaire les étapes de la version courte, avec davantage d'explications.

### Vérifier la version de Java ###

Lancez un terminal et tapez :

    java -version

- Si vous avez une version inférieure à 6, il faut la mettre à jour
- Si aucune JVM n'est installée, il faut procéder à une installation (voir plus loin)

### Télécharger OSS

Nous recommendons vivement de toujours utiliser la dernière version d'OSS disponible sur [SourceForge](http://www.open-search-server.com/download/ "Download").

Téléchargez le package **tar.gz** pour Linux/BSD, puis décompressez-le comme suit :

    tar -xzvf open-search-server-1.5.tar.gz
    
ou

	unzip open-search-server-1.5.zip
    
La résultante est un dossier **opensearchserver**. Tous les binaires se trouvent dans ce dossier, et vos données les y rejoindront.

### Lancement
Pour l'instant, il reste nécessaire d'utiliser une ligne de commande pour lancer OSS sous Mac. Pour ce faire, il faut utiliser l'application Terminal qui se trouve dans le dossier Utilitaires - qui se trouve lui-même dans le dossier Applications de votre Mac.

Dans le Terminal, rendez-vous dans votre dossier OpenSearchServer fraîchement installé, puis lancez l'application Start en tapant **./start.sh**. Et voilà !

#### Si vous ne connaissez rien au Terminal
Si vous n'avez jamais utilisé le Terminal de votre Mac, il est possible que vous ne sachiez pas comment vous rendre dans le dossier OpenSearch Server. Heureusement, il y a juste deux commandes Unix de base à connaître :

- si vous tapez ls (lettre L puis lettre S, formant l'abbréviation du mot "liste"), le Terminal affiche tous les fichiers et dossiers du dossier où vous vous trouvez afin que vous puissiez vous orienter.
- si vous tapez cd (abbréviation de "change directory"), le Terminall rentre dans le dossier indiqué. Par exemple cd downloads rentre dans le dossier downloads qui se trouve dans le dossier actuellement ouvert. Vous pouvez ensuite refaire un ls pour voir son contenu.
- cd suivi de deux points (cd ..) remonte d'un niveau dans l'arborescence.

Naviguez ainsi jusqu'à trouver le dossier OpenSearchServer, tapez **cd opensearchserver**, et faîtes un ls pour voir le fichier **start.sh** que nous recherchons.
