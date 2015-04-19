Cette page est le document de référence pour installer OpenSearchServer sur un système Linux (Ubuntu, CentOS, Xandros, etc.).

La version courte ci-dessous est habituellement suffisante ; en cas de souci utilisez la version détaillée.

## Version courte

- Assurez-vous d'avoir une Java Virtual Machine dans sa version 6 ou plus récente
- Téléchargez notre dernière version stable sur [tar.gz](http://www.open-search-server.com/download/  "Download")
- Décompressez-la, puis lancez le start.sh qui se trouve dans le dossier OSS
- Via votre navigateur préféré, ouvrez l'URL [http://votreserveur:9090](http://votreserveurr:9090) (remplacez **yourserver** par **localhost** si OSS tourne sur votre machine locale)
- Bonne découverte d'OpenSearchServer !

## Version détaillée

Installez une nouvelle instance Java - une JVM. Des JVMs connues sont :

- OpenJDK (http://openjdk.java.net/)
- Oracle/SUN Java (http://www.java.com/en/download/)
- IBM Java (https://www.ibm.com/developerworks/java/jdk/")

Nous allons maintenant refaire les étapes de la version courte, avec davantage d'explications.

### Vérifier la version de Java ###

Lancez un terminal et tapez :

    java -version

- Si vous avez une version inférieure à 6, il faut la mettre à jour
- Si aucune JVM n'est installée, il faut procéder à une installation

### Installer Java

En tant qu'acteurs open source, nous recommandons la dernière version d'OpenJDK (soit la 1.7.0 lors de l'écriture de cet article).

Pour ce faire, suivez juste les instructions sur [le site openJDK](http://openjdk.java.net/install)

### Télécharger OpenSearchServer

Nous recommendons vivement de toujours utiliser la dernière version d'OSS disponible sur [SourceForge](http://www.open-search-server.com/download/ "Download").

Téléchargez le package **tar.gz** pour Linux/BSD, puis décompressez-le comme suit :

    tar -xzvf open-search-server-1.5.tar.gz
    
La résultate est un dossier **opensearchserver**. Tous les binaires se trouvent dans ce dossier, et vos données les y rejoindront.

### Lancement

Il suffit de lancer le start.sh dans le dossier OSS, comme suit :

    cd open-search-server
    ./start.sh
    
Pour accéder à votre back office OSS, lancez un navigateur web (Firefox, Chromium, Opera, Safari...) à la page [http://votreserveur:9090](http://votreserveur:9090)

Si tout c'est bien passé, vous voyez maintenant l'interface OSS. Si ce n'est pas le cas et que vous êtes certain d'avoir bien suivi la procédure, voir la section débuggage.

Une fois l'interface disponible, vous pouvez passer à l'étape suivante pour créer votre premier index.

## Debuggage

### Le port 9090 est-il libre ?

Si il se trouve qu'un autre serveur utilise ce port, il va falloir indiquer un nouveau port dans les fichiers de configuration Catalina.

Première étape, vérifier si le port 9090 est bien occupé :

    netstat -an | grep :9090
   
Si la réponse ressemble à ceci, alors ce port est déjà pris :

    TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       676
    
Si c'est le cas, le plus simple est d'attribuer un nouveau port à OSS, comme suit :

Dans le dossier OSS, ouvrez le fichier **start.sh**
Trouvez la ligne qui commence par :

    SERVER_PORT=9090

Et changez ce passage en :

    SERVER_PORT=9091

Sauvegardez le fichier et lancez OpenSearchServer en lançant **start.sh**

Votre back office OpenSearch Server est maintenant joignable sur l'URL [http://votreserveur:9091](http://votreserveur:9091)
