Utiliser cette page en référence si vous installez OSS sur un système d'exploitation Linux de type Debian avec le gestionnaire de package.

Cette version simplifiée devrait être suffisante ; si ce n'est pas le cas lisez la version détaillée.

## Version simplifiée

- Assurez-vous d'avoir installé une machine virtuelle Java en version 7 ou supérieure.
- Téléchargez la dernière version stable [opensearchserver.deb](http://www.open-search-server.com/fr/telecharger/  "Téléchargement")
- Installer en utilisant la commande suivante: `dpkg -i opensearchserver.deb`
- Démarrez le service avec: `service opensearchserver start`
- Ouvrez votre navigateur favori avec l'URL [http://yourserver:9090](http://yourserver:9090) (remplacez **yourserver** by **localhost** s'il tourne sur votre server local)
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

    apt-get install jdk

### Installer OpenSearchServer

Nous vous recommandons d'avoir toujours la dernière version d'OSS téléchargeable sur [SourceForge](http://www.open-search-server.com/fr/telecharger/ "Téléchargement").

Téléchargez le package **opensearchserver.deb** pour Debian, puis installez-le en utilisant la commande ci-dessous:

    dpkg -i opensearchserver.deb
    
Une fois installé vous aurez les répertoires suivants:

- **/var/lib/opensearchserver**: Contains all your data (indexes).
- **/usr/share/opensearchserver**: Contains all your OSS binaries.

You also get the following files:
- **/etc/opensearchserver**: The configuration file of your OpenSearchServer instance
- **/etc/init.d/opensearchserver**: The script used by the system to start and stop the OpenSearchServer instance.

### Running it

Just use the service command:

    services opensearchserver start
    
To access your OSS Back Office, open you browser (Firefox, Chromium, Opera, Safari...) and open the page [http://yourserver:9090](http://yourserver:9090)

If everything went right you'll see the OSS interface. If nothing is displaying and you are sure you followed this installation procedure correctly, check the troubleshooting section.

So far, so good ? You can now go to the next step and create your first index.

### Stopping it

Use the service command with the stop parameter:

    services opensearchserver stop
    
## Configuration

The configuration file is located at **/etc/opensearchserver**.

### Changing the listening port

If by chance you already have a server listening on the 9090 tcp port you'll have to make a change in the config files.

Locate the line starting with:

    SERVER_PORT=9090

Then change that to another port:

    SERVER_PORT=9091

Restart your OpenSearchServer instance:

    services opensearchserver stop
    services opensearchserver start

Your OpenSearchServer Back Office is now available at this address: [http://yourserver:9091](http://yourserver:9091)

### Changing the memory usage

By default, OpenSearchServer don't set the memory allocation. It uses the default value from the Java Virtual Machine.

If you need to increase it, just edit the configuration file.

Locate the line with:

    #JAVA_OPTS="-Xms1G -Xmx1G"

Uncomment the line, and set the memory value as you need:

    JAVA_OPTS="-Xms2G -Xmx2G"
    
Restart OpenSearchServer.

Be aware that if you set a value greater than the physical memory available, OpenSearchServer will not start.