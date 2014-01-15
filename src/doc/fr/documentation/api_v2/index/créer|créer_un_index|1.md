Cette API créé un nouvel index

**Nécessite:** OpenSearchServer v1.5

### Paramètres d'appel

**URL:** ```/services/rest/index/{index_name}/template/{template_name}```

**Méthode:** ```POST```

**Entête HTTP** (type retourné):
- Accept: ```application/json```
- Accept: ```application/xml```

**Paramètres d'URL:**
- _**index_name**_ (recquis): Le nom de l'index
- _**template_name**_ (optionel): Le nom du template: EMPTY_INDEX, WEB_CRAWLER or FILE_CRAWLER.

### Réponse en cas de succès
L'index a été créé.

**Code HTTP:**: 200

**Type de contenu**: (application/json)

```language-json
{
  result: {
    @successful: "true",
    info: "Created Index my_index"
  }
}
```

### Réponse en cas d'erreur

La création de l'index a échoué. Le motif est indiqué dans le contenu.

**Code HTTP:**: 500

**Type de contenu:** (text/plain):

```asciidoc
directory my_index already exists
```

### Exemple d'appel

**En utilisant Curl:**

```shell
curl -XPOST http://localhost:8080/services/rest/index/my_index/template/WEB_CRAWLER
```

**Avec jQuery:**

```language-javascript
$.ajax({ 
   type: "POST",
   dataType: "json",
   url: "http://localhost:8080/services/rest/index/my_index/template/WEB_CRAWLER"
}).done(function (data) {
   console.log(data);
});
```