Utiliser cette page en référence si vous installez OSS sur un système d'exploitation Linux de type Debian avec le gestionnaire de package.

Cette version simplifiée devrait être suffisante ; si ce n'est pas le cas lisez la version détaillée.

## Version simplifiée

- Assurez-vous d'avoir installé une machine virtuelle Java en version 7 ou supérieure.
- Téléchargez la dernière version stable [opensearchserver.deb](http://www.open-search-server.com/fr/telecharger/  "Téléchargement")
- Installer en utilisant la commande suivante: `dpkg -i opensearchserver-X.X.X-bXXX.deb`
- Démarrez le service avec: `service opensearchserver start`
- Ouvrez votre navigateur favori avec l'URL [http://nomduserveur:9090](http://nomduserveur:9090) (remplacez **nomduserveur** by **localhost** s'il tourne sur votre server local)
- Découvrez OpenSearchServer

## Version détaillée

### Vérifier la version de Java ###

Dans une fenêtre de commande, entrez la ligne suivante:

    java -version

Vérifiez que vous disposez d'une version 7 ou supérieure:

- S'il ne s'agit pas d'une version 7 ou supérieures, vous devez mettre à jour votre machine virtuelle Java
- Si vous n'avez pas Java, installez-le (voir ci-dessous)

### Installer Java

En tant que solution open-source, nous vous recommandons d'installer la dernière version d'OpenJDK (1.7.0 au moment de la rédaction).

Pour installer OpenJDK utilisez le gestionnaire de package:

    apt-get install openjdk-7-jdk

### Installer OpenSearchServer

Nous vous recommandons d'avoir toujours la dernière version d'OSS téléchargeable sur [SourceForge](http://www.open-search-server.com/fr/telecharger/ "Téléchargement").

Téléchargez le package **opensearchserver-X.X.X-bXXX.deb** pour Debian, puis installez-le en utilisant la commande ci-dessous:

    dpkg -i opensearchserver-X.X.X-bXXX.deb
    
Une fois installé vous aurez les répertoires suivants:

- **/var/lib/opensearchserver**: Contient les données (index).
- **/usr/share/opensearchserver**: Contient les fichiers binaires d'OSS.

Vous trouverez également les fichiers suivants:
- **/etc/opensearchserver**: Le fichier de configuration de votre instance OpenSearchServer
- **/etc/init.d/opensearchserver**: Le script utilisé par le système pour démarrer et arrêter l'instance OpenSearchServer.

### Démarrer

Utilisez la commande de service start:

    service opensearchserver start
    
Pour accéder à l'interface d'administration OSS, lancez votre navigateur (Firefox, Chromium, Opera, Safari...) et ouvrez la page [http://nomduserveur:9090](http://nomduserveur:9090)

### Arrêter

Utilisez la commande de service stop:

    service opensearchserver stop
    
## Configuration

Le fichier de configuration est localisé ici: **/etc/opensearchserver**.

### Changer le port d'écoute

Si vous avez déjà un serveur utilisant le port tcp 9090, vous pouvez le modifier dans le fichier de configuration.

Identifiez la ligne contenant:

    SERVER_PORT=9090

Modifiez le port:

    SERVER_PORT=9091

Redémarrez votre instance OpenSearchServer:

    service opensearchserver stop
    service opensearchserver start

Votre interface d'administration OpenSearchServer est maintenant disponible sur cette adresse: [http://nomduserveur:9091](http://nomduserver:9091)

### Modifier l'allocation de mémoire:

Par défaut, OpenSearchServer ne défini pas la mémoire allouée. Il utilise l'allocation par défaut de la machine virtuelle Java.

Si vous souhaitez l'augmenter, modifiez le fichier de configuration comme suit:

Identifiez la ligne suivante:

    #JAVA_OPTS="-Xms1G -Xmx1G"

Décommentez-la, et modifiez la quantité de mémoire à votre convenance:

    JAVA_OPTS="-Xms2G -Xmx2G"
    
Redémarrez OpenSearchServer.

Sachez que si vous positionnez une valeur supérieure à la mémoire physique disponible, OpenSearchServer ne démarrera pas.