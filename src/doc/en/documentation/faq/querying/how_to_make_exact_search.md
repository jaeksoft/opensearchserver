## How to make exact search

Users often expect that using double quotes around the keywords will get them "exact" results. Here are some explanation about exactly what we can call "exact search", and how to implement them.

OpenSearchServer uses **two types** of internal queries: **"term" queries** and **"phrase" queries**.

* When **using double quotes around search keywords OpenSearchServer uses a "phrase" query**. Here phrase query means **"proximity search"**.
  * In phrase queries, **"proximity" means how close searched terms are in documents**. This level of proximity can be customized by changing value of field "Phrase slop" in front of each searched fields (in tab "Searched fields") or by changing value of the global "Phrase slop" parameter in the main tab.
     * For example a phrase slop of 2 means that "new york" could match text "york new" in a document, and **a phrase slop of 1 means that "new york" could match "new in york"**.
     * By default phrase slop is 0, thus asking for exact match.
* **Terms queries are used when no double quotes is used in keywords**. Terms queries will match a document if it contains every search terms, **no matter what their proximity is**.

You can decide precisely how users should be able to search:
 
* In tab "Searched fields", when selecting "Phrase" in column "Mode" it means that a phrase query will be used on searched terms, **even if no double quotes were used**. For instance if someone search for `new york` it will act as if keywords was `"new york"`. On the other hand if keywords are `"new york"` and **mode "Term" is selected then it will strip double quotes** and search for `new york`.
* If you want to let user decide to use "phrase query" or "term query" **you could select mode "Pattern", which will not strip or add anything**. This way users may get different results when searching with double quotes or without.
