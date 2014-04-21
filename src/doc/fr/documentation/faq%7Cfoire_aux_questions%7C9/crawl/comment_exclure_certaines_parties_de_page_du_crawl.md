OpenSearchServer offre la possibilité d'exclure certaines parties de page lors du crawl web.

### Exclure du contenu en utilisant la classe CSS opensearchserver.ignore

Le contenu de n'importe quel tag HTML possédant la classe `opensearchserver.ignore` sera ignoré lors du crawl web.

La classe `opensearchserver.ignore` peut être ajoutée à d'autres classes CSS déjà positionnées sur un tag.

Exemple :

    <div class="opensearchserver.ignore">Ce contenu ne sera pas indexé dans OpenSearchServer.</div>
    <div class="content opensearchserver.ignore">Ce contenu ne sera pas indexé dans OpenSearchServer.</div>