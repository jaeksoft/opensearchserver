## How to make an exact search

Some users expect that using double quotes around their keywords will get them "exact" results. Here are explanations about what can be called an "exact search", and how to implement it.

OpenSearchServer uses **two types** of internal queries: **"term" queries** and **"phrase" queries**.

* When **using double quotes around the search keywords OpenSearchServer uses a "phrase" query**. This can also be called a **"proximity search"**.
  * In phrase queries, **"proximity" means "how close the search terms are in documents"**. This level of proximity can be customized by changing the value in the `Phrase slop` field in front of each searched fields (in the `Searched fields` field) or by changing the value of the global `Phrase slop` parameter in the main tab.
    * For example a phrase slop of 2 means that "new york" could match the text "york new" in a document, and a phrase slop of 1 means that "new york" could match "new in york".
     * The default phrase slop value is 0, meaning an exact match.
* **Terms queries are used when no double quotes are used in keywords**. Terms queries will only match a document if it contains every search terms, **no matter what their proximity is**.

You can decide precisely how users should be able to search:
 
* In the "Searched fields" tab, when selecting "Phrase" in the "Mode" column it means that a phrase query will be used on search terms, **even if no double quotes were used**. For instance if someone searches for `new york` it will act as if the keywords were `"new york"`. On the other hand if the keywords are `"new york"` and **the "Term" mode is selected then it will strip the double quotes** and search for `new york`.
* In order to let users decide whether to use phrase queries or term queries **you could select the "Pattern" mode, which will not strip or add anything**. This way users can get different results when searching with double quotes - or without.
