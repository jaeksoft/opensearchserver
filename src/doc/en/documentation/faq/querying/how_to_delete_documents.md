## How to delete documents

### Deleting documents from the UI

To delete from from the interface use the main tab `Delete`.

You need to provide a query that will **search for documents to delete**. This query must be written with the standard query language used by OpenSearchServer, which is the one used by Lucene (see [Lucene doc](http://lucene.apache.org/core/2_9_4/queryparsersyntax.html)).

**Examples:**

* Deleting documents where field `category` is `music`: 
    * **`category:music`**
* Deleting all documents except the ones with a price:
    * **`*:* -price:[* TO *]`**  

Click button "Check" to first get the number of documents matching the query. This could help you detect an error in your query if the given number is not the one you expected.
Then click button "Delete"IL to actually delete documents.

#### Advanced deletion

Using the **Scheduler** you can create a job with a `Delete query` task. In the field `Query` you can write as above a query that would search for documents to delete. If you want to **apply several complex filters and search rules on documents to delete you can use the field `Template`**: indicate here a query template previously created in tab `Query`. 

This would for example allow for using **the powerful `Relative date filter` feature**: you could choose to only delete documents that were indexed during the last 2 days.

You can have a look at the page [How to use filters on query](http://www.opensearchserver.com/documentation/faq/querying/how_to_use_filters_on_query.md) to learn more about filtering. 

### Deleting documents with APIs

Several APIs can be used to delete documents:


* delete documents matching a given query or search template: [Deleting by query](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_query.md)
* delete documents that contain the listed values in a specific field: 
    * Giving values in URL: [Deleting by field](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_field.md)
    * Giving values as JSON: [Deleting by JSON](http://www.opensearchserver.com/documentation/api_v2/document/delete_by_JSON.md)