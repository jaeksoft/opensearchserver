## How to delete documents

### Deleting documents using the UI

This is done in the `Delete` tab, which is one of the top row tabs.

You need to provide a query that will **search for documents to delete**. It must be written using the standard OpenSearchServer query language, which is the one used by Lucene (see the [Lucene syntax documentation](http://lucene.apache.org/core/2_9_4/queryparsersyntax.html)).

**Examples:**

* Deleting documents that have a `category` field set to `music`: 
    * **`category:music`**
* Deleting all documents except for the ones with a price:
    * **`*:* -price:[* TO *]`**  

Before you actually delete anything, click the `Check` button for a count of documents matching the query. If the number looks wrong, you'll want to double-check.

Then click the `Delete` button to actually delete documents.

#### Advanced deletion

Using the **Scheduler** you can create a `Delete query` task. In the `Query` field, enter your query as discussed in the previous section. If you want to **apply complex filters and search rules on documents to delete, you can use the `Template` field**. Have it point toward a query template previously created in the `Query` tab. 

For instance, if you only want to delete documents indexed during the previous two days, you can use the **powerful `Relative date filter` feature**.

For more about filtering, you can consult the [How to use filters on query](http://www.opensearchserver.com/documentation/faq/querying/how_to_use_filters_on_query.md) page.

### Deleting documents using APIs

Several APIs can be used to delete documents:

* deleting documents that match a given query or search template: [Deleting by query](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_query.md)
* deleting documents that contain the listed values in a specific field: 
    * Using URLs: [Deleting by field](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_field.md)
    * Using JSON: [Deleting by JSON](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_JSON.md)
