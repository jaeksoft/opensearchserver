## Version courte

- Assurez-vous d'avoir JAVA 6 ou plus récent
- Téléchargez la dernière version stable [zip](http://www.open-search-server.com/download/ "Download")
- Décompressez-la et lancez **start.bat**, qui se trouve dans le dossier OpenSearchServer
- Via un navigateur web ouvrez l'URL [http://votreserveur:9090](http://votreserveur:9090) (votre serveur étant **localhost** si jamais vous exécutez OSS en local)
- Bonne découverte d'OpenSearchServer !

## Version détaillée

Nous allons maintenant refaire les étapes de la version courte, avec davantage d'explications.

### Vérifier sa version de Java

- Allez sur cette [page](http://www.java.com/en/download/installed.jsp) pour savoir quelle version de Java tourne sur votre machine
- Si ce n'est pas une version 6 ou plus récente, il faut faire une mise à jour
- Si la page web indique que vous n'avez pas d'instance Java, il faut en installer une

Dans les deux cas, la page vous propose un bouton pour télécharger gratuitement Java. Cliquez dessus et suivez les instructions.

### Télécharger OpenSearchServer

Nous recommendons vivement de toujours utiliser la dernière version d'OSS disponible sur [SourceForge](http://www.open-search-server.com/download/ "Download").

Téléchargez le package **ZIP** pour Windows et dézipez le sur votre disque.

Dans le dossier dézipé se trouve un dossier **opensearchserver**. Tous les binaires se trouvent dans ce dossier, et vos données les y rejoindront.

### Lancement

Lancez le fichier **start.bat** qui se trouve directement dans le dossier OpenSearchServer.

Pour accéder à votre back office OSS, lancez un navigateur web (Firefox, Chromium, Opera, Safari...) à la page [http://votreserveur:9090](http://votreserveur:9090)

Si tout c'est bien passé, vous voyez maintenant l'interface OSS. Si ce n'est pas le cas et que vous êtes certain d'avoir bien suivi la procédure, voir la section débuggage.

## Débuggage

Si tout c'est bien passé cette partie vous est inutile. Dans le cas contraire nous allons voir les causes possibles.

### Vérifier les variables d'environement

- Clic droit sur **Ordinateur**
- Choisir **Propriétés** dans le menu
- Onglet **Avancé** - ou page **Paramètres système avancés**
- Bouton **Variables d'environnement...**

La fenêtre suivante présente une liste de variables. Celle qui nous intéresse s'appelle soit **JRE_HOME** soit **JAVA_HOME**, selon le type d'instance Java sur cet ordinateur.

Si la bonne variable est présente, vérifiez qu'elle cible le bon dossier

Si elle n'est pas présente, il faut la créer comme suit :

- Si vous avez une JRE, créez ou mettez à jour la variable **JRE_HOME**. Dirigez-la vers le dossier où se trouve votre JRE Java, par exemple **C:\Program Files\Java\jre1.7.0_51**
- Si vous avez un JDK, créez ou mettez à jour la variable **JAVA_HOME**. Dirigez-la vers le dossier où se trouve votre JDK, par exemple **C:\Program Files\Java\jdk1.7.0_51**

Une fois ceci fait vous pouvez relancer OpenSearchServer en lançant **start.bat**.

Si vous avez Java, que vous ne voyez pas la variable *et* que vous ne savez plus si vous avez un JDK ou une JRE, allez dans le menu Démarrer, cliquez sur l'Invite de commandes, et tapez java -version pour voir ce qui est installé.

### Vérifier que le port 9090 est libre

Si il se trouve que vous avez déjà un serveur qui utilise le port TCP 9090, il va falloir faire une modification dans les fichiers de configuration de **start.bat**

Commencez par vérifier si le port 9090 est occupé via la ligne de commande suivante :

    netstat -o -n -a | findstr 0.0:9090
    
Si la réponse ressemble à ceci, alors ce port est déjà pris :

    TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       676
    
Si c'est le cas, le plus simple est d'attribuer un nouveau port à OSS, comme suit :

Ouvrez le fichier **start.bat** dans le dossier OpenSearchServer pour le modifier.
Trouvez la ligne qui commence par :

    SERVER_PORT=9090

Et changez ce passage en :

    SERVER_PORT=9091

Sauvegardez le fichier et lancez OpenSearchServer en lançant **start.bat**

Votre back office OpenSearch Server est maintenant joignable sur l'URL [http://votreserveur:9091](http://votreserveur:9091)
