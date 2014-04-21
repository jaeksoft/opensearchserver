Par défaut OpenSearchServer n'utilise que 256 mo de RAM. Cette valeur n'est pas suffisante pour les utilisations avancées. Pour allouer plus de mémoire à OpenSearchServer vous devez modifier le fichier de démarrage : 

**Pour Linux/Mac (start.sh)**

    JAVA_OPTS="-Xms1G -Xmx1G"

**Pour Windows(start.bat)**

    set JAVA_OPTS=-Xms1G -Xmx1G  

La mémoire allouée est définie par l'option JAVA_OPTS.

`-Xms2G -Xmx2G` means 2 GB of RAM.
Vous pouvez utiliser `-Xms768m -Xmx768m` pour allouer 768 mo de RAM.

## Plus de 2 Go de RAM

Vous devez utiliser OpenSearchServer en mode 64 bits pour pouvoir allouer plus de 2 Go de RAM.

Le premier pré-requis est d'utiliser un système d'exploitation 64 bits avec un Java 64 bits.

Il faut ensuite modifier le fichier `start.sh` comme suit :
  
JAVA_OPTS="-d64 -Xms6G -Xmx6G -server"  

Les paramètres habituels `Xms` and `Xmx` contrôlent la mémoire (6 Go dans l'exemple).

Le paramètre `-d64` active les opérations en 64 bits.

## Quelle taille de RAM est utilisable ?

Observez la valeur **free memory rate** dans l'onglet de monitoring d'OpenSearchServer pour savoir si plus de mémoire est disponible (onglet `/Runtime/System/General`). Une valeur supérieure à 20% est recommandée. 

![Memory usage](outofmemory.png)