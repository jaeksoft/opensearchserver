Ces APIs sont utilisées pour interfacer votre application avec OpenSearchServer. Il y a deux versions:

L'API v1 est la version historique. Elle inclut des fonctions de bas niveau toujours utilisées en interne par OpenSearchServer, elles restent maintenues. Cependant, aucune nouvelle fonction devrait y être ajoutée. Cet API ne doit être utilisé que par ceux développant des fonctions internes à OpenSearchServer.

L'API v2 est la nouvelle API REST. L'implémentation RESTFUL de cet API lui permet de controller la quasi totalité des fonctions d'OpenSearchServer. L'API v2 est fréquemment mise à jour et enrichie de nouvelles fonctions.