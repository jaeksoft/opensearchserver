Si des documents indexés contiennent le mot "ultraviolet" on peut souhaiter renvoyer ces documents lorsque la recherche est faite sur "ultra" ou bien sur "violet".

Cela peut être réalisé en créant un nouvel "analyzer" et en l'appliquant à un nouveau champ du schéma.

L'analyzer doit utiliser le "Ngram filter" pour créer plusieurs mots à partir de mots existant :
![ngram](ngram.png)

Un nouveau champ doit ensuite être créé. Il copiera la valeur du champ existant `content` et utilisera le nouveau `NgramAnalyzer`. 

![ngram field](ngram_field.png)

Une fois les documents ré-indexés et le template de query modifié pour inclure la recherche dans le nouveau champ `mega_content` les documents contenant le mot "ultraviolet" seront renvoyés lors de la recherche de "ultra" ou de "violet".