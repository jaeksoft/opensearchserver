## Bad credential

**Erreur :**

    <entry key="Exception">com.jaeksoft.searchlib.web.ServletException: com.jaeksoft.searchlib.SearchLibException: Bad credential</entry>

Cette exception survient lorsqu'un compte au moins a été créé dans l'instance OpenSearchServer mais n'est pas correctement utilisé dans les appels d'API.
Tous les appels d'API doivent contenir un paramètre `login` et un paramètre `key`.

**Exemple :**

http://localhost:8080/select?user=index1&login=admin&key=54a51ee4f27cbbcb7a771352b980567f


Voir aussi: [https://github.com/jaeksoft/opensearchserver/wiki/Authentication](https://github.com/jaeksoft/opensearchserver/wiki/Authentication)

## Expected to get a directory path

**Erreur :**

    <entry key="Exception">
      com.jaeksoft.searchlib.web.ServletException: com.jaeksoft.searchlib.SearchLibException: Expected to get a directory path
    </entry>

Cette exception survient lorsque l'index demandé n'est pas trouvé. Vérifiez l'orthographe de l'index dans la liste des indexs.
 
**Exemple :**

http://localhost:8080/select?user=index1 

L'index `index1` doit être visible dans la liste des indexs.
 
## PHP Library: OSS_API won't work whitout curl extension

**Erreur :**  
 
    Fatal error: OSS_API won't work whitout curl extension in OSS_API.class.php on line 23

Le client PHP pour OpenSearchServer nécessite l'extension `php-curl`. Installez et activez cette extension puis redémarrez le serveur web.