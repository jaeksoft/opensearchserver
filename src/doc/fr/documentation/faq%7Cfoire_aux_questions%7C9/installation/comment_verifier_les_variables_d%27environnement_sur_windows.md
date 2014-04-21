1. Faire un clic droit sur "Ordinateur"
2. Sélectionner "Propriétés"
3. Cliquer sur l'onglet "Paramètres système avancésé"
4. Cliquer sur le bouton "Variables d'environnement..."

Une liste de variable s'affiche. Selon le type de moteur JAVA installé sur l'ordinateur la variable à rechercher est `JRE_HOME` ou `JAVA_HOME`.

Si la variable est présente vérifiez qu'elle cible le dossier où est installé JAVA.

Si la variable n'est pas présente il faut la créer :

* Si un JRE est installé, créez la variable `JRE_HOME`. Sa valeur devra être le dossier où est installé Java JRE, par exemple : `C:\Program Files\Java\jre1.6.0_14`.
* Si un JDK est installé, créez la variable `JAVA_HOME`. Sa valeur devra être le dossier où est installé Java JDK, par exemple : `C:\Program Files\Java\jdk1.6.0_14`.

Une fois la variable créé relancer OpenSearchServer en utilisant le script `start.bat`.

Si Java est installé sur votre ordinateur mais que vous ne vous rappelez plus s'il s'agit d'un JDK ou d'un JRE ouvrez le menu Démarrer, cliquez sur Exécuter, tapez `cmd` puis validez. Une fenêtre de ligne de commande s'ouvre. Tapez `java -version` pour voir ce qui est installé. 


## Vérifier si le port 8080 est libre

Si un serveur écoute déjà sur le port TCP 8080 vous devrez modifier le port utilisé par OpenSearchServer. 

Pour savoir si le port 8080 est libre ou non utilisez la commande suivante dans une fenêtre de ligne de commande : `netstat -o -n -a`

Si la réponse obtenue ressemble à celle-ci alors le port n'est pas libre :

    TCP 0.0.0.0:8080 0.0.0.0:0 LISTENING 676

Pour changer le port utilisé par OpenSearchServer ouvrez le fichier `apache-tomcat-6.0.20/conf/server.xml` et localisez la ligne commençant par `<Connector port="8080" ...`.
Changez sa valeur pour `<connector port="8081" ...`

Sauvegardez le fichier et redémarrer OpenSearchServer avec le script `start.bat`. L'interface d'OpenSearchServer est maintenant accessible à cette adresse : http://localhost:8081